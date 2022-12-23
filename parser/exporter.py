#!/usr/bin/env python3
import json
import os

import utils
from data import PokedexType, ItemType, PokemonType, EvolutionConditionType

from pokedex import Pokedex

class Exporter:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose

    def save_all(self, path: str):
        # self._pokedex.save_pokemon_list(path + "pokemon_list.json")
        # self._pokedex.save_evolution_trees(path + "pokemon_evolution_trees.json")

        self.save_pokemon_names_xml(path, "pokemons.xml")

        self.save_enum_names_xml(path, "regions.xml", PokedexType, "region_name")
        self.save_enum_names_xml(path, "types.xml", PokemonType, "type_name")
        self.save_enum_names_xml(path, "items.xml", ItemType, "item_name")
        self.save_enum_names_xml(path, "conditions.xml", EvolutionConditionType, "condition_name")

        self.save_enums_kotlin(path, [PokedexType, PokemonType, ItemType, EvolutionConditionType])

    def save_enums_kotlin(self, file_path: str, enum_class_list: list):

        for enum_class in enum_class_list:
            class_name = enum_class.__name__

            full_path = f"{file_path}{class_name}.kt"

            with open(full_path, "w") as output_file:
                output_file.write("package fr.amazer.pokechu.data\n")
                output_file.write("\n")
                output_file.write(f"{utils.get_generated_warning_kotlin()}\n")
                output_file.write("\n")

                output_file.write(f"enum class {class_name} " + "{\n")
                enum_value_column_width = max([len(enum.name) for enum in enum_class])+1
                for enum in enum_class:
                    output_file.write(f"\t{enum.name+',':<{enum_value_column_width}} // {enum.value}\n")
                output_file.write("}")

    def save_pokemon_names_xml(self, folder_path: str, file_name: str):
        if len(self._pokedex.get_pokemon_names_keys()) == 0:
            return

        # Save strings.xml ids declaration
        self.save_pokemon_names_xml_lang(folder_path + "values/" + file_name, "")

        # Save strings-XXX.xml with localized names
        self.save_pokemon_names_xml_lang(folder_path + "values-fr/" + file_name, "fr")
        self.save_pokemon_names_xml_lang(folder_path + "values-en/" + file_name, "en")

    def save_pokemon_names_xml_lang(self, file_path: str, lang: str):

        dir_path = os.path.dirname(file_path)
        os.makedirs(dir_path, exist_ok=True)

        with open(file_path, "w") as output_file:
            output_file.write("<?xml version='1.0' encoding='utf-8'?>\n")
            output_file.write("\n")
            output_file.write(f"{utils.get_generated_warning_xml()}\n")
            output_file.write("\n")

            output_file.write("<resources>\n")
            for id in self._pokedex.get_pokemon_names_keys():
                int_id = int(id)
                if lang == "":
                    output_file.write(f"    <string name=\"pokemon_name_{int_id}\"/>\n")
                else:
                    names = self._pokedex.get_pokemon_names(id)
                    if lang in names:
                        name = names[lang].replace("'", "\\'")# json.dumps(names[lang]) # escapes quotes
                        output_file.write(f"    <string name=\"pokemon_name_{int_id}\">{name}</string>\n")

            output_file.write("</resources>")


    def save_enum_names_xml(self, folder_path: str, file_name: str, enum_class, prefix: str):
        # Save strings.xml ids declaration
        self.save_enum_names_xml_lang(folder_path + "/values/" + file_name, "", enum_class, prefix)

        # Save strings-XXX.xml with localized names
        self.save_enum_names_xml_lang(folder_path + "/values-fr/" + file_name, "fr", enum_class, prefix)
        self.save_enum_names_xml_lang(folder_path + "/values-en/" + file_name, "en", enum_class, prefix)

    def save_enum_names_xml_lang(self, file_path: str, lang: str, enum_class, prefix: str):

        with open(file_path, "w") as output_file:
            output_file.write("<?xml version='1.0' encoding='utf-8'?>\n")
            output_file.write("\n")
            output_file.write(f"{utils.get_generated_warning_xml()}\n")
            output_file.write("\n")

            output_file.write("<resources>\n")
            for enum in enum_class:
                if lang == "":
                    output_file.write(f"    <string name=\"{prefix}_{enum.value}\"/>\n")
                else:
                    if lang == "fr":
                        name = enum_class(enum).name_fr
                    else:
                        name = enum_class(enum).name_en
                    name = name.replace("'", "\\'")# json.dumps(names[lang]) # escapes quotes

                    output_file.write(f"    <string name=\"{prefix}_{enum.value}\">{name}</string>\n")

            output_file.write("</resources>")