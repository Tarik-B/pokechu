#!/usr/bin/env python3

from parsel import Selector
from urllib.request import urlopen
import re
from enum import Enum, auto

class PokedexType(Enum):
    DEX_PALDEA_EV = auto()

class EvolutionTableType(Enum):
    NAME = auto()
    LEVEL = auto()
    OTHER = auto()

def process_pokedex(pokedex_type: PokedexType):

    # Fetch pokemon list
    base_url = "https://www.pokepedia.fr/"
    full_url = base_url + pokedex_type.name

    html = urlopen(full_url).read().decode("utf-8")

    # Get pokemon list table
    xpath_selector = Selector(html)
    results = xpath_selector.xpath("//table[@class = 'tableaustandard sortable entetefixe']/tbody/tr/td[1]").getall()

    # Pokemon ids (xpath selector removes duplicates, need to use text() on each td)
    ids = []
    for result in results:
        subselector = Selector(result)
        ids.append( subselector.xpath("//td/text()").get() )

    # Fill blanks in ids
    for pokemon_index in range(len(ids)):
        if ids[pokemon_index] is None:
            ids[pokemon_index] = ids[pokemon_index - 1]

    print(f"Pokemon count = {len(ids)}")

    # Names
    names_fr = xpath_selector.xpath("//tr/td[@id]/a/text()").getall()
    if len(ids) != len(names_fr):
        raise Exception("error while parsing french names")

    names_en = xpath_selector.xpath("//tr/td[a[starts-with(@title, 'en:')]]/a/text()").getall()
    if len(ids) != len(names_fr):
        raise Exception("error while parsing english names")

    unique_ids = xpath_selector.xpath("//tr/td/a/img[number(@alt) = @alt]/@alt").getall()
    if len(ids) != len(names_fr):
        raise Exception("error while parsing real unique ids")

    # results = sel.xpath("//tr/td[3]/a/text()").getall()

    # Fetch pokepedia page
    base_url = "https://www.pokepedia.fr/Pok%C3%A9mon_n%C2%B0"
    for pokemon_index in range(len(unique_ids)):
        unique_id = unique_ids[pokemon_index]
        name_fr = names_fr[pokemon_index]

        # unique_id = "280"
        # name_fr = "Tarsal"

        full_url = base_url + unique_id
        html = urlopen(full_url).read().decode("utf-8")

        # Get evolution table
        xpath_selector = Selector(html)
        results = xpath_selector.xpath(f"//table[tbody/tr/th[contains(text(), 'évolution')]]")
        if len(results) != 1:
            #raise Exception("error while parsing evolution table")
            # No evolutions
            continue

        xpath_selector = Selector(results.get())
        results = xpath_selector.xpath(f"//table/tbody/tr/td/a[not(*)]/text() | "
                                       f"//table/tbody/tr/td/small").getall()

        # Parse evolutions
        evolutions = []
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
                    evolution_type = EvolutionTableType.LEVEL
                    data = match.group(1) # lvl number
                else:
                    evolution_type = EvolutionTableType.OTHER
                    data = rowtext # other info

                evolutions.append((evolution_type, data))

                current_row.append((None, (evolution_type, data)))

            else: # Pokemon name rows

                if not is_name_row:
                    is_name_row = True
                    cell_index = 0

                if len(evolution_table) == 0:
                    evolutions.append((EvolutionTableType.NAME, row))
                    current_row.append((row, None))
                else:
                    current_row[cell_index] = (row, current_row[cell_index][1])

            cell_index += 1

        if current_row:
            evolution_table.append(current_row)

        # Find pokemon indexes in list
        row_index = -1
        column_index = -1
        for i in range(len(evolution_table)):
            for j in range(len(evolution_table[i])):
                if evolution_table[i][j][0] == name_fr:
                    row_index = i
                    column_index = j
                    break

        if row_index < 0 or column_index < 0:
             raise Exception("error while parsing evolutions")

        subevolutions = []
        if row_index != 0: # has subevolutions
            previous_column_index = column_index if column_index < len(evolution_table[row_index-1]) else 0

            subevolution_name = evolution_table[row_index-1][previous_column_index][0] # previous row name
            subevolution_type = evolution_table[row_index][column_index][1] # current row evolution type
            subevolutions.append((subevolution_name, subevolution_type))

        surevolutions = []
        if row_index != len(evolution_table) - 1: # has surevolutions

            # Split next row by current number of pokemons in current row
            current_count = len(evolution_table[row_index])
            next_count = len(evolution_table[row_index+1])

            if next_count % current_count != 0:
                raise Exception("error while parsing surevolutions: current and next row pokemon count arent multiples")

            evolutions_per_pokemon = next_count//current_count
            start_index = column_index * evolutions_per_pokemon
            last_index = start_index + evolutions_per_pokemon

            for i in range(start_index, last_index):
                surevolutions.append(evolution_table[row_index + 1][i])

        print("")

    print(html)
    # for result in results:
        # print(result)

if __name__ == "__main__":
    # try:
    #     with open("list.txt", "r") as file:
    #         text = file.read()
    # except (IOError, OSError, FileNotFoundError, PermissionError, OSError):
    #     print("Error reading file")

    process_pokedex(PokedexType.DEX_PALDEA_EV)