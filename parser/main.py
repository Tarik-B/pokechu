#!/usr/bin/env python3

import time
import pokebase.cache

from utils import utils

from data.data_enums import Region
from parsing.parser_conditions import ConditionsParser
from parsing.parser_data import DataParser
from export.exporter import Exporter
from data.pokedex import Pokedex
from parsing.parser_list import ListParser
from parsing.parser_evolutions import EvolutionsParser
from export.exporter_sqlite import SQLiteExporter

OUTPUT_FOLDER = "../parser_output"

if __name__ == "__main__":
    # TODO use this syntax EVERYWHERE
    # test = 0
    # print(f"{test=}")
    # for c in Color:
    #     print(f"{c.name}={c.value}, hex_code={c.hex_code}, description={c.description}")
    #from pyparsing import nestedExpr
    # import shlex
    #test = "(2(1(1(7)(10))(4))(1(4)(6'item_7')))"
    #result1 = nestedExpr('(', ')').parseString(test).asList()
    # print(f"result pyparsing = {result1}")

    # print(f"Region.NATIONAL = {Region.NATIONAL.value}")
    # print(f"Region.PALDEA = {Region.PALDEA.value}")

    utils.PAGES_CACHE_FOLDER = OUTPUT_FOLDER + "/cache"
    pokebase.cache.set_cache(utils.PAGES_CACHE_FOLDER + "/pokebase")

    total_start_time = time.time()

    pokedex = Pokedex(Region.NATIONAL)
    pokedex.add_pokemon_entry("000", {"fr": "MissingNo.", "en": "MissingNo."})
    pokedex.add_pokemon_ids("000", [(Region.KANTO, "000")])

    ##################################################
    block_start_time = time.time()
    print("+ Processing Pokemon list")
    list_parser = ListParser(pokedex=pokedex, verbose=True)
    list_parser.process_pokedex_list_page(download_thumbnails=False, output_path=OUTPUT_FOLDER + "/images/pokemons")
    print(f"Pokemon count = {pokedex.get_pokemons_count()}")
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    ##################################################
    block_start_time = time.time()
    print("+ Processing Pokemon data pages")
    data_parser = DataParser(pokedex=pokedex, verbose=True)
    data_parser.process_pokemon_pages()
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    ##################################################
    block_start_time = time.time()
    print(f"+ Processing evolutions")
    evolutions_parser = EvolutionsParser(pokedex=pokedex, verbose=True)
    evolutions_parser.process_evolution_list_page(lang="fr")
    print(f"Evolution trees count = {pokedex.get_evolution_trees_count()}")
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    ##################################################
    block_start_time = time.time()
    print(f"+ Processing evolution conditions")
    conditions_parser = ConditionsParser(pokedex=pokedex, verbose=True)
    conditions_parser.process_evolution_trees_conditions()
    print(f"Processed conditions count = {conditions_parser._processed_condition_count}")
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    ##################################################
    block_start_time = time.time()
    print(f"+ Saving files and db")
    exporter = Exporter(pokedex=pokedex, verbose=True)
    exporter.save_all(OUTPUT_FOLDER)
    sqlite_exporter = SQLiteExporter(pokedex=pokedex, verbose=True)
    sqlite_exporter.save_all("../db", OUTPUT_FOLDER + "/db.sqlite")
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    print(f"+ Total time = {time.time() - total_start_time}s")
