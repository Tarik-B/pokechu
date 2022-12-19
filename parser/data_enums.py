#!/usr/bin/env python3

from enum import Enum, auto, unique

class AutoNumber(Enum):
    def __new__(cls):
        value = len(cls.__members__)  # note no + 1
        obj = object.__new__(cls)
        obj._value_ = value
        return obj


# class EvolutionConditionType(Enum):
#     UNKNOWN = auto()
#
#     AND = auto()
#     OR = auto()
#
#     LEVEL = auto()
#     LEVEL_GAIN = auto()
#     ITEM_USE = auto()
#     ITEM_HOLD = auto()
#     FRIENDSHIP = auto()
#     GENDER = auto()
#     LOCATION = auto()
#     DAY = auto()
#     NIGHT = auto()
#
#     KNOW_SKILL = auto()
#     LEARN_SKILL = auto()
#
#     TRADE = auto()

# class VariantType(Enum):
#     GIGAMAX = auto()
#     HISUI = auto()
#     GALAR = auto()
#     ALOLA = auto()
#     PALDEA = auto()
