#!/usr/bin/env python3
from enums import Item, EvolutionConditionType


class ItemData:
    def __init__(self, name_fr: str, name_en: str):
        self.name_fr = name_fr
        self.name_en = name_en

class Data:
    ITEMS = {
        Item.FIRE_STONE:	    ItemData("pierre feu", "fire stone"),
        Item.WATER_STONE:	    ItemData("pierre eau", "water stone"),
        Item.LEAF_STONE:	    ItemData("pierre plante", "leaf stone"),
        Item.THUNDER_STONE:	ItemData("pierre foudre", "thunder stone"),
        Item.MOON_STONE:	    ItemData("pierre lune", "moon stone"),
        Item.SUN_STONE:	    ItemData("pierre soleil", "sun stone"),
        Item.DAWN_STONE:	    ItemData("pierre aube", "dawn stone"),
        Item.DUSK_STONE:	    ItemData("pierre nuit", "dusk stone"),
        Item.SHINY_STONE:	    ItemData("pierre éclat", "shiny stone"),
        Item.ICE_STONE:	    ItemData("pierre glace", "ice stone"),

        Item.KINGS_ROCK:	    ItemData("roche royale", "king's rock"),
        Item.LINKING_CORD:	ItemData("fil de liaison", "linking cord"),
        Item.OVAL_STONE:	    ItemData("pierre ovale", "oval stone"),
        Item.METAL_COAT:	    ItemData("peau métal", "metal coat"),
        Item.SUN_SHARD:	    ItemData("éclat soleil", "sun shard"),
        Item.MOON_SHARD:	    ItemData("éclat lune", "moon shard"),
        Item.RAZOR_CLAW:	    ItemData("griffe rasoir", "razor claw"),
        Item.TART_APPLE:	    ItemData("pomme acidulée", "tart apple"),
        Item.SWEET_APPLE:	    ItemData("pomme sucrée", "sweet apple"),
        Item.CRACKED_POT:	    ItemData("théière fêlée", "cracked pot"),
        Item.CHIPPED_POT:	    ItemData("théière ébréchée", "chipped pot"),
        Item.GIMMIGHOUL_COIN:	ItemData("pièce de Mordudor", "Gimmighoul coin"),
    }