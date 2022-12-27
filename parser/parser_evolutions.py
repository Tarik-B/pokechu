#!/usr/bin/env python3

import parsel
import re

import utils

from pokedex import Pokedex

class EvolutionsParser:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose

    def process_evolution_list_page(self, lang: str):
        if lang == "fr":
            full_url = "https://www.pokepedia.fr/Liste_des_Pok%C3%A9mon_par_famille_d%27%C3%A9volution"
        else:
            full_url = "https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_evolution_family"

        html = utils.download_page(full_url)
        if not html:
            print(f"error while downloading page '{full_url}'")

        html = html.replace("\n", "")

        # Get evolution tables
        xpath_selector = parsel.Selector(html)
        if lang == "fr":
            results = xpath_selector.xpath("//table[@class = 'tableaustandard centre']").getall()
        else:
            results = xpath_selector.xpath("//table[@class = 'roundy']").getall()

        # Clean tables and split in rows
        evolution_rows = self.split_html_tables_in_rows(lang, results)

        # Build evolution trees
        self.process_evolution_rows(lang, evolution_rows)

        # Remove trees with only one node
        self._pokedex.remove_single_node_evolution_trees()

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

                    # Removes some patterns between parenthesis
                    patterns_parenthesis = [ "♀",'' "♂", "Pokémon Épée et Bouclier" ]
                    for pattern in patterns_parenthesis:
                        cell = re.sub(r"\(\s*" + pattern + r"\s*\)", "", cell)

                    # Ignore some games, remove everything between "or" and the name of the game between parenthesis
                    games_to_ignore = ["Pokémon XD", "Pokémon Donjon Mystère"]
                    for game in games_to_ignore:
                        cell = re.sub(r"ou(?!.*ou).*\(\s*" + game + r"\s*\)", "", cell)

                    # if "(" in cell or ")" in cell:
                        # print("parenthesis = " + cell)

                    if lang == "fr":
                        # Remove "Gen. X"
                        cell = re.sub(r"Gen. [0-9] ", "", cell)

                    # TODO Fix this, it removes "1000" from "Marcher 1000 pas (...)"
                    # Remove pokemon ids XXX/XXXX (useless here)
                    while(True):
                        cell_new = re.sub(r"^\d{3,4} ", "", cell)
                        if cell_new != cell:
                            cell = cell_new
                        else:
                            break

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

                if not self._pokedex.has_pokemon_entry(pokemon_name, lang):
                    if self._verbose:
                        print(f"unknown pokemon '{pokemon_name}', skipping the rest of the tree")
                    break

                # Find if already in a tree
                if current_tree is None:
                    current_tree, current_node = self._pokedex.find_in_evolution_trees(pokemon_name, lang)

                if current_tree is None:
                    if i != 0:
                        raise Exception("error while parsing evolution trees")

                    # Not found, create new tree
                    current_tree = current_node = self._pokedex.add_evolution_node(None, pokemon_name, None, lang)

                else:
                    # Skip to next if we found the pokemon
                    if self._pokedex.is_pokemon_evolution_node(current_node, pokemon_name, lang):

                        if i != 0 and lang == "en":
                            evolution_condition = row[i - 1]
                            # current_node["condition_en"] = evolution_condition

                        continue

                    # Search in current tree
                    node = self._pokedex.find_in_evolution_tree(current_node, pokemon_name, lang)
                    if node:
                        current_node = node

                        if i != 0 and lang == "en":
                            evolution_condition = row[i - 1]
                            # current_node["condition_en"] = evolution_condition
                    else:
                        # Not found, add to tree
                        evolution_condition = row[i - 1]

                        # self.process_evolution_condition(evolution_condition)

                        # Remove everything between parenthesis
                        # evolution_condition = re.sub(r"\(.*?\)", "", evolution_condition)
                        evolution_condition = evolution_condition.strip()

                        current_node = self._pokedex.add_evolution_node(current_node, pokemon_name,
                                                                        evolution_condition, lang)