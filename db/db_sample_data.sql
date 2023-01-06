-- Insert sample data
INSERT INTO pokemons (id, name) VALUES
    (1, "Bulbizarre"), (2, "Herbizarre"), (3, "Florizarre"),
    (280, "Tarsal"), (281, "Kirlia"),
    (282, "Gardevoir"), (475, "Gallame");
-- SELECT * FROM pokemons;

INSERT INTO regions (id, name) VALUES
    (1, "Kanto"), (3, "Johto"), (18, "Paldea");
-- SELECT * FROM regions;

INSERT INTO pokemon_regions (pokemon_id, region_id, local_id) VALUES
    (1, 1, 1), (1, 3, 226),
    (2, 1, 2), (2, 3, 227),
    (3, 1, 3), (3, 3, 228),
    (280, 18, 62), (281, 18, 63),
    (282, 18, 64), (475, 18, 65);
-- SELECT * FROM pokemon_regions;

INSERT INTO pokemon_evolutions (base_id, evolved_id, condition_raw) VALUES
    (1, 2, "Niveau 16"), (2, 3, "Niveau 32"),
    (280, 281, "Niveau 20"),
    (281, 282, "Niveau 30"), (281, 475, "Mâle (♂) + Pierre Aube");
-- SELECT * FROM pokemon_evolutions;