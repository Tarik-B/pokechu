#!/usr/bin/env python3

from parsel import Selector
from urllib.request import urlopen
import re
import pokedex
from html_table_parser import table_to_2d


class EvolutionsParser:
    def __init__(self, pokedex: pokedex.Pokedex):
        self.pokedex = pokedex

    def process_evolution_list_page(self):
        print(f"+ Processing evolution list page")

        full_url = "https://www.pokepedia.fr/Liste_des_Pok%C3%A9mon_par_famille_d%27%C3%A9volution"
        html = urlopen(full_url).read().decode("utf-8")

        # Get evolution tables
        xpath_selector = Selector(html)
        # results = xpath_selector.xpath(
        #     "//table[@class = 'tableaustandard centre']/tbody/tr/td").getall()
        results = xpath_selector.xpath("//table[@class = 'tableaustandard centre']").getall()

        cleaned_table = []
        for result in results:
            table: list[list[str]] = table_to_2d(result)
            # pprint(table, width=30)
            for row in table:

                if len(row) == 0:
                    continue

                if row[0].startswith("Famille"):
                    continue

                cleaned_row = []
                for cell in row:

                    # Remove ending \n
                    cell = cell.rstrip()
                    # Remove "Gen. X"


                    if cell not in cleaned_row:
                        cleaned_row.append(cell)

                cleaned_table.append(cleaned_row)

        for row in cleaned_table:
            print(row)

        # Get evolution tables
        # xpath_selector = Selector(html)
        # results = xpath_selector.xpath(f"//table[tbody/tr/th[contains(text(), 'évolution')]]")
        if len(results) != 1:
            # raise Exception("error while parsing evolution table")
            # No evolutions
            # continue
            return

    def process_pokemon_page(self, unique_id: str, name_fr: str):

        print(f"+ Processing {name_fr}")

        # Fetch pokepedia page
        base_url = "https://www.pokepedia.fr/Pok%C3%A9mon_n%C2%B0"

        full_url = base_url + unique_id
        html = urlopen(full_url).read().decode("utf-8")

        # Get evolution table
        xpath_selector = Selector(html)
        results = xpath_selector.xpath(f"//table[tbody/tr/th[contains(text(), 'évolution')]]")
        if len(results) != 1:
            # raise Exception("error while parsing evolution table")
            # No evolutions
            # continue
            return

        self.process_evolution_table(name_fr, results.get())

    def process_evolution_table(self, name_fr: str, html_table: str):

        xpath_selector = Selector(html_table)
        results = xpath_selector.xpath(f"//table/tbody/tr/td/a[not(*)]/text() | "
                                       f"//table/tbody/tr/td/small").getall()

        # Parse evolutions
        evolution_table = []
        current_row = []
        is_name_row = True
        cell_index = 0

        for i in range(len(results)):
            row = results[i]
            # Evolution rows (level or something else)
            if row.startswith("<"):

                if is_name_row:
                    is_name_row = False
                    cell_index = 0

                    # Validate current evolution row
                    evolution_table.append(current_row)
                    current_row = []

                # Flatten text from node and children
                subselector = Selector(row)
                rowtext = "".join(subselector.xpath(f"//small//text()").getall())
                rowtext = rowtext.removesuffix("  ▼")

                match = re.search(r"^Niveau (\d+)*", rowtext)

                if match is not None:
                    evolution_type = pokedex.EvolutionType.LEVEL
                    data = match.group(1)  # lvl number
                else:
                    evolution_type = pokedex.EvolutionType.OTHER
                    data = rowtext  # other info

                current_row.append((None, (evolution_type, data)))

            else:  # Pokemon name rows

                if not is_name_row:
                    is_name_row = True
                    cell_index = 0

                if len(evolution_table) == 0:
                    current_row.append((row, None))
                else:
                    current_row[cell_index] = (row, current_row[cell_index][1])

            cell_index += 1

        if current_row:
            evolution_table.append(current_row)

        self.pokedex.add_evolution_tree(evolution_table)
