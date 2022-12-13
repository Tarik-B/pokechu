#!/usr/bin/env python3
import re
from collections import OrderedDict

import parsel

import pokedex
import utils

import requests
import concurrent.futures

from data import Data
from enums import PokedexType


class DataParser:
    def __init__(self, pokedex: pokedex.Pokedex, verbose: bool):
        self.pokedex = pokedex
        self.verbose = verbose

    def process_pokemon_pages(self):
        # self.process_pokemon_page("013")

        for unique_id in self.pokedex.pokemons:
            self.process_pokemon_page(unique_id)

        ids_by_regions = dict()

        for unique_id in self.pokedex.pokemons:
            pokemon = self.pokedex.pokemons[unique_id]
            if "ids" not in pokemon:
                continue

            id_objs = pokemon["ids"]
            for id_obj in id_objs:
                region = id_obj["type"]
                id = id_obj["id"]
                if region not in ids_by_regions:
                    ids_by_regions[region] = []
                ids_by_regions[region].append(id)

        if self.verbose:
            for region in PokedexType:
                region_name = region.name

                print(f"========== region {region_name} ==========")

                if region_name in ids_by_regions:
                    regions = ids_by_regions[region_name]
                    regions.sort()

                    print(f"count = {len(regions)}")
                    print(f"{regions}")

    def process_pokemon_page(self, unique_id: str):
        # Fetch pokepedia page
        base_url = "https://www.pokepedia.fr/Pok%C3%A9mon_n%C2%B0"

        full_url = base_url + unique_id
        html = utils.download_page(full_url)

        # Keep only fiche table via regex first, before using parsel selector
        match_table = re.search(r"<table class=[\"|'].* ficheinfo", html)
        if not match_table:
            raise Exception("error while parsing pokemon card/fiche table")
        start, end = match_table.span()
        html = html[start:]
        opened = 1
        start = len("<table")
        while True:
            match = re.search(r"</?table>?", html[start:])
            if not match:
                break

            newstart, newend = match.span()
            tag = html[start+newstart:start+newend]
            start += newstart+len(tag)
            if tag == "<table":
                opened += 1
            else:
                opened -= 1

            if opened == 0:
                break

        html = html[:start]

        # Replace all img by their alt texts
        # table = utils.replace_imgs_by_alt_texts(html)
        # table_2d: list[list[str]] = utils.table_to_2d(table)
        # for row in table_2d:
        #     print(row)

        # Get card/fiche info
        xpath_selector = parsel.Selector(html)
        results = xpath_selector.xpath("//table[contains(@class, 'ficheinfo')]").getall()
        if len(results) != 1:
            raise Exception("error while parsing pokemon card/fiche info")

        xpath_selector = parsel.Selector(results[0])
        ids_table = xpath_selector.xpath("//table//table[1]").get()

        xpath_selector = parsel.Selector(ids_table)
        region_ids = xpath_selector.xpath("//td[number(text()) = text()]/text()").getall()
        region_names = xpath_selector.xpath("//strong/a/text()").getall() #|//strong/sup/a/span/text()
        regions_and_variants = xpath_selector.xpath("//strong//text()").getall()
        i = 0
        while i < len(regions_and_variants):
            if i != 0 and (i >= len(region_names) or region_names[i] != regions_and_variants[i]):
                if region_names[i - 1] == regions_and_variants[i - 1]:
                    regions_and_variants[i - 1] += " "
                regions_and_variants[i - 1] += regions_and_variants[i]
                del regions_and_variants[i]
            else:
                i += 1

        if len(regions_and_variants) != len(region_ids):
            raise Exception("error while parsing region links")

        regions_links = xpath_selector.xpath("//strong/a/@href").getall()
        if len(regions_links) != len(region_ids):
            raise Exception("error while parsing region links")

        # variants = re.split("|".join(regions_names), "".join(regions_and_variants))
        # if len(regions_names) != len(variants):
        #     raise Exception("error while parsing pokedex regions")

        # regions = [regions_names[i] + "_" + variants[i] for i in range(len(regions_names)) if variants[i]]

        ids = list(tuple())
        for i in range(len(regions_and_variants)):

            region = regions_and_variants[i]

            pokedex_type = None
            for type in Data.POKEDEXES:
                if region in Data.POKEDEXES[type].abbrev_names:
                    pokedex_type = type
                    break

            if pokedex_type == None:
                if self.verbose:
                    print("unknown region = " + region)
                continue

            # ids.append( { "type": PokedexType(pokedex_type).name, "id": region_ids[i] })
            ids.append( (pokedex_type, region_ids[i]) )

        self.pokedex.add_pokemon_ids(unique_id, ids)
