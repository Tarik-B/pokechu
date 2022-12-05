#!/usr/bin/env python3

from pokedex import PokedexType, Pokedex
from list_parser import ListParser
from evolutions_parser import EvolutionsParser

if __name__ == "__main__":
    pokedex = Pokedex(PokedexType.DEX_PALDEA_EV)

    parser = ListParser(pokedex)
    parser.process_pokedex_list_page(False)
    pokedex.save_pokemon_list("./output/pokemon_list.json")

    pokedex.save_pokemon_names("./output/strings.xml")

    parser = EvolutionsParser(pokedex)
    parser.process_evolution_list_page()
    pokedex.save_evolution_trees("./output/pokemon_evolution_trees.json")