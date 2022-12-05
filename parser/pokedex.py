#!/usr/bin/env python3

import json
import os
import re
from enum import Enum, auto

class PokedexType(Enum):
    DEX_PALDEA_EV = auto()
    NDEX = auto()


class EvolutionType(Enum):
    NAME = auto()
    LEVEL = auto()
    OTHER = auto()

class VariantType(Enum):
    GIGAMAX = auto()
    HISUI = auto()
    GALAR = auto()
    ALOLA = auto()
    PALDEA = auto()

class Pokedex:
    def __init__(self, type: PokedexType):
        self.type = type
        self.pokemons = list()
        self.evolution_trees = list()
        self.pokemon_names = dict()

        self.pokemon_names["fr"] = dict()
        self.pokemon_names["en"] = dict()

    def add_pokemon_entry(self, unique_id: str, paldea_id: str, name_fr: str, name_en: str, thumbnail_filename: str):
        pokemon = {}

        ids = {"unique": unique_id, "paldea": paldea_id}
        pokemon["ids"] = ids

        images = {"thumbnail": thumbnail_filename}
        pokemon["images"] = images

        self.pokemons.append(pokemon)

        self.pokemon_names["fr"][unique_id] = name_fr
        self.pokemon_names["en"][unique_id] = name_en

    def has_pokemon_entry(self, name_fr: str) -> bool:
        return self.convert_fr_name_to_unique_id(name_fr) is not None

    def convert_fr_name_to_unique_id(self, name_fr: str) -> str:
        for id in self.pokemon_names["fr"]:
            if self.pokemon_names["fr"][id] == name_fr:
                return id

        return None

    def add_evolution_node(self, parent: dict, name: str, condition: str) -> dict:
        unique_id = self.convert_fr_name_to_unique_id(name)

        if condition:
            condition_strings = [s.strip() for s in condition.split("+")]
            for condition_string in condition_strings:
                if condition_string == "Bonheur":
                    condition_type = "FRIENDSHIP"
                elif re.match( r"Niveau [0-9]+", condition_string):
                    condition_type = "LEVEL"
                    condition_data = re.sub("Niveau ", "", condition_string)
                else:
                    print(f"unknown evolution condition '{condition_string}'")

        new_node = {"id": unique_id, "condition": condition, "evolutions": []}
        if parent:
            parent["evolutions"].append(new_node)
        else:
            self.evolution_trees.append(new_node)

        # Return a reference to newly created node
        return new_node

    def is_pokemon_evolution_node(self, node: dict, name_fr: str) -> dict:
        unique_id = self.convert_fr_name_to_unique_id(name_fr)
        return node["id"] == unique_id

    def find_in_evolution_trees(self, name_fr: str):
        for tree in self.evolution_trees:
            node = self.find_in_evolution_tree(tree, name_fr)
            if node:
                return tree, node

        return None, None

    def find_in_evolution_tree(self, node: dict, name_fr: str) -> dict:

        if self.is_pokemon_evolution_node(node, name_fr):
            return node

        for child in node["evolutions"]:
            found = self.find_in_evolution_tree(child, name_fr)
            if found:
                return found

        return None

    def remove_single_node_evolution_trees(self):
        self.evolution_trees = [tree for tree in self.evolution_trees if len(tree["evolutions"]) != 0]

    def print_trees(self):
        for tree in self.evolution_trees:
            self.print_tree(tree)

    def print_tree(self, node, level=0):

        how = ( " (" + node["condition"] + ")" ) if node["condition"] else ""
        print("    " * (level - 1) + "+---" * (level > 0) + node["id"] + how )

        for child in node["evolutions"]:
            self.print_tree(child, level + 1)

    def save_pokemon_list(self, file_path: str):
        pretty_json = json.dumps(self.pokemons, indent=4, ensure_ascii=False)
        with open(file_path, "w") as outfile:
            # json.dump(parser.pokedex.pokemons, outfile)
            outfile.write(pretty_json)

    def save_evolution_trees(self, file_path: str):
        pretty_json = json.dumps(self.evolution_trees, indent=4, ensure_ascii=False)
        with open(file_path, "w") as outfile:
            outfile.write(pretty_json)

    def save_pokemon_names(self, file_path: str):
        if len(self.pokemon_names) == 0:
            return

        # Save strings.xml ids declaration
        self.save_pokemon_names_lang(file_path, self.pokemon_names["fr"], "")

        # Save strings-XXX.xml with localized names
        for lang in self.pokemon_names:
            self.save_pokemon_names_lang(file_path, self.pokemon_names[lang], lang)

    def save_pokemon_names_lang(self, file_path: str, names: dict, lang: str):

        if lang != "":
            file_name = os.path.splitext(file_path)[0]
            extension = os.path.splitext(file_path)[1]
            file_path = f"{file_name}-{lang}{extension}"

        with open(file_path, "w") as output_file:

            output_file.write("<?xml version='1.0' encoding='utf-8'?>\n")

            output_file.write("<resources>\n")
            for id in names:

                if lang == "":
                    output_file.write(f"    <string name=\"pokemon_name_{id}\"/>\n")
                else:
                    name = names[id]
                    output_file.write(f"    <string name=\"pokemon_name_{id}\">{name}</string>\n")

            output_file.write("</resources>")