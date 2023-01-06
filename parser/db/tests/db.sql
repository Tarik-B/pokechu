-- SQLite

SELECT * FROM pokemons;

-- SELECT COUNT(id) FROM pokemons;

-- Pokemon ids by region
-- SELECT id
-- FROM pokemons p
-- JOIN pokemon_regions pr ON pr.pokemon_id = p.id 
-- WHERE pr.region_id = 1;

-- Pokemon ids by region (properly)
-- SELECT pokemon_id, local_id
-- FROM regions
-- INNER JOIN pokemon_regions pr ON regions.id=pr.region_id
-- WHERE pr.region_id = 18
-- -- GROUP BY pokemon_id
-- ORDER BY local_id ASC
-- ;

-- Pokemon local ids
-- SELECT pr.region_id, pr.local_id
-- FROM pokemons p
--       JOIN pokemon_regions pr ON pr.pokemon_id = p.id
-- WHERE p.id = 1;

-- Pokemon local id -> national id
-- SELECT pokemon_id
-- FROM pokemon_regions
-- WHERE local_id = 23 AND region_id = 18
-- ;

-- Local ids by region
-- SELECT p.id as pokemon_id, pr.local_id
-- FROM pokemons p
--       JOIN pokemon_regions pr ON pr.pokemon_id = p.id 
--       JOIN regions r ON pr.region_id = r.id
-- WHERE pr.region_id = 18
-- -- ORDER BY local_id ASC
-- ;

-- Local ids by pokemon (ultra slow)
-- SELECT p.id as pokemon_id, r.id as region_id, pr.local_id
-- FROM pokemons p
--       JOIN pokemon_regions pr ON pr.pokemon_id = p.id 
--       JOIN regions r ON pr.region_id = r.id
-- WHERE p.id = 1
-- ;

-- Pokemon regions
-- SELECT p.id, p.name, GROUP_CONCAT(r.name, ", " ) AS region_names
-- FROM pokemons p
--     JOIN pokemon_regions pr ON pr.pokemon_id = p.id 
--     JOIN regions r ON pr.region_id = r.id
-- GROUP BY p.name;

-- -- Pokemon types
-- SELECT p.id, p.name, GROUP_CONCAT(t.name, ", " ) AS type_names
-- FROM pokemons p
--     JOIN pokemon_types pt ON pt.pokemon_id = p.id
--     JOIN types t ON pt.type_id = t.id
-- GROUP BY p.name
-- ORDER BY p.id ASC
-- ;

-- Pokemon types by pokemon
-- SELECT type_id FROM pokemon_types
-- WHERE pokemon_id = 280
-- ;

-- All pokemons types by pokemon
-- SELECT pokemon_id, GROUP_CONCAT(type_id) FROM pokemon_types
-- GROUP BY pokemon_id
-- ORDER BY pokemon_id ASC
-- ;

-- Pokemons types by region
-- SELECT p.id as pokemon_id, GROUP_CONCAT(type_id)
-- FROM pokemons p
--       LEFT JOIN pokemon_types pt ON pt.pokemon_id = p.id
-- GROUP BY p.id
-- ;

-- Pokemons (list) types
-- SELECT pokemons.id as pokemon_id, GROUP_CONCAT(type_id) as type_ids
-- FROM pokemons
-- LEFT JOIN pokemon_types ON pokemon_types.pokemon_id = pokemons.id
-- WHERE pokemons.id IN (2,3)
-- GROUP BY pokemons.id
-- ;


-- SELECT *
-- FROM pokemon_evolutions
-- -- WHERE condition_raw LIKE "% ou %"
-- WHERE evolved_id = 854 OR base_id = 854
-- ;

-- Evolution links count
-- SELECT *, (
--     SELECT COUNT(*) FROM pokemon_evolutions pe WHERE pe.base_id = p.id OR pe.evolved_id = p.id
-- ) AS evolution_links
-- FROM pokemons p;

-- Find evolution root
-- WITH RECURSIVE evolution_root AS (
--       SELECT pe.base_id, pe.evolved_id, 0 as depth
--       FROM pokemon_evolutions pe
--       WHERE pe.evolved_id = 475
      
--       UNION ALL
      
--       SELECT pe.base_id, pe.evolved_id, evolution_root.depth + 1
--       FROM evolution_root
--       JOIN pokemon_evolutions pe ON pe.evolved_id = evolution_root.base_id
-- )
-- SELECT base_id
-- FROM evolution_root
-- ORDER BY depth DESC LIMIT 1 -- depth is only used to order and limit
-- -- WHERE base_id NOT IN (SELECT evolved_id FROM evolution_root)
-- -- JOIN pokemons p ON p.id = evolution_root.evolved_id
-- ;

-- Evolution chains
-- WITH RECURSIVE evolution_chain AS (
--       SELECT pe.evolved_id, pe.base_id, pe.condition_raw
--       FROM pokemon_evolutions pe
--       WHERE pe.base_id = 172
      
--       UNION ALL
      
--       SELECT pe.evolved_id, pe.base_id, pe.condition_raw
--       FROM evolution_chain
--       JOIN pokemon_evolutions pe ON pe.base_id = evolution_chain.evolved_id
--      )
-- SELECT base_id, evolved_id, condition_raw FROM evolution_chain
-- JOIN pokemons p ON p.id = evolution_chain.evolved_id
-- ;

-- WITH RECURSIVE evolution_chain AS (
--     SELECT
--         base_id,
--         evolved_id,
--         base_id || " > " || evolved_id as path
--     FROM pokemon_evolutions
--     UNION ALL
--     SELECT
--         evolution_chain.base_id,
--         pe.evolved_id,
--         evolution_chain.path || " > " || pe.evolved_id as path
--     FROM pokemon_evolutions pe, evolution_chain
--     -- JOIN evolution_chain ec ON ec.evolved_id = pe.base_id
--     WHERE pe.base_id = evolution_chain.evolved_id
-- )
-- SELECT * FROM evolution_chain
-- -- WHERE base_id = 1
-- -- GROUP BY evolution_chain.base_id
-- ;


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
--     SELECT id, name, parent_id, 0, ""
--     FROM categories
--     WHERE id = 11
    
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