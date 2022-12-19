#!/usr/bin/env python3

import parsel

import utils

from data import PokemonType
from pokedex import Pokedex

class MiscParser:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose

    def process_type_pages(self):
        for type in PokemonType:

            full_url = "https://www.pokepedia.fr/" + type.name_fr + "_(type)"
            html = utils.download_page(full_url)
            if not html:
                print(f"error while downloading type page '{full_url}'")

            xpath_selector = parsel.Selector(html)
            results = xpath_selector.xpath("//table[contains(@class,'tableaustandard')]").getall()

            # table = utils.replace_imgs_by_alt_texts(results[0])

            table_2d: list[list[str]] = utils.table_to_2d(results[0])
            for row in table_2d:
                print(row)

    def process_evolution_list_page(self, lang: str):
        if lang == "fr":
            full_url = "https://www.pokepedia.fr/Liste_des_Pok%C3%A9mon_par_famille_d%27%C3%A9volution"
        else:
            full_url = "https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_evolution_family"

        html = utils.download_page(full_url)
        if not html:
            print(f"error while downloading page '{full_url}'")

        # html = html.replace("\n", "")

        # Get evolution tables
        # xpath_selector = parsel.Selector(html)
        # if lang == "fr":
        #     results = xpath_selector.xpath("//table[@class = 'tableaustandard centre']").getall()
        # else:
        #     results = xpath_selector.xpath("//table[@class = 'roundy']").getall()

        # # Replace all img by their alt texts
        # table = utils.replace_imgs_by_alt_texts(table)
        #
        # table_2d: list[list[str]] = utils.table_to_2d(table)
        # # pprint(table_2d, width=30)
        # for row in table_2d:
        #
        #     if len(row) == 0:
        #         continue
