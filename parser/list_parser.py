#!/usr/bin/env python3

from parsel import Selector
from urllib.request import urlopen
import requests
import os
import os.path
import urllib.parse

import pokedex

class ListParser:
    def __init__(self, pokedex: pokedex.Pokedex):
        self.pokedex = pokedex

    def process_pokedex_list_page(self, download_thumbnails: bool):

        print("+ Processing Pokemon list page")

        # Fetch pokemon list
        base_url = "https://www.pokepedia.fr"
        full_url = base_url + "/" + self.pokedex.type.name

        html = urlopen(full_url).read().decode("utf-8")

        # Get pokemon list table
        xpath_selector = Selector(html)
        results = xpath_selector.xpath(
            "//table[@class = 'tableaustandard sortable entetefixe']/tbody/tr/td[1]").getall()

        # Pokemon ids (xpath selector removes duplicates, need to use text() on each td)
        ids = []
        for result in results:
            subselector = Selector(result)
            ids.append(subselector.xpath("//td/text()").get())

        # Fill blanks in ids
        for pokemon_index in range(len(ids)):
            if ids[pokemon_index] is None:
                ids[pokemon_index] = ids[pokemon_index - 1]

        print(f"Pokemon count = {len(ids)}")

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

        thumbnail_urls = xpath_selector.xpath("//tr//img[number(@alt) = @alt]/@src").getall()
        if len(ids) != len(thumbnail_urls):
            raise Exception("error while parsing thumbnail urls")

        # results = sel.xpath("//tr/td[3]/a/text()").getall()

        for pokemon_index in range(len(ids)):
            paldea_id = ids[pokemon_index]
            unique_id = unique_ids[pokemon_index]
            name_fr = names_fr[pokemon_index]
            name_en = names_en[pokemon_index]
            thumbnail_url = thumbnail_urls[pokemon_index]

            url = urllib.parse.unquote(thumbnail_url)
            thumbnail_filename = os.path.basename(url)

            # Download thumbnail
            if download_thumbnails:
                self.download_thumbnail(thumbnail_url)

            # Add pokemon to pokedex
            self.pokedex.add_pokemon_entry(unique_id, paldea_id, name_fr, name_en, thumbnail_filename)

            #self.fetch_pokemon_page(unique_id, name_fr)

    def download_thumbnail(self, thumbnail_url: str):
        base_url = "https://www.pokepedia.fr"
        full_url = base_url + thumbnail_url

        url = urllib.parse.unquote(full_url)
        file_name = os.path.basename(url)
        file_path = "./output/images/" + file_name

        # Check if file exists before dl it
        if not os.path.exists(file_path):
            resp = requests.get(full_url)

            with open(file_path, "wb") as file:
                file.write(resp.content)

    # def process_pokemon_page(self, unique_id: str, name_fr: str):
    #
    #     print(f"+ Processing {name_fr}")
    #
    #     # Fetch pokepedia page
    #     base_url = "https://www.pokepedia.fr/Pok%C3%A9mon_n%C2%B0"
    #
    #     full_url = base_url + unique_id
    #     html = urlopen(full_url).read().decode("utf-8")