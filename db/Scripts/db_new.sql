-- SQLite
-- SELECT * FROM pokémons

-- PRAGMA foreign_keys = ON;

-- Reset
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS recipes;

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT
);

-- Insert categories
INSERT INTO categories (title)
VALUES
    ("Plat"),
    ("Dessert");

-- Create recipes table
CREATE TABLE recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    content TEXT,
    category_id INTEGER,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT -- CASCADE
);

-- Insert recipes
INSERT INTO recipes (title, slug, category_id)
VALUES
    ("Crème anglais", "creme-anglaise", 2),
    ("Soupe", "soupe", 1),
    ("Salade de fruit", "salade-de-fruit", 2);

-- Test delete
-- DELETE FROM categories WHERE id = 2;

-- Print
-- SELECT * FROM recipes;

SELECT r.id, r.title, c.title AS category
FROM recipes r
JOIN categories c ON r.category_id = c.id
WHERE c.title = "Dessert";

-- DROP TABLE recipes
-- CREATE UNIQUE INDEX idx_recipes_slug ON recipes (slug)
-- UPDATE recipes SET slug = "soupe2" WHERE id = 4
-- EXPLAIN QUERY PLAN SELECT * FROM recipes WHERE slug = "soupe"
-- PRAGMA index_list("recipes")
-- DROP INDEDX idx_recipes_slug
