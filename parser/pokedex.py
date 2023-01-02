#!/usr/bin/env python3

import collections

from data import Region


class Pokemon:
    def __init__(self, unique_id: int, names: dict, thumbnail_filename: str):
        self._unique_id = unique_id
        self._names = names # dict {'fr': string, 'en': string}
        self._thumbnail_filename = thumbnail_filename
        self._ids = list() # of dict {'type': Region, 'id': int}
        self._types = list() # of PokemonType
        self._height = -1.0 # in cm
        self._weight = -1.0 # in kg

    def get_name(self, lang: str): return self._names[lang] if lang in self._names else None
    def get_ids(self): return self._ids
    def set_ids(self, ids: list): self._ids = ids
    def get_types(self): return self._types
    def set_types(self, types: list): self._types = types
    def get_height(self): return self._height
    def set_height(self, height: float): self._height = height
    def get_weight(self): return self._weight
    def set_weight(self, weight: float): self._weight = weight
    # def set_data(self, key: str, value):
    #     if key in self._data:
    #         print(f"warning pokemon {self._unique_id}, data key '{key}' replacing data = '{self._data[key]}', by = '{value}'")
    #     self._data[key] = value


class Pokedex:
    def __init__(self, region: Region):
        self._region = region
        self._pokemons = dict()
        self._evolution_trees = list()
        self._pokemon_names = dict()
        # Used only to speedup id lookups
        self._names_to_unique_ids = dict()

        self._names_to_unique_ids["fr"] = dict()
        self._names_to_unique_ids["en"] = dict()

    # Public functions
    def get_region(self) -> Region: return self._region
    def get_pokemons_count(self) -> int: return len(self._pokemons)
    def get_pokemons_ids(self) -> list: return list(self._pokemons.keys())
    def get_pokemon(self, id: str) -> Pokemon: return self._pokemons[id]
    def get_pokemons(self) -> dict: return self._pokemons
    def get_evolution_trees_count(self) -> int: return len(self._evolution_trees)
    def get_evolution_tree(self, index: int): return self._evolution_trees[index] if index < len( self._evolution_trees) else None
    def get_pokemon_names_keys(self) -> list: return self._pokemon_names.keys()
    def get_pokemon_names(self, key: str) -> dict: return self._pokemon_names[key] if key in self._pokemon_names else None

    def add_pokemon_entry(self, unique_id: str, names: dict, thumbnail_filename: str):
        self._pokemons[unique_id] = Pokemon(int(unique_id), names, thumbnail_filename)

        self._pokemon_names[unique_id] = names

        # Build name -> id map for fast lookups
        for lang in names:
            name = names[lang]
            if lang in self._names_to_unique_ids:
                self._names_to_unique_ids[lang][name] = unique_id

    def has_pokemon_entry(self, name: str, lang: str) -> bool:
        return self._convert_name_to_unique_id(name, lang) is not None

    def add_pokemon_ids(self, unique_id: str, ids: list):
        if unique_id not in self._pokemons:
            return

        # ids is a list of tuple(Region,str)
        id_list = []
        for id_tuple in ids:
            type = id_tuple[0]
            id = id_tuple[1]

            id_list.append({"type": Region(type), "id": int(id)})

        self._pokemons[unique_id].set_ids(id_list)

    def add_evolution_node(self, parent: dict, name: str, condition: str, lang: str) -> dict:
        unique_id = self._convert_name_to_unique_id(name, lang)
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
            self._evolution_trees.append(new_node)

        # Return a reference to newly created node
        return new_node

    def is_pokemon_evolution_node(self, node: dict, name: str, lang: str) -> bool:
        unique_id = self._convert_name_to_unique_id(name, lang)
        if unique_id is None:
            return False

        return node["id"] == unique_id

    def find_in_evolution_trees(self, name: str, lang: str):
        for tree in self._evolution_trees:
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
        self._evolution_trees = [tree for tree in self._evolution_trees
                                 if ("evolutions" in tree and len(tree["evolutions"]) != 0)]

    def _convert_name_to_unique_id(self, name: str, lang: str) -> str:
        if lang not in self._names_to_unique_ids:
            return None

        if name not in self._names_to_unique_ids[lang]:
            return None

        return self._names_to_unique_ids[lang][name]

    def _sort_evolution_trees_keys_in_order(self, dictionary: dict, key_order: list) -> collections.OrderedDict:

        ordered_dictionary = collections.OrderedDict(dictionary)
        for key in key_order:
            if key in ordered_dictionary:
                ordered_dictionary.move_to_end(key)

        if "evolutions" in ordered_dictionary:
            evolutions = ordered_dictionary["evolutions"]
            for i in range(len(evolutions)):
                evolution = evolutions[i]
                evolutions[i] = self._sort_evolution_trees_keys_in_order(evolution, key_order)

        return ordered_dictionary
