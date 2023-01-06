-- SQLite
-- SELECT * FROM pokémons

-- PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS table2;
DROP TABLE IF EXISTS table1;


CREATE TABLE table1 (
    id INTEGER PRIMARY KEY NOT NULL UNIQUE,
    title VARCHAR(10) NOT NULL
);

CREATE TABLE table2 (
    tid INTEGER NOT NULL,
    parent_id INTEGER NOT NULL,

    PRIMARY KEY (tid, parent_id),
    UNIQUE (tid, parent_id),
    FOREIGN KEY (tid) REFERENCES table1(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES table1(id) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO table1
    VALUES
    (1, "t1"),
    (2, "t2"),
    (3, "t3"),
    (4, "t4"),
    (5, "t5"),
    (6, "t6"),
    (7, "t7");
-- SELECT * FROM table1;

INSERT INTO table2
    VALUES
    (2, 1),
    (3, 1),
    (4, 2),
    (5, 3),
    (7, 6);
-- SELECT * FROM table2;

WITH RECURSIVE cte AS (
      SELECT t2.tid, t2.parent_id FROM table2 t2
      WHERE t2.parent_id = 1
      
      UNION ALL
      
      SELECT t2.tid, t2.parent_id FROM cte
      JOIN table2 t2 ON t2.parent_id = cte.tid
     )
SELECT tid, parent_id FROM cte
JOIN table1 t1 ON t1.id = cte.tid
;

-- Test delete
-- DELETE FROM categories WHERE id = 2;

-- Print
-- SELECT * FROM recipes;

-- SELECT r.id, r.title, c.title AS category
-- FROM recipes r
-- JOIN categories c ON r.category_id = c.id
-- WHERE c.title = "Dessert";

-- DROP TABLE recipes
-- CREATE UNIQUE INDEX idx_recipes_slug ON recipes (slug)
-- UPDATE recipes SET slug = "soupe2" WHERE id = 4
-- EXPLAIN QUERY PLAN SELECT * FROM recipes WHERE slug = "soupe"
-- PRAGMA index_list("recipes")
-- DROP INDEDX idx_recipes_slug
