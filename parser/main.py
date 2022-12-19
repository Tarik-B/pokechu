#!/usr/bin/env python3

import time
import pokebase.cache

import utils

from parser_conditions import ConditionsParser
from parser_data import DataParser
from exporter import Exporter
from parser_misc import MiscParser
from pokedex import PokedexType, Pokedex
from parser_list import ListParser
from parser_evolutions import EvolutionsParser
from exporter_sqlite import SQLiteExporter


if __name__ == "__main__":
    # TODO use this syntax EVERYWHERE
    test = 0
    print(f"{test=}")

    # for c in Color:
    #     print(f"{c.name}={c.value}, hex_code={c.hex_code}, description={c.description}")

    from pyparsing import nestedExpr
    import shlex


    test = "(2(1(1(7)(10))(4))(1(4)(6'item_7')))"
    result1 = nestedExpr('(', ')').parseString(test).asList()
    print(f"result pyparsing = {result1}")
    print(f"PokedexType.NATIONAL = {PokedexType.NATIONAL.value}")
    print(f"PokedexType.PALDEA = {PokedexType.PALDEA.value}")

    pokebase.cache.set_cache(utils.PAGES_CACHE_FOLDER + "/pokebase")

    total_start_time = time.time()

    pokedex = Pokedex(PokedexType.NATIONAL)

    ##################################################
    # block_start_time = time.time()
    # print(f"+ Processing misc data")
    # misc_parser = MiscParser(pokedex=pokedex, verbose=True)
    # misc_parser.process_type_pages()
    # print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    ##################################################
    block_start_time = time.time()
    print("+ Processing Pokemon list")
    list_parser = ListParser(pokedex=pokedex, verbose=True)
    list_parser.process_pokedex_list_page(download_thumbnails=False)
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
    exporter.save_all("./output/")
    sqlite_exporter = SQLiteExporter(pokedex=pokedex, verbose=True)
    sqlite_exporter.save_all("./output/db.sqlite")
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    print(f"+ Total time = {time.time() - total_start_time}s")
