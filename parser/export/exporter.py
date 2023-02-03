#!/usr/bin/env python3

import os

from utils import utils
from data.data_enums import Region, EvolutionItem, PokemonType, EvolutionCondition, Game

from data.pokedex import Pokedex

class Exporter:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose

    def save_all(self, output_path: str):
        # self._pokedex.save_pokemon_list(path + "pokemon_list.json")
        # self._pokedex.save_evolution_trees(path + "pokemon_evolution_trees.json")

        self._save_pokemon_names_xml(output_path, "pokemons.xml")

        self._save_enum_names_xml(output_path, "regions.xml", Region, "region_name")
        self._save_enum_names_xml(output_path, "pokemon_types.xml", PokemonType, "pokemon_type_name")
        self._save_enum_names_xml(output_path, "evolution_items.xml", EvolutionItem, "evolution_item_name")
        self._save_enum_names_xml(output_path, "evolution_conditions.xml", EvolutionCondition, "evolution_condition_name")
        self._save_enum_names_xml(output_path, "evolution_conditions.xml", EvolutionCondition, "evolution_condition_name")
        self._save_enum_names_xml(output_path, "games.xml", Game, "game_name")

        self._save_enums_kotlin(output_path, [Region, PokemonType, EvolutionItem, EvolutionCondition, Game])

        self._save_descriptions(output_path, "descriptions.xml", "pokemon_description")

    def _save_descriptions(self, folder_path: str, file_name: str, prefix: str):

        all_langs = ["fr", "en"]
        self._save_descriptions_lang(folder_path + "/values/" + file_name, "", prefix, all_langs)

        self._save_descriptions_lang(folder_path + "/values-fr/" + file_name, "fr", prefix, all_langs)
        self._save_descriptions_lang(folder_path + "/values-en/" + file_name, "en", prefix, all_langs)

    def _save_descriptions_lang(self, file_path: str, lang: str, prefix: str, all_langs: list):

        with open(file_path, "w") as output_file:
            output_file.write("<?xml version='1.0' encoding='utf-8'?>\n")
            output_file.write("\n")
            output_file.write(f"{utils.get_generated_warning_xml()}\n")
            output_file.write("\n")

            output_file.write("<resources>\n")

            for id in self._pokedex.get_pokemons_ids():
                int_id = int(id)

                descriptions = self._pokedex.get_pokemon(id).get_descriptions()
                if not descriptions:
                    continue

                # Must have descriptions in all langs
                if len(descriptions) != 2:
                    print(f"pokemon {id} has descriptions in {len(descriptions)} langs, not 2")
                    continue
                all_found = True
                for all_lang in all_langs:
                    if all_lang not in descriptions:
                        print(f"pokemon {id} has no '{all_lang}' descriptions")
                        all_found = False
                if not all_found:
                    continue

                versions = descriptions[lang] if (lang != "") else descriptions["fr"]
                for version in versions:

                    # Keep only versions that are in all languages
                    all_found = [ (version in descriptions[all_lang]) for all_lang in all_langs ]
                    if not all(found for found in all_found):
                        continue

                    if lang == "":
                        output_file.write(f"    <string name=\"{prefix}_{int_id}_{version.name}\"/>\n")
                    else:
                        description = versions[version]
                        description = description.replace("'", "\\'")
                        description = description.replace("\n", " ")

                        output_file.write(f"    <string name=\"{prefix}_{int_id}_{version.name}\">{description}</string>\n")

            output_file.write("</resources>")

    def _save_enums_kotlin(self, folder_path: str, enum_class_list: list):

        for enum_class in enum_class_list:
            class_name = enum_class.__name__

            full_path = f"{folder_path}/{class_name}.kt"

            with open(full_path, "w") as output_file:
                output_file.write("package fr.amazer.pokechu.enums\n")
                output_file.write("\n")
                output_file.write(f"{utils.get_generated_warning_kotlin()}\n")
                output_file.write("\n")

                output_file.write(f"enum class {class_name} " + "{\n")
                enum_value_column_width = max([len(enum.name) for enum in enum_class])+1
                for enum in enum_class:
                    output_file.write(f"\t{enum.name+',':<{enum_value_column_width}} // {enum.value}\n")
                output_file.write("}")

    def _save_pokemon_names_xml(self, folder_path: str, file_name: str):
        if len(self._pokedex.get_pokemon_names_keys()) == 0:
            return

        # Save strings.xml ids declaration
        self._save_pokemon_names_xml_lang(folder_path + "/values/" + file_name, "")

        # Save strings-XXX.xml with localized names
        self._save_pokemon_names_xml_lang(folder_path + "/values-fr/" + file_name, "fr")
        self._save_pokemon_names_xml_lang(folder_path + "/values-en/" + file_name, "en")

    def _save_pokemon_names_xml_lang(self, file_path: str, lang: str):

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


    def _save_enum_names_xml(self, folder_path: str, file_name: str, enum_class, prefix: str):
        # Save strings.xml ids declaration
        self._save_enum_names_xml_lang(folder_path + "/values/" + file_name, "", enum_class, prefix)

        # Save strings-XXX.xml with localized names
        self._save_enum_names_xml_lang(folder_path + "/values-fr/" + file_name, "fr", enum_class, prefix)
        self._save_enum_names_xml_lang(folder_path + "/values-en/" + file_name, "en", enum_class, prefix)

    def _save_enum_names_xml_lang(self, file_path: str, lang: str, enum_class, prefix: str):

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