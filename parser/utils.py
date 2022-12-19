#!/usr/bin/env python3


import bs4
import bs4.element
import itertools
import os
import re
import urllib

PAGES_CACHE_FOLDER = "./output/cache/"


def read_file(file_path: str, encoding="utf-8") -> str:
    with open(file_path, "r", encoding=encoding) as file:
        try:
            return file.read()
        except OSError as e:
            raise Exception(f"error '{e}' while reading {file_path}")
    return None


def download_page(url: str) -> str:
    parsed_url = urllib.parse.urlparse(urllib.parse.unquote(url))
    fullurl = f"{parsed_url.scheme}://{parsed_url.netloc}{parsed_url.path}"

    if parsed_url.query:
        fullurl += f"?{parsed_url.query}"
    if parsed_url.fragment:
        fullurl += f"#{parsed_url.fragment}"

    full_path = f"{PAGES_CACHE_FOLDER}{parsed_url.netloc}{parsed_url.path}"

    # Check if file exists before dl it
    if os.path.exists(full_path):
        file = read_file(full_path, "utf-8")
        if file:
            return file
        # else just keep going and download the file then

    # Use custom user agent to bypass blocking of known spider/bot user agents
    request = urllib.request.Request(
        url=url,
        headers={"User-Agent": "Mozilla/5.0"}
    )

    try:
        page = urllib.request.urlopen(request)
    except urllib.error.URLError as e:
        print(f"error '{e}' while downloading page {url}")
        return None

    page_source = page.read()
    html = page_source.decode("utf-8")

    # Save file
    try:
        dir_path = os.path.dirname(full_path)
        os.makedirs(dir_path, exist_ok=True)

        with open(file=full_path, mode="w", encoding="utf-8") as file:
            file.write(html)
    except OSError as e:
        print(f"error '{e}' while writing page file {full_path}")
        # return None # just keep going and return the file

    return html


# Code by Martijn Pieters (https://stackoverflow.com/a/48451104)
def table_to_2d(html_table: str):
    # soup = bs4.BeautifulSoup(result, 'lxml')
    soup = bs4.BeautifulSoup(html_table, 'html.parser')

    rowspans = []  # track pending rowspans
    rows = soup.table.find_all('tr')

    # first scan, see how many columns we need
    colcount = 0
    for r, row in enumerate(rows):
        cells = row.find_all(['td', 'th'], recursive=False)
        # count columns (including spanned).
        # add active rowspans from preceding rows
        # we *ignore* the colspan value on the last cell, to prevent
        # creating 'phantom' columns with no actual cells, only extended
        # colspans. This is achieved by hardcoding the last cell width as 1.
        # a colspan of 0 means “fill until the end” but can really only apply
        # to the last cell; ignore it elsewhere.
        colcount = max(
            colcount,
            sum(int(c.get('colspan', 1)) or 1 for c in cells[:-1]) + len(cells[-1:]) + len(rowspans))
        # update rowspan bookkeeping; 0 is a span to the bottom.
        rowspans += [int(c.get('rowspan', 1)) or len(rows) - r for c in cells]
        rowspans = [s - 1 for s in rowspans if s > 1]

    # it doesn't matter if there are still rowspan numbers 'active'; no extra
    # rows to show in the table means the larger than 1 rowspan numbers in the
    # last table row are ignored.

    # build an empty matrix for all possible cells
    table = [[None] * colcount for row in rows]

    # fill matrix from row data
    rowspans = {}  # track pending rowspans, column number mapping to count
    for row, row_elem in enumerate(rows):
        span_offset = 0  # how many columns are skipped due to row and colspans
        for col, cell in enumerate(row_elem.find_all(['td', 'th'], recursive=False)):
            # adjust for preceding row and colspans
            col += span_offset
            while rowspans.get(col, 0):
                span_offset += 1
                col += 1

            # fill table data
            rowspan = rowspans[col] = int(cell.get('rowspan', 1)) or len(rows) - row
            colspan = int(cell.get('colspan', 1)) or colcount - col
            # next column is offset by the colspan
            span_offset += colspan - 1

            types = (bs4.element.NavigableString, bs4.element.CData)
            value = cell.get_text(separator=" ", strip=True, types=types)

            for drow, dcol in itertools.product(range(rowspan), range(colspan)):
                try:
                    table[row + drow][col + dcol] = value
                    rowspans[col + dcol] = rowspan
                except IndexError:
                    # rowspan or colspan outside the confines of the table
                    pass

        # update rowspan bookkeeping
        rowspans = {c: s - 1 for c, s in rowspans.items() if s > 1}

    return table


def replace_imgs_by_alt_texts(html: str):
    newstring = ""
    start = 0
    for match in re.finditer(r"<img\s+alt=\"(.*?)\".*?>", html):
        end, newstart = match.span()
        newstring += html[start:end]  # Add what precedes

        rep = match.group(1).replace(" ", "_")  # Add img alt text with spaces replaced by _
        newstring += rep

        start = newstart
    newstring += html[start:]

    return newstring

    html = re.sub("<img\s+alt=\"(.*?)\".*?>", "\\1", html, flags=re.DOTALL)  # DOTALL/s to match multiline img tags

    return html


def get_generated_warning_xml():
    warning = "<!-- *********************************************************************** -->\n" \
              "<!--   WARNING! Auto-generated file, all manual changes made will be lost!   -->\n" \
              "<!-- *********************************************************************** -->"
    return warning

def get_generated_warning_kotlin():
    warning = "/* ************************************************************************** */\n" \
              "/*    WARNING! Auto-generated file, all manual changes made will be lost!     */\n" \
              "/* ************************************************************************** */"
    return warning