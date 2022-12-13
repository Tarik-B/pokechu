#!/usr/bin/env python3

from enum import Enum, auto


class PokedexType(Enum):
    NATIONAL = auto()
    # 1st gen
    KANTO = auto()
    KANTO_PE = auto()
    # 2nd gen
    JOHTO = auto()
    JOHTO_HGSS = auto()
    # 3rd gen
    HOENN = auto()
    HOENN_ORAS = auto()
    # 4th gen
    SINNOH = auto()
    SINNOH_PT = auto()
    # 5th gen
    UNOVA = auto()
    UNOVA_B2W2 = auto()
    # 6th gen
    KALOS = auto()
    # 7th gen
    ALOLA = auto()
    ALOLA_USUM = auto()
    # 8th gen
    GALAR = auto()
    ISLE_ARMOR = auto()
    CROWN_TUNDRA = auto()
    HISUI = auto()
    # 9th gen
    PALDEA = auto()

class EvolutionConditionType(Enum):
    UNKNOWN = auto()

    AND = auto()
    OR = auto()

    LEVEL = auto()
    LEVEL_GAIN = auto()
    ITEM_USE = auto()
    ITEM_HOLD = auto()
    FRIENDSHIP = auto()
    GENDER = auto()
    LOCATION = auto()
    DAY = auto()
    NIGHT = auto()

    KNOW_SKILL = auto()
    LEARN_SKILL = auto()

    TRADE = auto()
class VariantType(Enum):
    GIGAMAX = auto()
    HISUI = auto()
    GALAR = auto()
    ALOLA = auto()
    PALDEA = auto()

class Item(Enum):
    FIRE_STONE = auto()
    WATER_STONE = auto()
    LEAF_STONE = auto()
    THUNDER_STONE = auto()
    MOON_STONE = auto()
    SUN_STONE = auto()
    DAWN_STONE = auto()
    DUSK_STONE = auto()
    SHINY_STONE = auto()
    ICE_STONE = auto()

    KINGS_ROCK = auto()
    LINKING_CORD = auto()
    OVAL_STONE = auto()
    METAL_COAT = auto()
    SUN_SHARD = auto()
    MOON_SHARD = auto()
    RAZOR_CLAW = auto()
    TART_APPLE = auto()
    SWEET_APPLE = auto()
    CRACKED_POT = auto()
    CHIPPED_POT = auto()
    GIMMIGHOUL_COIN = auto()

    BLACK_AUGURITE = auto()
    PROTECTOR = auto()
    DRAGON_SCALE = auto()
    ELECTIRIZER = auto()
    MAGMARIZER = auto()
    UPGRADE = auto()
    DUBIOUS_DISC = auto()
    RAZOR_FANG = auto()
    PRISM_SCALE = auto()
    REAPER_CLOTH = auto()
    DEEP_SEA_TOOTH = auto()
    DEEP_SEA_SCALE = auto()
    SACHET = auto()
    WHIPPED_DREAM = auto()
