#!/usr/bin/env python3
from enums import PokedexType
from pokedex import Pokedex
import sqlite3
from contextlib import closing

class SQLiteWriter:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self.pokedex = pokedex
        self.verbose = verbose

    def save_all(self, db_file_path: str):
        db_clear = self.read_sql_file("../db/db_clear.sql")
        db_init = self.read_sql_file("../db/db_init.sql")
        if not db_clear or not db_init:
            return

        try:
            with closing(sqlite3.connect(db_file_path)) as connection:
                with closing(connection.cursor()) as cursor:

                    # Reset and reinit db
                    cursor.executescript(db_clear)
                    cursor.executescript(db_init)
                    connection.commit()

                    self.save_pokemon_list(cursor)
                    connection.commit()

                    self.save_regions(cursor)
                    connection.commit()

                    self.save_pokemon_regions(cursor)
                    connection.commit()

                    self.save_evolutions(cursor)
                    connection.commit()

        except Exception as err:
            print("error while inserting into db = " + str(err))

    def save_pokemon_list(self, cursor):
        query = "INSERT INTO pokemons (id, name) VALUES "
        ids_and_names = [ (id,self.pokedex.pokemon_names[id]["fr"]) for id in self.pokedex.pokemons]
        query += ", ".join(f"({id}, '{name}')" for id,name in ids_and_names)
        query += ";"

        cursor.execute(query)
    def save_regions(self, cursor):
        query = "INSERT INTO regions (id, name) VALUES "
        regions_and_names = [(region.value, region.name) for region in PokedexType]
        query += ", ".join(f"({region}, '{name}')" for region, name in regions_and_names)
        query += ";"

        cursor.execute(query)

    def save_pokemon_regions(self, cursor):
        query = "INSERT INTO pokemon_regions (pokemon_id, region_id, local_id) VALUES "
        tuples = [(id, idobj["type"], idobj["id"]) for id in self.pokedex.pokemons for idobj in self.pokedex.pokemons[id]["ids"] ]
        query += ", ".join(f"({pokemon_id}, {PokedexType[region_id].value}, {local_id})" for pokemon_id, region_id, local_id in tuples)
        query += ";"

        cursor.execute(query)
    def save_evolutions(self, cursor):
        for tree in self.pokedex.evolution_trees:
            evolutions = []

            evolutions += self.get_evolution_hierarchy(tree)
            # if node:
            #     return tree, node


            query = "INSERT INTO pokemon_evolutions VALUES "
            query += ", ".join(f"({base_id}, {evolved_id}, '{self.fix_simple_quotes(condition_raw)}')"
                               for base_id, evolved_id, condition_raw in evolutions)
            query += ";"

            cursor.execute(query)

    def fix_simple_quotes(self, string: str) -> str:
        return string.replace("'","''")

    def get_evolution_hierarchy(self, node: dict) -> dict:

        evolutions = []
        if "evolutions" in node:
            for child in node["evolutions"]:
                evolutions.append( (node["id"], child["id"], child["condition_raw"]) )
                evolutions += self.get_evolution_hierarchy(child)

        return evolutions

    def read_sql_file(self, file_path: str) -> str:
        with open(file_path, "r") as file:
            try:
                return file.read()
            except OSError as e:
                raise Exception(f"error '{e}' while reading {file_path}")
        return None
