#!/usr/bin/env python3

import re
import parsel
import requests
import os
import os.path
import urllib.parse

import utils

from pokedex import Pokedex
from data import Region


class ListParser:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose

    def process_pokedex_list_page(self, download_thumbnails: bool):
        # Fetch pokemon list
        base_url = "https://www.pokepedia.fr"
        full_url = base_url + "/" + Region(self._pokedex.get_region()).pokepedia_shortcut

        html = utils.download_page(full_url)
        if not html:
            print(f"error while downloading page '{full_url}'")

        # Get pokemon list table
        xpath_selector = parsel.Selector(html)
        results = xpath_selector.xpath(
            "//table[@class = 'tableaustandard sortable entetefixe']/tbody/tr/td[1]").getall()

        # Pokemon ids (xpath selector removes duplicates, need to use text() on each td)
        ids = []
        for result in results:
            subselector = parsel.Selector(result)
            ids.append(subselector.xpath("//td/text()").get())

        # Fill blanks in ids
        # for pokemon_index in range(len(ids)):
        #     if ids[pokemon_index] is None:
        #         ids[pokemon_index] = ids[pokemon_index - 1]

        # Names
        names_fr = xpath_selector.xpath("//tr/td[@id]/a/text()").getall()
        if len(ids) != len(names_fr):
            raise Exception("error while parsing french names")

        names_en = xpath_selector.xpath("//tr/td[a[starts-with(@title, 'en:')]]/a/text()").getall()
        if len(ids) != len(names_en):
            raise Exception("error while parsing english names")

        unique_ids = xpath_selector.xpath("//tr/td/a/img[number(@alt) = @alt]/@alt").getall()
        if len(ids) != len(unique_ids):
            raise Exception("error while parsing real unique ids")

        # <img alt="742" src="Liste%20des%20Pok%C3%A9mon%20dans%20l'ordre%20du%20Pok%C3%A9dex%20National%20%E2%80%94
        # %20Pok%C3%A9p%C3%A9dia_files/Miniature_742_EB.png" decoding="async" class="notpageimage miniature" width="52" height="52">

        # Fast
        # Get all img urls and then keep only those with 3 digits inside
        thumbnail_urls = xpath_selector.xpath("//tr//img/@src").getall()
        thumbnail_urls = [url for url in thumbnail_urls if re.search(r"\d{3}", url) != None]

        # Also fast
        # all_imgs = xpath_selector.xpath("//tr//img/@src").getall()
        # all_imgs = [img for img in all_imgs if "/thumb/" not in img]

        # Too slow
        # thumbnail_urls = xpath_selector.xpath("//tr//img[number(@alt) = @alt]/@src").getall()

        # Also too slow
        # thumbnail_urls = xpath_selector.xpath("//tr//img[@class = 'notpageimage miniature']/@src").getall()
        # thumbnail_urls = xpath_selector.xpath("//tr//img[contains(@class, 'miniature')]/@src").getall()
        # thumbnail_urls = xpath_selector.xpath("//tr//img[ends-with(@class, 'miniature')]/@src").getall()
        # thumbnail_urls = xpath_selector.xpath("//tr//img[@class]/@src").getall()

        if len(ids) != len(thumbnail_urls):
            raise Exception("error while parsing thumbnail urls")

        # results = sel.xpath("//tr/td[3]/a/text()").getall()

        # Remove duplicates ids (mega evolutions, gigamax, etc.)
        duplicates_ids_index = [index for index in range(len(ids)) if ids[index] is None]
        ids = [ids[i] for i in range(len(ids)) if i not in duplicates_ids_index]
        names_fr = [names_fr[i] for i in range(len(names_fr)) if i not in duplicates_ids_index]
        names_en = [names_en[i] for i in range(len(names_en)) if i not in duplicates_ids_index]
        unique_ids = [unique_ids[i] for i in range(len(unique_ids)) if i not in duplicates_ids_index]
        thumbnail_urls = [thumbnail_urls[i] for i in range(len(thumbnail_urls)) if i not in duplicates_ids_index]

        # Process pokemons
        for pokemon_index in range(len(ids)):

            # paldea_id = ids[pokemon_index]
            unique_id = unique_ids[pokemon_index]
            names = { "fr": names_fr[pokemon_index], "en": names_en[pokemon_index]}

            # Add local pokedex ids
            local_ids = []
            match self._pokedex.get_region():
                case Region.NATIONAL:
                    pass
                case Region.PALDEA:
                    paldea_id = ids[pokemon_index]
                    local_ids.append({Region.PALDEA: paldea_id})

            thumbnail_url = thumbnail_urls[pokemon_index]
            url = urllib.parse.unquote(thumbnail_url)
            thumbnail_filename = os.path.basename(url)

            # Download thumbnail
            if download_thumbnails:
                self.download_thumbnail(thumbnail_url)

            # Add pokemon to pokedex
            self._pokedex.add_pokemon_entry(unique_id, names, thumbnail_filename)

            # self.fetch_pokemon_page(unique_id, name_fr)


    def download_thumbnail(self, thumbnail_url: str):
        base_url = "https://www.pokepedia.fr"
        full_url = base_url + thumbnail_url

        url = urllib.parse.unquote(full_url)
        file_name = os.path.basename(url)
        file_path = "./output/images/pokemons/" + file_name

        # Check if file exists before dl it
        if not os.path.exists(file_path):
            resp = requests.get(full_url)

            with open(file_path, "wb") as file:
                file.write(resp.content)