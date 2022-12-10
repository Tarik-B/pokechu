#!/usr/bin/env python3

from enum import Enum, auto


class PokedexType(Enum):
    NDEX = auto()
    DEX_PALDEA_EV = auto()
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
