#!/usr/bin/env python3

import json
import os
from collections import OrderedDict

from data import Data
from enums import Item, PokedexType
import csv
class PokedexTypeData:
    def __init__(self, name_fr: str, gen_fr: str):
        self.name_fr = name_fr
        self.gen_fr = gen_fr

class Pokedex:
    def __init__(self, type: PokedexType):
        self.type = type
        self.pokemons = dict()
        self.evolution_trees = list()
        self.pokemon_names = dict()
        # Used only to speedup ids lookup
        self.names_to_unique_ids = dict()

        self.names_to_unique_ids["fr"] = dict()
        self.names_to_unique_ids["en"] = dict()

        # self.pokemon_names["fr"] = dict()
        # self.pokemon_names["en"] = dict()

    def add_pokemon_entry(self, unique_id: str, names: list, thumbnail_filename: str):
        pokemon = {}

        # ids = {"unique": unique_id}
        # pokemon["ids"] = ids

        # images = {"thumbnail": thumbnail_filename}
        pokemon["thumbnail"] = thumbnail_filename

        self.pokemons[unique_id] = pokemon

        self.pokemon_names[unique_id] = names

        for lang in names:
            name = names[lang]
            if lang in self.names_to_unique_ids:
                self.names_to_unique_ids[lang][name] = unique_id

    def has_pokemon_entry(self, name: str, lang: str) -> bool:
        return self.convert_name_to_unique_id(name, lang) is not None

    def add_pokemon_ids(self, unique_id: str, ids: list):
        if unique_id not in self.pokemons:
            return

        # ids is a list of tuple(PokedexType,str)
        id_list = []
        for id_tuple in ids:
            type = id_tuple[0]
            id = id_tuple[1]

            id_list.append( { "type": PokedexType(type).name, "id": id })

        self.pokemons[unique_id]["ids"] = id_list
    def convert_name_to_unique_id(self, name: str, lang: str) -> str:
        # for id in self.pokemon_names:
        #     names = self.pokemon_names[id]
        #     if names[lang] == name:
        #         return id
        #
        # return None
        if lang not in self.names_to_unique_ids:
            return None

        if name not in self.names_to_unique_ids[lang]:
            return None

        return self.names_to_unique_ids[lang][name]


    def add_evolution_node(self, parent: dict, name: str, condition: str, lang: str) -> dict:
        unique_id = self.convert_name_to_unique_id(name, lang)
        if unique_id is None:
            return None

        new_node = {"id": unique_id}
        if condition is not None:
            new_node["condition_raw"] = condition
        # new_node["evolutions"] = []

        if parent:
            if "evolutions" not in parent:
                parent["evolutions"] = []
            parent["evolutions"].append(new_node)
        else:
            self.evolution_trees.append(new_node)

        # Return a reference to newly created node
        return new_node

    def is_pokemon_evolution_node(self, node: dict, name: str, lang: str) -> bool:
        unique_id = self.convert_name_to_unique_id(name, lang)
        if unique_id is None:
            return False

        return node["id"] == unique_id

    def find_in_evolution_trees(self, name: str, lang: str):
        for tree in self.evolution_trees:
            node = self.find_in_evolution_tree(tree, name, lang)
            if node:
                return tree, node

        return None, None

    def find_in_evolution_tree(self, node: dict, name: str, lang: str) -> dict:

        if self.is_pokemon_evolution_node(node, name, lang):
            return node

        if "evolutions" not in node:
            return None

        for child in node["evolutions"]:
            found = self.find_in_evolution_tree(child, name, lang)
            if found:
                return found

        return None

    def remove_single_node_evolution_trees(self):
        self.evolution_trees = [tree for tree in self.evolution_trees
                                if ("evolutions" in tree and len(tree["evolutions"]) != 0)]

    def print_trees(self):
        for tree in self.evolution_trees:
            self.print_tree(tree)

    def print_tree(self, node, level=0):

        how = (" (" + node["condition"] + ")") if node["condition"] else ""
        print("    " * (level - 1) + "+---" * (level > 0) + node["id"] + how)

        for child in node["evolutions"]:
            self.print_tree(child, level + 1)

    def clear_evolution_trees(self):
        self.evolution_trees = list()

    def save_pokemon_list_json(self, file_path: str):
        pretty_json = json.dumps(self.pokemons, indent=4, ensure_ascii=False)
        with open(file_path, "w") as outfile:
            # json.dump(parser.pokedex.pokemons, outfile)
            outfile.write(pretty_json)
    def save_pokemon_list_csv(self, file_path: str):
        #
        # with open(file_path, "w") as csvfile:
        #     fieldnames = ['first_name', 'last_name']
        #     writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        #
        #     writer.writeheader()
        #     writer.writerow({'first_name': 'Baked', 'last_name': 'Beans'})
        #     writer.writerow({'first_name': 'Lovely', 'last_name': 'Spam'})
        #     writer.writerow({'first_name': 'Wonderful', 'last_name': 'Spam'})

        with open(file_path, "w") as csvfile:
            # writer = csv.writer(csvfile, delimiter=',', quotechar='|', quoting=csv.QUOTE_MINIMAL)
            writer = csv.writer(csvfile, delimiter=',')

            writer.writerow(["id", "thumbnail"])

            for id in self.pokemons:
                pokemon = self.pokemons[id]
                thumbnail = pokemon["thumbnail"]

                writer.writerow([id, thumbnail])

    def save_evolution_trees(self, file_path: str):

        # Sort trees dict keys
        sorted_trees = []
        for i in range(len(self.evolution_trees)):
            sorted_tree = self.sort_evolution_trees_keys_in_order(self.evolution_trees[i], ['id',
                                                                                            'condition_raw',
                                                                                            'conditions',
                                                                                            'evolutions'])
            sorted_trees.append(sorted_tree)

        pretty_json = json.dumps(sorted_trees, indent=4, ensure_ascii=False)
        with open(file_path, "w") as outfile:
            outfile.write(pretty_json)

    def sort_evolution_trees_keys_in_order(self, dictionary: dict, key_order: list) -> OrderedDict:

        ordered_dictionary = OrderedDict(dictionary)
        for key in key_order:
            if key in ordered_dictionary:
                ordered_dictionary.move_to_end(key)

        if "evolutions" in ordered_dictionary:
            evolutions = ordered_dictionary["evolutions"]
            for i in range(len(evolutions)):
                evolution = evolutions[i]
                evolutions[i] = self.sort_evolution_trees_keys_in_order(evolution, key_order)

        return ordered_dictionary

    def save_pokemon_names(self, file_path: str):
        if len(self.pokemon_names) == 0:
            return

        # Save strings.xml ids declaration
        self.save_pokemon_names_lang(file_path, "")

        # Save strings-XXX.xml with localized names
        self.save_pokemon_names_lang(file_path, "fr")
        self.save_pokemon_names_lang(file_path, "en")

    def save_pokemon_names_lang(self, file_path: str, lang: str):

        if lang != "":
            file_name = os.path.splitext(file_path)[0]
            extension = os.path.splitext(file_path)[1]
            file_path = f"{file_name}-{lang}{extension}"

        with open(file_path, "w") as output_file:

            output_file.write("<?xml version='1.0' encoding='utf-8'?>\n")

            output_file.write("<resources>\n")
            for id in self.pokemon_names:
                int_id = int(id)
                if lang == "":
                    output_file.write(f"    <string name=\"pokemon_name_{int_id}\"/>\n")
                else:
                    names = self.pokemon_names[id]
                    if lang in names:
                        name = json.dumps(names[lang]) # escapes quotes
                        output_file.write(f"    <string name=\"pokemon_name_{int_id}\">{name}</string>\n")

            output_file.write("</resources>")

    def save_region_names(self, file_path: str):
        if len(self.pokemon_names) == 0:
            return

        # Save strings.xml ids declaration
        self.save_region_names_lang(file_path, "")

        # Save strings-XXX.xml with localized names
        self.save_region_names_lang(file_path, "fr")
        self.save_region_names_lang(file_path, "en")

    def save_region_names_lang(self, file_path: str, lang: str):

        if lang != "":
            file_name = os.path.splitext(file_path)[0]
            extension = os.path.splitext(file_path)[1]
            file_path = f"{file_name}-{lang}{extension}"

        with open(file_path, "w") as output_file:

            output_file.write("<?xml version='1.0' encoding='utf-8'?>\n")

            output_file.write("<resources>\n")
            for type in PokedexType:
                if lang == "":
                    output_file.write(f"    <string name=\"region_{type.value}\"/>\n")
                else:
                    pokedex_data = Data.POKEDEXES[type]
                    if lang == "fr":
                        name = pokedex_data.name_fr
                    else:
                        name = pokedex_data.name_en
                    # name = json.dumps(name) # escapes quotes

                    output_file.write(f"    <string name=\"region_{type.value}\">{name}</string>\n")

            output_file.write("</resources>")