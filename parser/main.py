#!/usr/bin/env python3
from conditions_parser import ConditionsParser
from data_parser import DataParser
from pokedex import PokedexType, Pokedex
from list_parser import ListParser
from evolutions_parser import EvolutionsParser

import time

if __name__ == "__main__":
    total_start_time = time.time()

    pokedex = Pokedex(PokedexType.NATIONAL)

    ##################################################
    block_start_time = time.time()
    list_parser = ListParser(pokedex=pokedex, verbose=False)
    print("+ Processing Pokemon list")
    list_parser.process_pokedex_list_page(download_thumbnails=False)
    print(f"Pokemon count = {len(pokedex.pokemons)}")
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    ##################################################
    block_start_time = time.time()
    data_parser = DataParser(pokedex=pokedex, verbose=False)
    print("+ Processing Pokemon data pages")
    data_parser.process_pokemon_pages()
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    # pokedex.save_pokemon_list_json("./output/pokemon_list.json")
    pokedex.save_pokemon_list_sqlite("../db/db_new.sqlite")
    # pokedex.save_pokemon_list_csv("./output/pokemon_list.csv")
    pokedex.save_pokemon_names("./output/pokemons.xml")

    ##################################################
    block_start_time = time.time()
    evolutions_parser = EvolutionsParser(pokedex=pokedex, verbose=False)
    print(f"+ Processing evolutions")
    evolutions_parser.process_evolution_list_page(lang="fr")
    print(f"Evolution trees count = {len(pokedex.evolution_trees)}")
    print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    # pokedex.clear_evolution_trees()
    # print(f"+ Processing evolution list page (en)")
    # parser.process_evolution_list_page("en")
    # pokedex.save_evolution_trees("./output/pokemon_evolution_trees_en.json")

    ##################################################
    # block_start_time = time.time()
    # conditions_parser = ConditionsParser(pokedex=pokedex, verbose=False)
    # print(f"+ Processing evolution conditions")
    # conditions_parser.process_evolution_trees_conditions()
    # print(f"Processed conditions count = {conditions_parser.processed_condition_count}")
    # print(f"Time = {time.time() - block_start_time}s")
    ##################################################

    pokedex.save_evolution_trees("./output/pokemon_evolution_trees.json")

    print(f"+ Total time = {time.time() - total_start_time}s")
