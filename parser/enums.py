#!/usr/bin/env python3

from enum import Enum, auto

# Color = Enum('Color', ['RED', 'GREEN', 'BLUE'], start=0)

PokedexType = Enum( "PokedexType", [
    "NATIONAL",
    # 1st gen
    "KANTO",
    "KANTO_PE",
    # 2nd gen
    "JOHTO",
    "JOHTO_HGSS",
    # 3rd gen
    "HOENN",
    "HOENN_ORAS",
    # 4th gen
    "SINNOH",
    "SINNOH_PT",
    # 5th gen
    "UNOVA",
    "UNOVA_B2W2",
    # 6th gen
    "KALOS",
    # 7th gen
    "ALOLA",
    "ALOLA_USUM",
    # 8th gen
    "GALAR",
    "ISLE_ARMOR",
    "CROWN_TUNDRA",
    "HISUI",
    # 9th gen
    "PALDEA"
], start = 0 )

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
