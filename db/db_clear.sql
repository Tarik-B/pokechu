-- PRAGMA foreign_keys = ON;

-- Reset
DROP TABLE IF EXISTS pokemon_types;
DROP TABLE IF EXISTS pokemon_evolutions;
DROP TABLE IF EXISTS pokemon_regions;
DROP TABLE IF EXISTS games;
DROP TABLE IF EXISTS types;
DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS pokemons;

DROP VIEW IF EXISTS view_pokemons_by_regions;
DROP VIEW IF EXISTS view_pokemons_evolutions;
