#!/usr/bin/env python3

import json
from enum import Enum, auto

class PokedexType(Enum):
    DEX_PALDEA_EV = auto()

class EvolutionType(Enum):
    NAME = auto()
    LEVEL = auto()
    OTHER = auto()


class Pokedex:
    def __init__(self, type: PokedexType):
        self.type = type
        self.pokemons = list()
        self.evolutions = dict()

    def add_pokemon_entry(self, unique_id: str, paldea_id: str, name_fr: str, name_en: str, thumbnail_filename: str):
        pokemon = {}

        ids = {"unique": unique_id, "paldea": paldea_id}
        pokemon["ids"] = ids

        names = {"fr": name_fr, "en": name_en}
        pokemon["names"] = names

        images = {"thumbnail": thumbnail_filename}
        pokemon["images"] = images

        self.pokemons.append(pokemon)

    def add_evolution_tree(self, evolution_table: list):
        pass

    def add_evolutions(evolution_table: list):
        pass

    def save_pokemon_list(self, file_path: str):
        pretty_json = json.dumps(self.pokemons, indent=4, ensure_ascii=False)
        with open(file_path, "w") as outfile:
            # json.dump(parser.pokedex.pokemons, outfile)
            outfile.write(pretty_json)

    def save_evolution_list(self, file_path: str):
        pretty_json = json.dumps(self.evolutions, indent=4, ensure_ascii=False)
        with open(file_path, "w") as outfile:
            # json.dump(parser.pokedex.evolutions, outfile)
            outfile.write(pretty_json)

    def test(self, evolution_table: list):

        for i in range(len(evolution_table)):

            row = evolution_table[i]
            for j in range(len(row)):

                entry = row[j]
                name_fr = entry[0]
                evolution_type: EvolutionType = entry[1]
                pokemon = { id: name_fr }

                match evolution_type:
                    case EvolutionType.LEVEL:
                        pass
                    case EvolutionType.OTHER:
                        pass
                    case _:
                        pass

                names = {"id": "name_fr", "en": "name_en"}
                pokemon["names"] = names

                ids = {"paldea": "paldea_id"}
                pokemon["ids"] = ids

                images = {"thumbnail": "thumbnail_filename"}
                pokemon["images"] = images

        self.evolutions.append(evolution_tree)