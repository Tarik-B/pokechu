#!/usr/bin/env python3
from conditions_parser import ConditionsParser
from pokedex import PokedexType, Pokedex
from list_parser import ListParser
from evolutions_parser import EvolutionsParser

if __name__ == "__main__":
    pokedex = Pokedex(PokedexType.DEX_PALDEA_EV)

    list_parser = ListParser(pokedex)
    print("+ Processing Pokemon list page")
    list_parser.process_pokedex_list_page(False)
    print(f"Pokemon count = {len(pokedex.pokemons)}")

    pokedex.save_pokemon_list("./output/pokemon_list.json")
    pokedex.save_pokemon_names("./output/strings.xml")

    evolutions_parser = EvolutionsParser(pokedex)
    print(f"+ Processing evolution list page (fr)")
    evolutions_parser.process_evolution_list_page("fr")
    print(f"Evolution trees count = {len(pokedex.evolution_trees)}")

    # pokedex.clear_evolution_trees()
    # print(f"+ Processing evolution list page (en)")
    # parser.process_evolution_list_page("en")
    # pokedex.save_evolution_trees("./output/pokemon_evolution_trees_en.json")

    conditions_parser = ConditionsParser(pokedex)
    conditions_parser.process_evolution_trees_conditions()

    pokedex.save_evolution_trees("./output/pokemon_evolution_trees.json")
