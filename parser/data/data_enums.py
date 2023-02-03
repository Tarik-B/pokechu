#!/usr/bin/env python3

from enum import Enum, unique

# TODO EXTRACT ALL DATA AND AUTO GENERATE ENUMS USING FUNCTIONAL SYNTAX
# Region = Enum( "Region", [
#     "NATIONAL",
# ], start = 0 )


@unique
class Region(Enum):
    pokepedia_shortcut: str
    abbrev_names: list
    name_fr: str
    name_en: str

    def __new__(cls, pokepedia_shortcut: str, abbrev_names: list, name_fr: str, name_en: str):
        obj = object.__new__(cls)
        obj._value_ = len(cls.__members__) # starts at 0, no + 1

        obj.pokepedia_shortcut = pokepedia_shortcut
        obj.abbrev_names = abbrev_names
        obj.name_fr = name_fr
        obj.name_en = name_en

        return obj

    NATIONAL = ("NDEX", [], "National", "National")
    # 1st gen
    KANTO = ("DEX_KANTO_RBJ", ["Kanto"], "Kanto", "Kanto")  # (also "DEX_KANTO_RFVF")
    KANTO_PE = ("DEX_KANTO_LGPE", ["Kanto LGPE"], "Kanto LGPE", "Kanto LGPE")
    # 2nd gen
    JOHTO = ("DEX_JOHTO_OAC", ["Johto OAC"], "Johto OAC", "Johto")
    JOHTO_HGSS = ("DEX_JOHTO_HGSS", ["Johto HGSS"], "Johto HGSS", "Johto HGSS")
    # 3rd gen
    HOENN = ("DEX_HOENN_RSE", ["Hoenn RSE"], "Hoenn RSE", "Hoenn")
    HOENN_ORAS = ("DEX_HOENN_ROSA", ["Hoenn ROSA"], "Hoenn ROSA", "Hoenn ROSA")
    # 4th gen
    SINNOH = ("DEX_SINNOH_DPP", ["Sinnoh"], "Sinnoh", "Sinnoh")
    SINNOH_PT = ("", ["Sinnoh Pt"], "Sinnoh Pt", "Sinnoh Pt")  # (also "DEX_SINNOH_DEPS")
    # 5th gen
    UNOVA = ("DEX_UNYS_NB", ["Unys NB"], "Unys NB", "Unova")
    UNOVA_B2W2 = ("DEX_UNYS_NB2", ["Unys N2B2"], "Unys N2B2", "Unova B2W2")
    # 6th gen
    KALOS = ("DEX_KALOS_XY", ["Kalos (Centre)", "Kalos (Côtes)", "Kalos (Monts)"], "Kalos", "Kalos")
    # 7th gen
    ALOLA = ("DEX_ALOLA_SL", ["Alola SL"], "Alola SL", "Alola")
    ALOLA_USUM = ("DEX_ALOLA_USUL", ["Alola USUL"], "Alola USUL", "Alola USUM")
    # 8th gen
    GALAR = ("DEX_GALAR_EB", ["Galar"], "Galar", "Galar")
    ISLE_ARMOR = ("DEX_ISOLARMURE_EB", ["Isolarmure"], "Isolarmure", "Isle Armor")
    CROWN_TUNDRA = ("DEX_COURONNEIGE_EB", ["Couronneige"], "Couronneige", "Crown Tundra")
    HISUI = ("DEX_HISUI_LPA", ["Hisui"], "Hisui", "Hisui")
    # 9th gen
    PALDEA = ("DEX_PALDEA_EV", ["Paldea"], "Paldea", "Paldea")

