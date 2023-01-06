#!/usr/bin/env python3

import re
import parsel
import pokebase
# import pokepy # produces "AttributeError: module 'collections' has no attribute 'MutableMapping'"

from utils import utils
# import requests
# import concurrent.futures
# import time
# import os

from data.pokedex import Pokedex
from data.data_enums import Region, PokemonType

# POKEBASE_PICKLE_CACHE_FOLDER = "./output/cache/pokebase_pickle/"

class DataParser:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose

    def process_pokemon_pages(self):

        # with concurrent.futures.ThreadPoolExecutor() as executor:
        #
        #     futures = []
        #
        #     pokemon_ids = self._pokedex.get_pokemons_ids()
        #     size = 100
        #     splitted_ids = [pokemon_ids[i:i + size] for i in range(0, len(pokemon_ids), size)]
        #
        #     for split in splitted_ids:
        #         # if int(unique_id) % 100 == 0:
        #         # print(f"Processing data of pokemon {unique_id}")
        #
        #         futures.append(executor.submit(self.process_pokemon_pages_list, ids=split))
        #
        #     concurrent.futures.wait(futures, return_when=concurrent.futures.ALL_COMPLETED)  # ALL_COMPLETED is actually the
            # for future in concurrent.futures.as_completed(futures):
                # print(future.result())
                # pass

        # pokemons = ["238", "260", "483", "484", "487", "492", "513", "617", "619", "626", "641", "642", "645", "710",
        #             "711", "718", "720", "745", "746", "774", "888", "889"]
        for unique_id in self._pokedex.get_pokemons_ids():

            if int(unique_id)%100 == 0:
                print(f"Processing data of pokemon {unique_id}")

            self.process_pokemon_page(unique_id)

    def process_pokemon_pages_list(self, ids: list):
        for id in ids:
            self.process_pokemon_page(id)

    def process_pokemon_page(self, unique_id: str):
        # print(f"Processing data of pokemon {unique_id}")

        # Fetch pokepedia page
        base_url = "https://www.pokepedia.fr/Pok%C3%A9mon_n%C2%B0"

        full_url = base_url + unique_id
        html = utils.download_page(full_url)
        if not html:
            print(f"error while downloading page '{full_url}'")
            return

        # Get pokeapi data
        # pokeapi_url = "https://pokeapi.co/api/v2/pokemon/" + str(int(unique_id))
        # json_string = utils.download_page(pokeapi_url)
        # if not json_string:
        #     print(f"error while downloading page '{pokeapi_url}'")
        #     return
        # pokeapi_data = json.loads(json_string)
        pokeapi_data = None

        # pokebase_data = self.get_pokebase_data(unique_id) # too damn long
        # pokepy_client = pokepy.V2Client() # error, see import
        # pokepy_data = pokepy_client.get_pokemon(1)

        # Keep only fiche table via regex first, before using parsel selector
        match_table = re.search(r"<table class=[\"|'].* ficheinfo", html)
        if not match_table:
            raise Exception("error while parsing pokemon card/fiche table")
        start, end = match_table.span()
        html = html[start:]
        opened_table_tags = 1
        start = len("<table")
        while True:
            match = re.search(r"</?table>?", html[start:])
            if not match:
                break

            newstart, newend = match.span()
            tag = html[start+newstart:start+newend]
            start += newstart+len(tag)
            opened_table_tags += 1 if tag == "<table" else -1

            if opened_table_tags == 0:
                break

        html = html[:start]

        # Get card/fiche info
        xpath_selector = parsel.Selector(html)
        results = xpath_selector.xpath("//table[contains(@class, 'ficheinfo')]").getall()
        if len(results) != 1:
            raise Exception("error while parsing pokemon card/fiche info")

        self.process_pokemon_ids(unique_id, results[0], pokeapi_data)
        self.process_pokemon_data(unique_id, results[0], pokeapi_data)

    def process_pokemon_ids(self, unique_id: str, html_table: str, pokeapi_data: dict):
        xpath_selector = parsel.Selector(html_table)
        ids_table = xpath_selector.xpath("//table//table[1]").get()

        xpath_selector = parsel.Selector(ids_table)
        region_ids = xpath_selector.xpath("//td[number(text()) = text()]/text()").getall()
        region_names = xpath_selector.xpath("//strong/a/text()").getall() #|//strong/sup/a/span/text()
        regions_and_variants = xpath_selector.xpath("//strong//text()").getall()
        i = 0
        while i < len(regions_and_variants):
            if i != 0 and (i >= len(region_names) or region_names[i] != regions_and_variants[i]):
                if region_names[i - 1] == regions_and_variants[i - 1]:
                    regions_and_variants[i - 1] += " "
                regions_and_variants[i - 1] += regions_and_variants[i]
                del regions_and_variants[i]
            else:
                i += 1

        if len(regions_and_variants) != len(region_ids):
            raise Exception("error while parsing region links")

        regions_links = xpath_selector.xpath("//strong/a/@href").getall()
        if len(regions_links) != len(region_ids):
            raise Exception("error while parsing region links")

        # variants = re.split("|".join(regions_names), "".join(regions_and_variants))
        # if len(regions_names) != len(variants):
        #     raise Exception("error while parsing pokedex regions")

        # regions = [regions_names[i] + "_" + variants[i] for i in range(len(regions_names)) if variants[i]]

        ids = list(tuple())
        for i in range(len(regions_and_variants)):

            region = regions_and_variants[i]

            pokedex_type = None
            for type in Region:
                if region in Region(type).abbrev_names:
                    pokedex_type = type
                    break

            if pokedex_type == None:
                if self._verbose:
                    print("unknown region = " + region)
                continue

            # ids.append( { "type": Region(pokedex_type).name, "id": region_ids[i] })
            ids.append( (pokedex_type, region_ids[i]) )

        self._pokedex.add_pokemon_ids(unique_id, ids)

    def process_pokemon_data(self, unique_id: str, html_table: str, pokeapi_data: dict):

        # Replace all img by their alt texts
        table = utils.replace_imgs_by_alt_texts(html_table)
        table_2d: list[list[str]] = utils.table_to_2d(table)
        for i in range(len(table_2d)):
                row = table_2d[i]
                if not row:
                    continue
                def uniqify_list(seq):
                    seen = set()
                    seen_add = seen.add
                    return [x for x in seq if not (x in seen or seen_add(x))]

                if row[0] == "Type" or row[0] == "Types":
                    row = uniqify_list(row)

                    pokemon_types = []
                    for j in range(1, len(row)):
                        cell = row[j]

                        # Split by "-"
                        types = []
                        if " - " in cell:
                            types += cell.split(" - ")
                        else:
                            types.append(cell)

                        for type in types:
                            # Remove everything between parenthesis
                            type = re.sub(r"\(.*?\)", "", type)

                            match = re.search(r"Miniature_Type_(.*).png", type)
                            if match:
                                type = match.group(1).lower()
                            else:
                                print(f"pokemon {unique_id}, invalid type = {len(type)}")
                                continue

                            # TOOD convert type to PokemonType
                            pokemon_type = None
                            for pktype in PokemonType:
                                if PokemonType(pktype).name_fr == type:
                                    pokedex_type = PokemonType(pktype)

                            if pokedex_type == None:
                                if self.verbose:
                                    print("unknown pokemon type = " + type)
                                continue

                            pokemon_types.append(pokedex_type)

                    self._pokedex.get_pokemon(unique_id).set_types(pokemon_types)

                elif row[0].startswith("Nom japonais"):
                    row = uniqify_list(row)
                    if len(row) != 2:
                        print(f"pokemon {unique_id}, invalid japanese name row length = {len(row)}")
                        continue
                elif row[0].startswith("Nom anglais"):
                    row = uniqify_list(row)
                    if len(row) != 2:
                        print(f"pokemon {unique_id}, invalid english name row length = {len(row)}")
                        continue
                elif  row[0].startswith("Taille"):
                    row = uniqify_list(row)
                    if row[0] == "Tailles" and (i + 1) < len(table_2d):
                        row += uniqify_list(table_2d[i + 1])

                    if len(row) <= 1:
                        print(f"pokemon {unique_id}, invalid height row length = {len(row)}")
                        continue

                    for j in range(1,len(row)):
                        height = float(self.process_pokemon_height(unique_id, row[j]))
                        # if height != pokebase_data.height:
                        #     print(f"pokemon {unique_id}, {j-1} parsed height = {height} != pokebase height ="
                        #           f" {pokebase_data.height}")
                        # if height != pokeapi_data["height"]:
                        #     print(f"pokemon {unique_id}, {j-1} parsed height = {height} != pokeapi json height ="
                        #           f" {pokeapi_data['height']}")

                        # if not height.is_integer():
                        #     print(f"pokemon {unique_id}, height not an integer = {height}")

                        self._pokedex.get_pokemon(unique_id).set_height(height)

                elif row[0].startswith("Poids"):
                    row = uniqify_list(row)
                    if len(row) == 1 and (i + 1) < len(table_2d):
                        row += uniqify_list(table_2d[i + 1])

                    if len(row) <= 1:
                        print(f"pokemon {unique_id}, invalid weight row length = {len(row)}")
                        continue

                    for j in range(1, len(row)):
                        weight = float(self.process_pokemon_weight(unique_id, row[j]))
                        # if weight != pokebase_data.weight:
                        #     print(f"pokemon {unique_id}, {j-1} parsed weight = {weight} != pokebase weight ="
                        #           f" {pokebase_data.weight}")
                        # if weight != pokeapi_data["weight"]:
                        #     print(f"pokemon {unique_id}, {j-1} parsed weight = {weight} != pokeapi json weight ="
                        #           f" {pokeapi_data['weight']}")

                        # if not weight.is_integer():
                        #     print(f"pokemon {unique_id}, weight not an integer = {weight}")

                        self._pokedex.get_pokemon(unique_id).set_weight(weight)

                # ['Nom japonais', 'フシギダネ Fushigidane']
                # ['Nom anglais', 'Bulbasaur']
                # Descriptions du Pokédex
                # Talents
    def process_pokemon_height(self, unique_id: str, cell: list) -> float:
        return self.process_pokemon_float(unique_id, cell, "m")

    def process_pokemon_weight(self, unique_id: str, cell: list) -> float:
        return self.process_pokemon_float(unique_id, cell, "kg")

    def process_pokemon_float(self, unique_id: str, cell: list, unit: str) -> float:
        cell = cell.replace(",", ".")

        if match := re.search(f"(\d+\.\d+) " + unit, cell):
            value = match.group(1)
            try:
                value = float(value)
                return value
            except ValueError as e:
                print(f"pokemon {unique_id}, could not parse float {value}'")
        else:
            print(f"pokemon {unique_id}, could not parse float {cell}'")
            return 0.0
    def get_pokebase_data(self, unique_id: str):

        # full_path = POKEBASE_PICKLE_CACHE_FOLDER + unique_id
        #
        # if os.path.exists(full_path):
        #     with open(full_path, "rb") as file:
        #         try:
        #             return pickle.load(file)
        #         except OSError as e:
        #             print(f"error '{e}' while reading pokebase file {full_path}")
        #             # return None # just keep going and call the api then

        pokebase_data = pokebase.pokemon(int(unique_id))

        # try:
        #     dir_path = os.path.dirname(full_path)
        #     os.makedirs(dir_path, exist_ok=True)
        #
        #     with open(full_path, "wb") as outfile:
        #         pickle.dump(pokebase_data, outfile)
        # except OSError as e:
        #     print(f"error '{e}' while write pokebase file {full_path}")
        #     # return None # just keep going and return the file

        return pokebase_data