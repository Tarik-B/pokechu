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
        results = xpath_selector.xpath("//table[@class = 'tableaustandard centre']").getall()

        # Clean tables
        cleaned_table = []
        for result in results:
            table: list[list[str]] = table_to_2d(result)
            # pprint(table, width=30)
            for row in table:

                if len(row) == 0:
                    continue

                cleaned_row = []
                cleaned_index = 0
                for cell in row:

                    cell = cell.replace('"', '')

                    if cell.startswith("Famille"):
                        break

                    # Remove "Gen. X"
                    cell = re.sub('Gen. [0-9]', '', cell)

                    cell = re.sub('Fichier:.*\.png', '', cell)

                    # Remove non breaking spaces
                    cell = cell.replace(u'\xa0', u' ')

                    cell = cell.replace("►", "")

                    # Remove leading/trailing whitespaces/line breaks
                    cell = cell.strip()

                    if cell not in cleaned_row:

                        # Pokemon names
                        if cleaned_index % 2 == 0:
                            pokemon_name = cell

                            if not self.pokedex.has_pokemon_entry(pokemon_name):
                                print("pokemon not found = " + pokemon_name)

                        cleaned_row.append(cell)
                        cleaned_index += 1

                if len(cleaned_row) > 1:

                    if len(cleaned_row) % 2 == 0:
                        raise Exception("error while parsing evolution rows")

                    cleaned_table.append(cleaned_row)

        # [' Bulbizarre', 'Niveau 16 ►', ' Herbizarre', 'Niveau 32 ►', ' Florizarre', '◄ Méga-Évolution ►', ' Méga-Florizarre']
        # [' Bulbizarre', 'Niveau 16 ►', ' Herbizarre', 'Niveau 32 ►', ' Florizarre', '◄ Gigamax ►', ' Florizarre Gigamax']

        # Build evolution trees
        for row in cleaned_table:

            current_tree = None
            current_node = None

            for i in range(0, len(row), 2):
                pokemon_name = row[i]

                if not self.pokedex.has_pokemon_entry(pokemon_name):
                    print(f"unknown pokemon '{pokemon_name}', skipping the rest of the tree")
                    break

                # Find if already in a tree
                if current_tree is None:
                    current_tree, current_node = self.pokedex.find_in_evolution_trees(pokemon_name)

                if current_tree is None:
                    if i != 0:
                        raise Exception("error while parsing evolution trees")

                    # Not found, create new tree
                    current_tree = current_node = self.pokedex.add_evolution_node(None, pokemon_name, "")
                else:
                    # Skip to next if we found the pokemon
                    if self.pokedex.is_pokemon_evolution_node(current_node, pokemon_name):
                        continue

                    # Search in current tree
                    node = self.pokedex.find_in_evolution_tree(current_node, pokemon_name)
                    if node:
                        current_node = node
                    else:
                        # Not found, add to tree
                        evolution_condition = row[i-1]

                        current_node = self.pokedex.add_evolution_node(current_node, pokemon_name, evolution_condition)

        # Remove trees with only one node
        self.pokedex.remove_single_node_evolution_trees()

        # self.pokedex.print_trees()