#!/usr/bin/env python3
from enums import Item, EvolutionConditionType, PokedexType


class PokedexData:
    def __init__(self, pokepedia_shortcut: str, abbrev_names: list):
        self.pokepedia_shortcut = pokepedia_shortcut
        self.abbrev_names = abbrev_names

class ItemData:
    def __init__(self, name_fr: str, name_en: str):
        self.name_fr = name_fr
        self.name_en = name_en

class Data:
    POKEDEXES = {
        PokedexType.NATIONAL: PokedexData("NDEX", []),
        # 1st gen
        PokedexType.KANTO: PokedexData("DEX_KANTO_RBJ", ["Kanto"]), # Kanto (also "DEX_KANTO_RFVF")
        PokedexType.KANTO_PE: PokedexData("DEX_KANTO_LGPE", ["Kanto LGPE"]), # Kanto (Pokémon : Let's Go, Pikachu et Let's Go, Évoli)
        # 2nd gen
        PokedexType.JOHTO: PokedexData("DEX_JOHTO_OAC", ["Johto OAC"]), # Johto
        PokedexType.JOHTO_HGSS: PokedexData("DEX_JOHTO_HGSS", ["Johto HGSS"]), # Johto (Pokémon Or HeartGold et Argent SoulSilver)
        # 3rd gen
        PokedexType.HOENN: PokedexData("DEX_HOENN_RSE", ["Hoenn RSE"]), # Hoenn
        PokedexType.HOENN_ORAS: PokedexData("DEX_HOENN_ROSA", ["Hoenn ROSA"]), # Hoenn (Pokémon Rubis Oméga et Saphir Alpha)
        # 4th gen
        PokedexType.SINNOH: PokedexData("DEX_SINNOH_DPP", ["Sinnoh"]), # Sinnoh (also "DEX_SINNOH_DEPS")
        PokedexType.SINNOH_PT: PokedexData("", ["Sinnoh Pt"]), # Sinnoh (Pokémon Platine)
        # 5th gen
        PokedexType.UNOVA: PokedexData("DEX_UNYS_NB", ["Unys NB"]), # Unys
        PokedexType.UNOVA_B2W2: PokedexData("DEX_UNYS_NB2", ["Unys N2B2"]), # Unys (Pokémon Noir 2 et Blanc 2)
        # 6th gen
        PokedexType.KALOS: PokedexData("DEX_KALOS_XY", ["Kalos (Centre)", "Kalos (Côtes)", "Kalos (Monts)"]), # Kalos
        # 7th gen
        PokedexType.ALOLA: PokedexData("DEX_ALOLA_SL", ["Alola SL"]), # Alola
        PokedexType.ALOLA_USUM: PokedexData("DEX_ALOLA_USUL", ["Alola USUL"]), # Alola (Pokémon Ultra-Soleil et Ultra-Lune)
        # 8th gen
        PokedexType.GALAR: PokedexData("DEX_GALAR_EB", ["Galar"]), # Galar
        PokedexType.ISLE_ARMOR: PokedexData("DEX_ISOLARMURE_EB", ["Isolarmure"]), # Isolarmure
        PokedexType.CROWN_TUNDRA: PokedexData("DEX_COURONNEIGE_EB", ["Couronneige"]), # Couronneige
        PokedexType.HISUI: PokedexData("DEX_HISUI_LPA", ["Hisui"]), # Hisui
        # 9th gen
        PokedexType.PALDEA: PokedexData("DEX_PALDEA_EV", ["Paldea"]), # Paldea
    }

    ITEMS = {
        Item.FIRE_STONE:	    ItemData("pierre feu", "fire stone"),
        Item.WATER_STONE:	    ItemData("pierre eau", "water stone"),
        Item.LEAF_STONE:	    ItemData("pierre plante", "leaf stone"),
        Item.THUNDER_STONE:     ItemData("pierre foudre", "thunder stone"),
        Item.MOON_STONE:	    ItemData("pierre lune", "moon stone"),
        Item.SUN_STONE:	        ItemData("pierre soleil", "sun stone"),
        Item.DAWN_STONE:	    ItemData("pierre aube", "dawn stone"),
        Item.DUSK_STONE:	    ItemData("pierre nuit", "dusk stone"),
        Item.SHINY_STONE:	    ItemData("pierre éclat", "shiny stone"),
        Item.ICE_STONE:	        ItemData("pierre glace", "ice stone"),

        Item.KINGS_ROCK:	    ItemData("roche royale", "king's rock"),
        Item.LINKING_CORD:	    ItemData("fil de liaison", "linking cord"),
        Item.OVAL_STONE:	    ItemData("pierre ovale", "oval stone"),
        Item.METAL_COAT:	    ItemData("peau métal", "metal coat"),
        Item.SUN_SHARD:	        ItemData("éclat soleil", "sun shard"),
        Item.MOON_SHARD:	    ItemData("éclat lune", "moon shard"),
        Item.RAZOR_CLAW:	    ItemData("griffe rasoir", "razor claw"),
        Item.TART_APPLE:	    ItemData("pomme acidulée", "tart apple"),
        Item.SWEET_APPLE:	    ItemData("pomme sucrée", "sweet apple"),
        Item.CRACKED_POT:	    ItemData("théière fêlée", "cracked pot"),
        Item.CHIPPED_POT:	    ItemData("théière ébréchée", "chipped pot"),
        Item.GIMMIGHOUL_COIN:	ItemData("pièce de Mordudor", "Gimmighoul coin"),

        Item.BLACK_AUGURITE:	ItemData("obsidienne", "black augurite"),
        Item.PROTECTOR:         ItemData("protecteur", "protector"),
        Item.DRAGON_SCALE:	    ItemData("écaille draco", "dragon scale"),
        Item.ELECTIRIZER:	    ItemData("électriseur", "electirizer"),
        Item.MAGMARIZER:	    ItemData("magmariseur", "magmarizer"),
        Item.UPGRADE:           ItemData("améliorator", "upgrade"),
        Item.DUBIOUS_DISC:	    ItemData("cd douteux", "dubious disc"),
        Item.RAZOR_FANG:	    ItemData("croc rasoir", "razor fang"),
        Item.PRISM_SCALE:	    ItemData("bel'écaille", "prism scale"),
        Item.REAPER_CLOTH:	    ItemData("tissu fauche", "reaper cloth"),
        Item.DEEP_SEA_TOOTH:	ItemData("dent océan", "deep sea tooth"),
        Item.DEEP_SEA_SCALE:	ItemData("écaille océan", "deep sea scale"),
        Item.SACHET:	        ItemData("sachet senteur", "sachet"),
        Item.WHIPPED_DREAM:     ItemData("chantibonbon", "whipped dream"),
    }