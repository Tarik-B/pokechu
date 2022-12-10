#!/usr/bin/env python3

import parsel
import re
import pokedex
import utils

class EvolutionsParser:
    def __init__(self, pokedex: pokedex.Pokedex):
        self.pokedex = pokedex

    def process_evolution_list_page(self, lang: str):
        if lang == "fr":
            full_url = "https://www.pokepedia.fr/Liste_des_Pok%C3%A9mon_par_famille_d%27%C3%A9volution"
        else:
            full_url = "https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_evolution_family"

        html = utils.download_page(full_url)

        html = html.replace("\n", "")

        # Get evolution tables
        xpath_selector = parsel.Selector(html)
        if lang == "fr":
            results = xpath_selector.xpath("//table[@class = 'tableaustandard centre']").getall()
        else:
            results = xpath_selector.xpath("//table[@class = 'roundy']").getall()

        # DISABLED, can't work, we need all pokemon evolution trees cause paldea pokedex includes old gen pokemons
        #
        # Use table titles to keep only table of selected pokedex
        # if self.pokedex.type != PokedexType.NDEX:
        #
        #     # Get evolution tables titles
        #     xpath_selector = parsel.Selector(html)
        #     if lang == "fr":
        #         # <span class="mw-headline">
        #         result_titles = xpath_selector.xpath("//h3/span[@class = 'mw-headline']/text()").getall()
        #     else:
        #         pass
        #
        #     if len(results) != len(result_titles):
        #         raise Exception("error while parsing evolution table titles, count different from tables")
        #
        #     # Get generation string
        #     if lang == "fr":
        #         gen_name = Pokedex.POKEDEX_TYPES[self.pokedex.type].gen_fr
        #     else:
        #         pass
        #
        #     results = [results[i] for i in range(len(results)) if gen_name.lower() in result_titles[i].lower()]

        # Clean tables and split in rows
        evolution_rows = self.split_html_tables_in_rows(lang, results)

        # Build evolution trees
        self.process_evolution_rows(lang, evolution_rows)

        # Remove trees with only one node
        self.pokedex.remove_single_node_evolution_trees()

    def split_html_tables_in_rows(self, lang: str, tables: list) ->  list:

        EVOLUTION_FAMILY_STRING = "famille"
        EVOLUTION_FAMILY_STRING_ENGLISH = "family"

        cleaned_rows = []
        for table in tables:

            # Replace all img by their alt texts
            table = utils.replace_imgs_by_alt_texts(table)

            table_2d: list[list[str]] = utils.table_to_2d(table)
            # pprint(table_2d, width=30)
            for row in table_2d:

                if len(row) == 0:
                    continue

                cleaned_row = []
                cleaned_index = 0
                for cell in row:

                    if not cell:
                        continue

                    cell = cell.replace('"', '')

                    # Remove leading/trailing whitespaces/line breaks
                    cell = cell.strip()

                    # Skip pokemon family rows
                    if lang == "fr":
                        if cell.lower().startswith(EVOLUTION_FAMILY_STRING):
                            break
                    else:
                        if cell.lower().endswith(EVOLUTION_FAMILY_STRING_ENGLISH):
                            break

                    # Remove pokemon ids XXX (useless here)
                    cell = re.sub(r"\b(\d{3})\b", "", cell)

                    if lang == "fr":
                        # Remove "Gen. X"
                        cell = re.sub(r"Gen. [0-9]", "", cell)

                    cell = re.sub(r"Fichier:.*\.png", "", cell)

                    # Remove non breaking spaces
                    cell = cell.replace(u'\xa0', u' ')

                    cell = cell.replace("►", "")
                    cell = cell.replace("→", "")

                    # Remove leading/trailing whitespaces/line breaks
                    cell = cell.strip()

                    # Remove filename of images
                    cell = re.sub(r"\w+\.png", "", cell)

                    if lang == "en":
                        # Remove "(Kantonian)", "(Galarian)", etc.
                        cell = re.sub(r" \(.*\)", "", cell)

                    if cell == "":
                        continue

                    if cell not in cleaned_row:

                        # Pokemon names
                        if cleaned_index % 2 == 0:
                            pokemon_name = cell

                            # if not self.pokedex.has_pokemon_entry(pokemon_name, lang):
                            #     print(f"pokemon not found = '{pokemon_name}'")

                        cleaned_row.append(cell)
                        cleaned_index += 1

                if len(cleaned_row) > 1:

                    if len(cleaned_row) % 2 == 0:
                        # raise Exception("error while parsing evolution rows")
                        continue

                    cleaned_rows.append(cleaned_row)

        return cleaned_rows

    def process_evolution_rows(self, lang: str, cleaned_rows: list):
        for row in cleaned_rows:

            current_tree = None
            current_node = None

            for i in range(0, len(row), 2):
                pokemon_name = row[i]

                if pokemon_name == "Sprigatito":
                    print("found it")

                if not self.pokedex.has_pokemon_entry(pokemon_name, lang):
                    # print(f"unknown pokemon '{pokemon_name}', skipping the rest of the tree")
                    break

                # Find if already in a tree
                if current_tree is None:
                    current_tree, current_node = self.pokedex.find_in_evolution_trees(pokemon_name, lang)

                if current_tree is None:
                    if i != 0:
                        raise Exception("error while parsing evolution trees")

                    # Not found, create new tree
                    current_tree = current_node = self.pokedex.add_evolution_node(None, pokemon_name, "", lang)

                else:
                    # Skip to next if we found the pokemon
                    if self.pokedex.is_pokemon_evolution_node(current_node, pokemon_name, lang):

                        if i != 0 and lang == "en":
                            evolution_condition = row[i - 1]
                            current_node["condition_en"] = evolution_condition

                        continue

                    # Search in current tree
                    node = self.pokedex.find_in_evolution_tree(current_node, pokemon_name, lang)
                    if node:
                        current_node = node

                        if i != 0 and lang == "en":
                            evolution_condition = row[i - 1]
                            current_node["condition_en"] = evolution_condition
                    else:
                        # Not found, add to tree
                        evolution_condition = row[i - 1]

                        # self.process_evolution_condition(evolution_condition)

                        current_node = self.pokedex.add_evolution_node(current_node, pokemon_name,
                                                                       evolution_condition, lang)