@unique
class Game(Enum):
    name_fr: str
    name_en: str
    region: Region

    def __new__(cls, name_fr: str, name_en: str, region: Region):
        obj = object.__new__(cls)
        obj._value_ = len(cls.__members__) # starts at 0, no + 1

        obj.name_fr = name_fr
        obj.name_en = name_en
        obj.region = region

        return obj

    RED = ("Pokémon Rouge", "Pokémon Red", Region.KANTO)
    BLUE = ("Pokémon Bleu", "Pokémon Blue", Region.KANTO)
    YELLOW = ("Pokémon Jaune", "Pokémon Yellow", Region.KANTO)
    GOLD = ("Pokémon Or", "Pokémon Gold", Region.JOHTO)
    SILVER = ("Pokémon Argent", "Pokémon Silver", Region.JOHTO)
    CRYSTAL = ("Pokémon Cristal", "Pokémon Crystal", Region.JOHTO)
    RUBY = ("Pokémon Rubis", "Pokémon Ruby", Region.HOENN)
    SAPPHIRE = ("Pokémon Saphir", "Pokémon Sapphire", Region.HOENN)
    FIRERED = ("Pokémon Rouge Feu", "Pokémon FireRed", Region.KANTO)
    LEAFGREEN = ("Pokémon Vert Feuille", "Pokémon LeafGreen", Region.KANTO)
    EMERALD = ("Pokémon Émeraude", "Pokémon Emerald", Region.HOENN)
    DIAMOND = ("Pokémon Diamant", "Pokémon Diamond", Region.SINNOH)
    PEARL = ("Pokémon Perle", "Pokémon Pearl", Region.SINNOH)
    PLATINUM = ("Pokémon Platine", "Pokémon Platinum", Region.SINNOH_PT)
    HEARTGOLD = ("Pokémon Or HeartGold", "Pokémon HeartGold", Region.JOHTO_HGSS)
    SOULSILVER = ("Pokémon Argent SoulSilver", "Pokémon SoulSilver", Region.JOHTO_HGSS)
    BLACK = ("Pokémon Noir", "Pokémon Black", Region.UNOVA)
    WHITE = ("Pokémon Blanc", "Pokémon White", Region.UNOVA)
    BLACK2 = ("Pokémon Noir 2", "Pokémon Black 2", Region.UNOVA_B2W2)
    WHITE2 = ("Pokémon Blanc 2", "Pokémon White 2", Region.UNOVA_B2W2)
    X = ("Pokémon X", "Pokémon X", Region.KALOS)
    Y = ("Pokémon Y", "Pokémon Y", Region.KALOS)
    OMEGA_RUBY = ("Pokémon Rubis Oméga", "Pokémon Omega Ruby", Region.HOENN_ORAS)
    ALPHA_SAPPHIRE = ("Pokémon Saphir Alpha", "Pokémon Alpha Sapphire", Region.HOENN_ORAS)
    SUN = ("Pokémon Soleil", "Pokémon Sun", Region.ALOLA)
    MOON = ("Pokémon Lune", "Pokémon Moon", Region.ALOLA)
    ULTRA_SUN = ("Pokémon Ultra-Soleil", "Pokémon Ultra Sun", Region.ALOLA_USUM)
    ULTRA_MOON = ("Pokémon Ultra-Lune", "Pokémon Ultra Moon", Region.ALOLA_USUM)
    LETS_GO_PIKACHU = ("Pokémon : Let's Go, Pikachu", "Pokémon Let's Go, Pikachu!", Region.KANTO_PE)
    LETS_GO_EEVEE = ("Pokémon : Let's Go, Évoli", "Pokémon Let's Go, Eevee!", Region.KANTO_PE)
    SWORD = ("Pokémon Épée", "Pokémon Sword", Region.GALAR)
    SHIELD = ("Pokémon Bouclier", "Pokémon Shield", Region.GALAR)
    ISLE_ARMOR = ("L'île solitaire de l'Armure", "The Isle of Armor", Region.ISLE_ARMOR)
    CROWN_TUNDRA = ("Les terres enneigées de la Couronne", "The Crown Tundra", Region.CROWN_TUNDRA)
    BRILLIANT_DIAMOND = ("Pokémon Diamant Étincelant", "Pokémon Brilliant Diamond", Region.SINNOH_PT)
    SHINING_PEARL = ("Pokémon Perle Scintillante", "Pokémon Shining Pearl", Region.SINNOH_PT)
    LEGENDS_ARCEUS = ("Légendes Pokémon : Arceus", "Pokémon Legends: Arceus", Region.HISUI)
    SCARLET = ("Pokémon Écarlate", "Pokémon Scarlet", Region.PALDEA)
    VIOLET = ("Pokémon Violet", "Pokémon Violet", Region.PALDEA)


@unique
class PokemonType(Enum):
    name_fr: str
    name_en: str

    def __new__(cls, name_fr: str, name_en: str):
        obj = object.__new__(cls)
        obj._value_ = len(cls.__members__) # starts at 0, no + 1

        obj.name_fr = name_fr
        obj.name_en = name_en

        return obj

    STEEL = ("acier", "steel")
    FIGHTING = ("combat", "fighting")
    DRAGON = ("dragon", "dragon")
    WATER = ("eau", "water")
    ELECTRIC = ("électrik", "electric")
    FAIRY = ("fée", "fairy")
    FIRE = ("feu", "fire")
    ICE = ("glace", "ice")
    BUG = ("insecte", "bug")
    NORMAL = ("normal", "normal")
    GRASS = ("plante", "grass")
    POISON = ("poison", "poison")
    PSYCHIC = ("psy", "psychic")
    ROCK = ("roche", "rock")
    GROUND = ("sol", "ground")
    GHOST = ("spectre", "ghost")
    DARK = ("ténèbres", "dark")
    FLYING = ("vol", "flying")

    # Unused
    # UNKNOWN = ("inconnu", "unknown")
    # SHADOW = ("obscur", "shadow")
    # GLITCH = ("bird", "glitch")


