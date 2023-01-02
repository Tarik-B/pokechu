#!/usr/bin/env python3
import sqlite3
import contextlib

import utils
from data import Region, PokemonType
from pokedex import Pokedex

class SQLiteExporter:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose

    def save_all(self, db_file_path: str):
        db_clear = utils.read_file("./db/db_clear.sql")
        db_init = utils.read_file("./db/db_init.sql")
        if not db_clear or not db_init:
            return

        with contextlib.closing(sqlite3.connect(db_file_path)) as connection:
            with contextlib.closing(connection.cursor()) as cursor:
                try:
                    # Reset and reinit db
                    cursor.executescript(db_clear)
                    cursor.executescript(db_init)
                    connection.commit()
                except Exception as err:
                    print("error while inserting into db = " + str(err))

                self.save_pokemons(connection, cursor)
                self.save_regions(connection, cursor)
                self.save_types(connection, cursor)

                self.save_pokemon_regions(connection, cursor)
                self.save_pokemon_evolutions(connection, cursor)
                self.save_pokemon_types(connection, cursor)


    def save_pokemons(self, connection, cursor):
        if self._pokedex.get_pokemons_count() == 0:
            return

        query = "INSERT INTO pokemons (id, name, height, weight) VALUES "
        ids_and_names = [(id,pokemon.get_name("fr"),pokemon.get_height(),pokemon.get_weight())
                         for id,pokemon in self._pokedex.get_pokemons().items()]
        query += ", ".join(f"({id}, '{name}', {height}, {weight})" for id,name,height,weight in ids_and_names)
        query += ";"

        try:
            cursor.execute(query)
            connection.commit()
        except Exception as err:
            print("error while inserting pokemons into db = " + str(err))

    def save_regions(self, connection, cursor):
        query = "INSERT INTO regions (id, name) VALUES "
        regions_and_names = [(region.value, region.name) for region in Region]
        query += ", ".join(f"({region}, '{name}')" for region, name in regions_and_names)
        query += ";"

        try:
            cursor.execute(query)
            connection.commit()
        except Exception as err:
            print("error while inserting regions into db = " + str(err))

    def save_types(self, connection, cursor):
        query = "INSERT INTO types (id, name) VALUES "
        types_and_names = [(type.value, type.name) for type in PokemonType]
        query += ", ".join(f"({type}, '{name}')" for type, name in types_and_names)
        query += ";"

        try:
            cursor.execute(query)
            connection.commit()
        except Exception as err:
            print("error while inserting types into db = " + str(err))

    def save_pokemon_regions(self, connection, cursor):
        query = "INSERT INTO pokemon_regions (pokemon_id, region_id, local_id) VALUES "
        tuples = [(id, id_obj["type"], id_obj["id"]) for id in self._pokedex.get_pokemons_ids() for id_obj in self._pokedex.get_pokemon(id).get_ids()]
        query += ", ".join(f"({pokemon_id}, {region_enum.value}, {local_id})" for pokemon_id, region_enum, local_id in tuples)
        query += ";"

        try:
            cursor.execute(query)
            connection.commit()
        except Exception as err:
            print("error while inserting pokemon regions into db = " + str(err))
    def save_pokemon_evolutions(self, connection, cursor):
        for i in range(self._pokedex.get_evolution_trees_count()):
            tree = self._pokedex.get_evolution_tree(i)

            evolutions = self.get_evolution_hierarchy(tree)

            query = "INSERT INTO pokemon_evolutions (base_id, evolved_id, condition_raw, condition_encoded) VALUES "
            query += ", ".join(f"({base_id}, {evolved_id}, '{self.fix_simple_quotes(condition_raw)}', '{self.fix_simple_quotes(condition_encoded)}')"
                               for base_id, evolved_id, condition_raw, condition_encoded in evolutions)
            query += ";"

            try:
                cursor.execute(query)
                connection.commit()
            except Exception as err:
                print("error while inserting evolutions into db = " + str(err))

    def save_pokemon_types(self, connection, cursor):
        query = "INSERT INTO pokemon_types (pokemon_id, type_id) VALUES "
        ids_and_types = [(id, type_enum) for id in self._pokedex.get_pokemons_ids() for type_enum in self._pokedex.get_pokemon(id).get_types()]
        query += ", ".join(f"({pokemon_id}, {type_id.value})" for pokemon_id, type_id in ids_and_types)
        query += ";"

        try:
            cursor.execute(query)
            connection.commit()
        except Exception as err:
            print("error while inserting pokemon regions into db = " + str(err))

    def fix_simple_quotes(self, string: str) -> str:
        return string.replace("'","''")

    def get_evolution_hierarchy(self, node: dict) -> dict:

        evolutions = []
        if "evolutions" in node:
            for child in node["evolutions"]:
                evolutions.append( (node["id"], child["id"], child["condition_raw"], child["condition_encoded"]) )
                evolutions += self.get_evolution_hierarchy(child)

        return evolutions