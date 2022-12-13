-- SQLite

PRAGMA foreign_keys = ON;

-- Reset
DROP TABLE IF EXISTS pokemon_regions;
DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS pokemon_evolutions;
DROP TABLE IF EXISTS pokemons;

DROP VIEW IF EXISTS view_pokemons_by_regions;
DROP VIEW IF EXISTS view_pokemons_evolutions;

-- Create tables
CREATE TABLE pokemons (
    id INTEGER PRIMARY KEY NOT NULL UNIQUE,
    name VARCHAR(10) NOT NULL
    -- user_id INTEGER NOT NULL,
    -- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE regions (
    id INTEGER PRIMARY KEY NOT NULL,
    name VARCHAR(10) NOT NULL
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
    condition_raw VARCHAR(150) NOT NULL,

    PRIMARY KEY (base_id, evolved_id),
    UNIQUE (base_id, evolved_id),
    FOREIGN KEY (base_id) REFERENCES pokemons(id) ON DELETE CASCADE,
    FOREIGN KEY (evolved_id) REFERENCES pokemons(id) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO regions (id, name) VALUES
    (1, "Kanto"), (3, "Johto"), (18, "Paldea");
-- SELECT * FROM regions;

INSERT INTO pokemons
    VALUES
    (1, "Bulbizarre"), (2, "Herbizarre"), (3, "Florizarre"),
    (280, "Tarsal"), (281, "Kirlia"),
    (282, "Gardevoir"), (475, "Gallame");
-- SELECT * FROM pokemons;

INSERT INTO pokemon_regions VALUES
    (1, 1, 1), (1, 3, 226),
    (2, 1, 2), (2, 3, 227),
    (3, 1, 3), (3, 3, 228),
    (280, 18, 62), (281, 18, 63),
    (282, 18, 64), (475, 18, 65);
-- SELECT * FROM pokemon_regions;

INSERT INTO pokemon_evolutions /*(base_id, evolved_id, condition_raw)*/ VALUES
    (1, 2, "Niveau 16"), (2, 3, "Niveau 32"),
    (280, 281, "Niveau 20"),
    (281, 282, "Niveau 30"), (281, 475, "Mâle (♂) + Pierre Aube");
-- SELECT * FROM pokemon_regions;

-- Pokemons by regions
CREATE VIEW view_pokemons_by_regions
AS
    SELECT p.id, p.name, GROUP_CONCAT(r.name, ", " ) AS region_names
    FROM pokemons p
        JOIN pokemon_regions pr ON pr.pokemon_id = p.id 
        JOIN regions r ON pr.region_id = r.id
    GROUP BY p.name;
-- SELECT * FROM view_pokemons_by_regions WHERE region_names LIKE "%";

-- Pokemons evolutions
    -- base_id INTEGER NOT NULL,
    -- evolved_id INTEGER NOT NULL,
    -- condition_raw VARCHAR(150) NOT NULL,

-- SELECT * FROM pokemon_evolutions;
-- SELECT id, name FROM pokemons;

-- Pokemon evolutions
CREATE VIEW view_pokemons_evolutions
AS
    SELECT p.id, p.name, GROUP_CONCAT(o.name, ", " ) AS evolutions
    FROM pokemons p
        LEFT JOIN pokemon_evolutions pe ON pe.base_id = p.id
        LEFT JOIN pokemons o ON o.id = pe.evolved_id
    GROUP BY p.id;
SELECT * FROM view_pokemons_evolutions;

-- WITH RECURSIVE recursive_evolutions (base_id, evolved_id, level, path) AS (
--     SELECT base_id, evolved_id, 0, "" FROM pokemon_evolutions
--     UNION ALL
--     SELECT
--         e.base_id,
--         e.evolved_id,
--         recursive_evolutions.level + 1,
--         recursive_evolutions.path || recursive_evolutions.base_id || " > "
--     FROM pokemon_evolutions e, recursive_evolutions
--     WHERE e.evolved_id = recursive_evolutions.base_id
-- )
-- SELECT evolved_id, level, path FROM recursive_evolutions;

-- WITH RECURSIVE children (evolved_id, base_id, level, path) AS (
--     SELECT evolved_id, base_id, 0, "" FROM pokemon_evolutions
--     UNION ALL
--     SELECT
--         e.evolved_id,
--         e.base_id,
--         children.level + 1,
--         children.path || children.evolved_id || " > "
--     FROM pokemon_evolutions e, children
--     WHERE e.base_id = children.evolved_id
-- )
-- SELECT base_id, level, path FROM children;

-- 
-- 

-- DROP TABLE IF EXISTS categories;
-- CREATE TABLE IF NOT EXISTS categories (
--     id INTEGER PRIMARY KEY AUTOINCREMENT,
--     name VARCHAR(255) NOT NULL,
--     parent_id INTEGER,
--     FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE CASCADE
-- );
-- -- DELETE FROM categories;
-- INSERT INTO categories
-- VALUES  (10, "Poisson", NULL),
--         (11, "Requin", 10),
--         (12, "Requin blanc", 11),
--         (13, "Grand requin blanc", 12),
--         (14, "Petit requin blanc", 12);

-- SELECT * FROM categories;

-- WITH RECURSIVE children (id, name, parent_id, level, path) AS (
--     SELECT id, name, parent_id, 0, "" FROM categories WHERE id = 11
--     UNION ALL
--     SELECT
--         c.id,
--         c.name,
--         c.parent_id,
--         children.level + 1,
--         children.path || children.name || " > "
--     FROM categories c, children
--     WHERE c.parent_id = children.id
-- )
-- SELECT id, name, level, path FROM children;

/*
SELECT r.title
FROM ingredients i
JOIN ingredients_recipes ir ON ir.ingredient_id = i.id 
JOIN recipes r ON ir.recipe_id = r.id
WHERE i.name = 'Oeuf';

SELECT * FROM recipes WHERE id = 2;

SELECT ir.quantity, ir.unit, i.name
FROM ingredients_recipes ir 
JOIN ingredients i ON ir.ingredient_id = i.id
WHERE ir.recipe_id = 2;
*/

-- SELECT i.name, COUNT(ir.recipe_id) as count
-- FROM ingredients i 
-- LEFT JOIN ingredients_recipes ir ON ir.ingredient_id = i.id
-- LEFT JOIN recipes r ON ir.recipe_id = r.id
-- GROUP BY i.name
-- ORDER BY count DESC, i.name ASC
-- LIMIT 3 OFFSET 3;

/*
SELECT DISTINCT i.name
FROM ingredients i 
LEFT JOIN ingredients_recipes ir ON ir.ingredient_id = i.id
LEFT JOIN recipes r ON ir.recipe_id = r.id
WHERE ir.recipe_id IS NOT NULL;
LEFT JOIN recipes r ON ir.recipe_id = r.id
*/