@unique
class EvolutionItem(Enum):
    name_fr: str
    name_en: str

    def __new__(cls, name_fr: str, name_en: str):
        obj = object.__new__(cls)
        obj._value_ = len(cls.__members__) # starts at 0, no + 1

        obj.name_fr = name_fr
        obj.name_en = name_en

        return obj

    FIRE_STONE = ("pierre feu", "fire stone")
    WATER_STONE = ("pierre eau", "water stone")
    LEAF_STONE = ("pierre plante", "leaf stone")
    THUNDER_STONE = ("pierre foudre", "thunder stone")
    MOON_STONE = ("pierre lune", "moon stone")
    SUN_STONE = ("pierre soleil", "sun stone")
    DAWN_STONE = ("pierre aube", "dawn stone")
    DUSK_STONE = ("pierre nuit", "dusk stone")
    SHINY_STONE = ("pierre éclat", "shiny stone")
    ICE_STONE = ("pierre glace", "ice stone")

    KINGS_ROCK = ("roche royale", "king's rock")
    LINKING_CORD = ("fil de liaison", "linking cord")
    OVAL_STONE = ("pierre ovale", "oval stone")
    METAL_COAT = ("peau métal", "metal coat")
    SUN_SHARD = ("éclat soleil", "sun shard")
    MOON_SHARD = ("éclat lune", "moon shard")
    RAZOR_CLAW = ("griffe rasoir", "razor claw")
    TART_APPLE = ("pomme acidulée", "tart apple")
    SWEET_APPLE = ("pomme sucrée", "sweet apple")
    CRACKED_POT = ("théière fêlée", "cracked pot")
    CHIPPED_POT = ("théière ébréchée", "chipped pot")
    GIMMIGHOUL_COIN = ("pièce de Mordudor", "Gimmighoul coin")

    BLACK_AUGURITE = ("obsidienne", "black augurite")
    PROTECTOR = ("protecteur", "protector")
    DRAGON_SCALE = ("écaille draco", "dragon scale")
    ELECTIRIZER = ("électriseur", "electirizer")
    MAGMARIZER = ("magmariseur", "magmarizer")
    UPGRADE = ("améliorator", "upgrade")
    DUBIOUS_DISC = ("cd douteux", "dubious disc")
    RAZOR_FANG = ("croc rasoir", "razor fang")
    PRISM_SCALE = ("bel'écaille", "prism scale")
    REAPER_CLOTH = ("tissu fauche", "reaper cloth")
    DEEP_SEA_TOOTH = ("dent océan", "deep sea tooth")
    DEEP_SEA_SCALE = ("écaille océan", "deep sea scale")
    SACHET = ("sachet senteur", "sachet")
    WHIPPED_DREAM = ("chantibonbon", "whipped dream")

    AUSPICIOUS_ARMOR = ("armure de la fortune", "auspicious armor")
    MALICIOUS_ARMOR = ("armure de la rancune", "malicious armor")

@unique
class EvolutionCondition(Enum):
    name_fr: str
    name_en: str
    patterns: list

    def __new__(cls, name_fr: str, name_en: str, patterns: list):
        obj = object.__new__(cls)
        obj._value_ = len(cls.__members__) # starts at 0, no + 1

        obj.name_fr = name_fr
        obj.name_en = name_en
        obj.patterns = patterns

        return obj

    UNKNOWN = ("", "", [])

    AND = ("et", "and", [r"+"])
    OR = ("ou", "or", [r" ou "])

    LEVEL = ("niveau", "level", [r"niveau ([0-9]+)"])
    # ?: = non-capturing groups, is required to do an OR without capturing a group
    LEVEL_GAIN = ("gain de niveau", "level gain", [r"(?:gagner|gain|monter) (?:un|de|d'un) niveau"]) # Merge both with or

    # TODO Merge all items in one pattern with or |
    ITEM_USE = ("utiliser", "use", [r"(?:au contact .*)?(" + EvolutionItem(item_type).name_fr + r")" for
                                    item_type in EvolutionItem])
    ITEM_HOLD = ("tenir", "hold", [r"en tenant\s(?:\w*[\s|\'])?(" + EvolutionItem(item_type).name_fr + r")" for item_type in EvolutionItem])

    HAPPINESS = ("bonheur", "happiness", [r"bonheur"])
    MALE = ("male", "mâle", [r"mâle"])
    FEMALE = ("female", "femelle", [r"femelle"])
    LOCATION = ("lieu", "location", [])

    DAY = ("jour", "day", [r"(?:de|en|pendant)?\s?(?:\w*\s)?(?:journée|jour)"])
    NIGHT = ("nuit", "night", [r"(?:de|pendant)?\s?(?:\w*\s)?nuit"])

    KNOW_SKILL = ("connaître", "know", [r"(?:connaître|en connaissant)\s*(?:la|une)?\s*(?:capacité)? (.*)"])
    LEARN_SKILL = ("apprendre", "learn", [r"apprendre (?:la|une) capacité (.*)"])

    TRADE = ("échange", "trade", [r"échange"])