-- Create tables
CREATE TABLE pokemons (
    id INTEGER PRIMARY KEY NOT NULL UNIQUE,
    name TEXT NOT NULL,
    height REAL NOT NULL,
    weight REAL NOT NULL
);
CREATE TABLE regions (
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL
);
CREATE TABLE types (
    id INTEGER PRIMARY KEY NOT NULL UNIQUE,
    name TEXT NOT NULL
);
CREATE TABLE pokemon_regions (
    pokemon_id INTEGER NOT NULL,
    region_id INTEGER NOT NULL,
    local_id INTEGER NOT NULL,
    
    PRIMARY KEY (pokemon_id, region_id),
    UNIQUE (pokemon_id, region_id),
    FOREIGN KEY (pokemon_id) REFERENCES pokemons(id) ON DELETE CASCADE,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
);
CREATE TABLE pokemon_evolutions (
    base_id INTEGER NOT NULL,
    evolved_id INTEGER NOT NULL,
    condition_raw TEXT NOT NULL,
    condition_encoded TEXT NOT NULL,

    PRIMARY KEY (base_id, evolved_id),
    UNIQUE (base_id, evolved_id),
    FOREIGN KEY (base_id) REFERENCES pokemons(id) ON DELETE CASCADE,
    FOREIGN KEY (evolved_id) REFERENCES pokemons(id) ON DELETE CASCADE
);
CREATE TABLE pokemon_types (
    pokemon_id INTEGER NOT NULL,
    type_id INTEGER NOT NULL,
    
    PRIMARY KEY (pokemon_id, type_id),
    UNIQUE (pokemon_id, type_id),
    FOREIGN KEY (pokemon_id) REFERENCES pokemons(id) ON DELETE CASCADE,
    FOREIGN KEY (type_id) REFERENCES types(id) ON DELETE CASCADE
);

-- Pokemons by regions
CREATE VIEW view_pokemons_by_regions
AS
    SELECT p.id, p.name, GROUP_CONCAT(r.name, ", " ) AS region_names
    FROM pokemons p
        JOIN pokemon_regions pr ON pr.pokemon_id = p.id 
        JOIN regions r ON pr.region_id = r.id
    GROUP BY p.name;
-- SELECT * FROM view_pokemons_by_regions WHERE region_names LIKE "%";

-- Evolutions
CREATE VIEW view_pokemons_evolutions
AS
    SELECT p.id, p.name, GROUP_CONCAT(o.name, ", " ) AS evolutions
    FROM pokemons p
        LEFT JOIN pokemon_evolutions pe ON pe.base_id = p.id
        LEFT JOIN pokemons o ON o.id = pe.evolved_id
    GROUP BY p.id;
-- SELECT * FROM view_pokemons_evolutions;
