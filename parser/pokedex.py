#!/usr/bin/env python3

import json
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

    def add_pokemon_entry(self, unique_id: str, paldea_id: str, name_fr: str, name_en: str, thumbnail_filename: str):
        pokemon = {}

        ids = {"unique": unique_id, "paldea": paldea_id}
        pokemon["ids"] = ids

        names = {"fr": name_fr, "en": name_en}
        pokemon["names"] = names

        images = {"thumbnail": thumbnail_filename}
        pokemon["images"] = images

        self.pokemons.append(pokemon)

    def has_pokemon_entry(self, name_fr: str) -> bool:
        return self.convert_fr_name_to_unique_id(name_fr) is not None

    def convert_fr_name_to_unique_id(self, name_fr: str) -> str:
        for pokemon in self.pokemons:
            if pokemon["names"]["fr"] == name_fr:
                return pokemon["ids"]["unique"]

        return None

    def add_evolution_node(self, parent: dict, name: str, condition: str) -> dict:
        unique_id = self.convert_fr_name_to_unique_id(name)
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