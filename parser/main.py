#!/usr/bin/env python3

import json

from pokedex import PokedexType
from pokepedia_parser import PokepediaParser

if __name__ == "__main__":
    # try:
    #     with open("list.txt", "r") as file:
    #         text = file.read()
    # except (IOError, OSError, FileNotFoundError, PermissionError, OSError):
    #     print("Error reading file")

    parser = PokepediaParser(PokedexType.DEX_PALDEA_EV)
    parser.fetch_pokedex_page()
    print("Pokemon list:")
    pretty_json = json.dumps(parser.pokedex.pokemons, indent=4, ensure_ascii=False)
    with open("./output/pokemon_list.json", "w") as outfile:
        # json.dump(parser.pokedex.pokemons, outfile)
        outfile.write(pretty_json)

    print("Evolution trees:")
    pretty_json = json.dumps(parser.pokedex.evolutions, indent=4, ensure_ascii=False)
    with open("./output/pokemon_evolutions.json", "w") as outfile:
        # json.dump(parser.pokedex.evolutions, outfile)
        outfile.write(pretty_json)

    # ordered = collections.OrderedDict(sorted(pokemons.items()))
    # print(json.dumps(ordered, indent=4, ensure_ascii=False))


