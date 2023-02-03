#!/usr/bin/env python3

import os
import re
import parsel
import pokebase
import concurrent.futures
# import pokepy # produces "AttributeError: module 'collections' has no attribute 'MutableMapping'"

from utils import utils

from data.pokedex import Pokedex
from data.data_enums import Region, PokemonType, Game

POKEBASE_PICKLE_CACHE_FOLDER = "./output/cache/pokebase_pickle/"

class DataParser:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose
        self.games = []
        self.forms = {"noform": 0}

        self.versions = {}
        self.processed_count = 0
    def process_pokemon_pages(self):

        print(f"cpu count = {os.cpu_count()}")

        with concurrent.futures.ThreadPoolExecutor() as executor:
            print(f"executor max_workers = {executor._max_workers}")

            futures = []

            pokemon_ids = self._pokedex.get_pokemons_ids()

            # pokemon_ids = ["025", "238", "260", "483", "484", "487", "492", "513", "617", "619", "626", "641", "642",
            #                "645", "710", "711", "718", "720", "745", "746", "774", "888", "889"]

            # pokemon_ids = ["746"]
            # pokemon_ids = ["025"]
            # pokemon_ids = ["509"]
            # pokemon_ids = ["492"]
            # pokemon_ids = ["586"]
            # pokemon_ids = ["666"]
            pokemon_ids = ["800"]

            # pokemon_ids = ["641", "800", "898", "741", "493", "642", "905", "645", "646"]
            def split(ids: list, n: int):
                k, m = divmod(len(ids), n)
                return (ids[i * k + min(i, m):(i + 1) * k + min(i + 1, m)] for i in range(n))

            splitted_ids = list(split(pokemon_ids, executor._max_workers))
            # size = 100
            # splitted_ids = [pokemon_ids[i:i + size] for i in range(0, len(pokemon_ids), size)]

            for split in splitted_ids:
                # if int(unique_id) % 100 == 0:
                # print(f"Processing data of pokemon {unique_id}")

                futures.append(executor.submit(self._process_pokemon_pages_list, ids=split))

            concurrent.futures.wait(futures, return_when=concurrent.futures.ALL_COMPLETED)  # ALL_COMPLETED is actually the
            for future in concurrent.futures.as_completed(futures):
                try:
                    result = future.result()
                except Exception as e:
                    print(f"error '{e}' on data processing thread")

        # for unique_id in self._pokedex.get_pokemons_ids():

            # if int(unique_id)%100 == 0:
            #     print(f"Processing data of pokemon {unique_id}")

            # self._process_pokemon_page(unique_id)

        print(self.games)
        print(self.forms)
        print(self.versions)

    def _process_pokemon_pages_list(self, ids: list):
        for id in ids:
            self._process_pokemon_page(id)

    def _process_pokemon_page(self, unique_id: str):
        # print(f"Processing data of pokemon {unique_id}")

        # unique_id = "025"

        # Fetch pokepedia page
        base_url = "https://www.pokepedia.fr/Pok%C3%A9mon_n%C2%B0"

        full_url = base_url + unique_id
        html = utils.download_page(full_url)
        if not html:
            print(f"error while downloading page '{full_url}'")
            return

        # Get pokeapi data
        # pokeapi_url = "https://pokeapi.co/api/v2/pokemon/" + str(int(unique_id))
        # json_string = utils.download_page(pokeapi_url)
        # if not json_string:
        #     print(f"error while downloading page '{pokeapi_url}'")
        #     return
        # pokeapi_data = json.loads(json_string)
        pokeapi_data = None

        pokebase_data = self.get_pokebase_data(unique_id) # too damn long
        # pokepy_client = pokepy.V2Client() # error, see import
        # pokepy_data = pokepy_client.get_pokemon(1)

        # self._process_fiche_info(unique_id, html, pokeapi_data)

        self._process_pokebase_data(unique_id, pokebase_data)

        self._process_descriptions(unique_id, html)

        self.processed_count += 1
        if self.processed_count % 50 == 0:
            print(f"Finished processing data of {self.processed_count}th pokemon {unique_id}")

    def _process_fiche_info(self, unique_id: str, html: str, pokeapi_data: dict):
        # Keep only fiche table via regex first, before using parsel selector
        match_table = re.search(r"<table class=[\"|'].* ficheinfo", html)
        if not match_table:
            raise Exception("error while parsing pokemon card/fiche table")
        start, end = match_table.span()
        html = html[start:]
        opened_table_tags = 1
        start = len("<table")
        while True:
            match = re.search(r"</?table>?", html[start:])
            if not match:
                break

            newstart, newend = match.span()
            tag = html[start+newstart:start+newend]
            start += newstart+len(tag)
            opened_table_tags += 1 if tag == "<table" else -1

            if opened_table_tags == 0:
                break

        html = html[:start]

        # Get card/fiche info
        xpath_selector = parsel.Selector(html)
        results = xpath_selector.xpath("//table[contains(@class, 'ficheinfo')]").getall()
        if len(results) != 1:
            raise Exception("error while parsing pokemon card/fiche info")

        self._process_pokemon_ids(unique_id, results[0], pokeapi_data)
        self._process_pokemon_data(unique_id, results[0], pokeapi_data)

    def _process_pokemon_ids(self, unique_id: str, html_table: str, pokeapi_data: dict):
        xpath_selector = parsel.Selector(html_table)
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
            for type in Region:
                if region in Region(type).abbrev_names:
                    pokedex_type = type
                    break

            if pokedex_type == None:
                if self._verbose:
                    print("unknown region = " + region)
                continue

            # ids.append( { "type": Region(pokedex_type).name, "id": region_ids[i] })
            ids.append( (pokedex_type, region_ids[i]) )

            # FIX add all pokemons from kanto to kanto lgpe too
            # FIX add all pokemons from sinnoh to sinnoh pt too
            if (pokedex_type is Region.KANTO):
                ids.append((Region.KANTO_PE, region_ids[i]))
            elif (pokedex_type is Region.SINNOH):
                ids.append((Region.SINNOH_PT, region_ids[i]))

        self._pokedex.add_pokemon_ids(unique_id, ids)

    def _process_pokemon_data(self, unique_id: str, html_table: str, pokeapi_data: dict):

        # Replace all img by their alt texts
        table = utils.replace_imgs_by_alt_texts(html_table)
        table_2d: list[list[str]] = utils.table_to_2d(table)
        for i in range(len(table_2d)):
                row = table_2d[i]
                if not row:
                    continue

                if row[0] == "Type" or row[0] == "Types":
                    row = utils.uniqify_list(row)

                    pokemon_types = []
                    for j in range(1, len(row)):
                        cell = row[j]

                        # Split by "-"
                        types = []
                        if " - " in cell:
                            types += cell.split(" - ")
                        else:
                            types.append(cell)

                        for type in types:
                            # Remove everything between parenthesis
                            type = re.sub(r"\(.*?\)", "", type)

                            match = re.search(r"Miniature_Type_(.*).png", type)
                            if match:
                                type = match.group(1).lower()
                            else:
                                print(f"pokemon {unique_id}, invalid type = {len(type)}")
                                continue

                            # TOOD convert type to PokemonType
                            pokemon_type = None
                            for pktype in PokemonType:
                                if PokemonType(pktype).name_fr == type:
                                    pokedex_type = PokemonType(pktype)

                            if pokedex_type == None:
                                if self.verbose:
                                    print("unknown pokemon type = " + type)
                                continue

                            pokemon_types.append(pokedex_type)

                    self._pokedex.get_pokemon(unique_id).set_types(pokemon_types)

                elif row[0].startswith("Nom japonais"):
                    row = utils.uniqify_list(row)
                    if len(row) != 2:
                        print(f"pokemon {unique_id}, invalid japanese name row length = {len(row)}")
                        continue
                elif row[0].startswith("Nom anglais"):
                    row = utils.uniqify_list(row)
                    if len(row) != 2:
                        print(f"pokemon {unique_id}, invalid english name row length = {len(row)}")
                        continue
                elif  row[0].startswith("Taille"):
                    row = utils.uniqify_list(row)
                    if row[0] == "Tailles" and (i + 1) < len(table_2d):
                        row += utils.uniqify_list(table_2d[i + 1])

                    if len(row) <= 1:
                        print(f"pokemon {unique_id}, invalid height row length = {len(row)}")
                        continue

                    for j in range(1,len(row)):
                        height = float(self._process_pokemon_height(unique_id, row[j]))
                        # if height != pokebase_data.height:
                        #     print(f"pokemon {unique_id}, {j-1} parsed height = {height} != pokebase height ="
                        #           f" {pokebase_data.height}")
                        # if height != pokeapi_data["height"]:
                        #     print(f"pokemon {unique_id}, {j-1} parsed height = {height} != pokeapi json height ="
                        #           f" {pokeapi_data['height']}")

                        # if not height.is_integer():
                        #     print(f"pokemon {unique_id}, height not an integer = {height}")

                        self._pokedex.get_pokemon(unique_id).set_height(height)

                elif row[0].startswith("Poids"):
                    row = utils.uniqify_list(row)
                    if len(row) == 1 and (i + 1) < len(table_2d):
                        row += utils.uniqify_list(table_2d[i + 1])

                    if len(row) <= 1:
                        print(f"pokemon {unique_id}, invalid weight row length = {len(row)}")
                        continue

                    for j in range(1, len(row)):
                        weight = float(self._process_pokemon_weight(unique_id, row[j]))
                        # if weight != pokebase_data.weight:
                        #     print(f"pokemon {unique_id}, {j-1} parsed weight = {weight} != pokebase weight ="
                        #           f" {pokebase_data.weight}")
                        # if weight != pokeapi_data["weight"]:
                        #     print(f"pokemon {unique_id}, {j-1} parsed weight = {weight} != pokeapi json weight ="
                        #           f" {pokeapi_data['weight']}")

                        # if not weight.is_integer():
                        #     print(f"pokemon {unique_id}, weight not an integer = {weight}")

                        self._pokedex.get_pokemon(unique_id).set_weight(weight)

                # ['Nom japonais', 'フシギダネ Fushigidane']
                # ['Nom anglais', 'Bulbasaur']
                # Descriptions du Pokédex
                # Talents
    def _process_pokemon_height(self, unique_id: str, cell: list) -> float:
        return self._process_pokemon_float(unique_id, cell, "m")

    def _process_pokemon_weight(self, unique_id: str, cell: list) -> float:
        return self._process_pokemon_float(unique_id, cell, "kg")

    def _process_pokemon_float(self, unique_id: str, cell: list, unit: str) -> float:
        cell = cell.replace(",", ".")

        if match := re.search(f"(\d+\.\d+) " + unit, cell):
            value = match.group(1)
            try:
                value = float(value)
                return value
            except ValueError as e:
                print(f"pokemon {unique_id}, could not parse float {value}'")
        else:
            print(f"pokemon {unique_id}, could not parse float {cell}'")
            return 0.0
    def get_pokebase_data(self, unique_id: str):

        # full_path = POKEBASE_PICKLE_CACHE_FOLDER + unique_id
        #
        # if os.path.exists(full_path):
        #     with open(full_path, "rb") as file:
        #         try:
        #             return pickle.load(file)
        #         except OSError as e:
        #             print(f"error '{e}' while reading pokebase file {full_path}")
        #             # return None # just keep going and call the api then

        pokebase_data = pokebase.pokemon(int(unique_id))

        # try:
        #     dir_path = os.path.dirname(full_path)
        #     os.makedirs(dir_path, exist_ok=True)
        #
        #     with open(full_path, "wb") as outfile:
        #         pickle.dump(pokebase_data, outfile)
        # except OSError as e:
        #     print(f"error '{e}' while write pokebase file {full_path}")
        #     # return None # just keep going and return the file

        return pokebase_data

    def _process_pokebase_data(self, unique_id: str, pokebase_data: pokebase.interface.APIResource):

        POKEBASE_VERSION_TO_GAME = {
            "sword": [Game.SWORD],
            "shield": [Game.SHIELD],
            "black": [Game.BLACK],
            "white": [Game.WHITE],
            "black-2": [Game.BLACK2],
            "white-2": [Game.WHITE2],
            "x": [Game.X],
            "y": [Game.Y],
            "omega-ruby": [Game.OMEGA_RUBY],
            "alpha-sapphire": [Game.ALPHA_SAPPHIRE],
            "ultra-sun": [Game.ULTRA_SUN],
            "ultra-moon": [Game.ULTRA_MOON],
            "ruby": [Game.RUBY],
            "sapphire": [Game.SAPPHIRE],
            "emerald": [Game.EMERALD],
            "firered": [Game.FIRERED],
            "leafgreen": [Game.LEAFGREEN],
            "diamond": [Game.DIAMOND],
            "pearl": [Game.PEARL],
            "platinum": [Game.PLATINUM],
            "heartgold": [Game.HEARTGOLD],
            "soulsilver": [Game.SOULSILVER],
            "sun": [Game.SUN],
            "moon": [Game.MOON],
            "legends-arceus": [Game.LEGENDS_ARCEUS],
            "lets-go-pikachu": [Game.LETS_GO_PIKACHU],
            "lets-go-eevee": [Game.LETS_GO_EEVEE],
            "gold": [Game.GOLD],
            "silver": [Game.SILVER],
            "crystal": [Game.CRYSTAL],
            "red": [Game.RED],
            "blue": [Game.BLUE],
            "yellow": [Game.YELLOW],
        }

        species: pokebase.interface.APIResource = pokebase_data.species
        flavor_text_entries: list = species.flavor_text_entries

        flavor_text_entry: pokebase.interface.APIMetadata
        for flavor_text_entry in flavor_text_entries:

            language: pokebase.interface.APIResource = flavor_text_entry.language
            language_name: str = language.name

            if language_name not in {"fr", "en"}:
                continue

            flavor_text: str = flavor_text_entry.flavor_text

            version: pokebase.interface.APIResource = flavor_text_entry.version
            version_name: str = version.name

            # if version_name not in self.versions:
            #     self.versions[version_name] = 0
            #
            # self.versions[version_name] += 1

            if version_name not in POKEBASE_VERSION_TO_GAME:
                print(f"error while parsing flavor text of pokemon {unique_id}, unknown version = {version}")
                continue

            games = POKEBASE_VERSION_TO_GAME[version_name]

            for game in games:
                self._pokedex.get_pokemon(unique_id).add_description(language_name, game, flavor_text)

    def _process_descriptions(self, unique_id: str, html: str):

        # Find description pages urls
        xpath_selector = parsel.Selector(html)
        results = xpath_selector.xpath("//table[@data-section = 'Descriptions']//a/@href").getall()
        # if len(results) != 1:
        #     raise Exception("error while parsing pokemon descriptions")

        # Remove
        results = utils.uniqify_list(results)
        # results = [result for result in results if results.count(result) == 1]

        # Download all pages
        for url in results:
            self._process_description_page(unique_id, url)

        ############
        # FORMS
        xpath_selector = parsel.Selector(html)
        results = xpath_selector.xpath("//h3[*/@id = 'Formes']").getall()
        if len(results) == 0:
            # Has no forms
            return

        results = xpath_selector.xpath("//h3[*/@id = 'Formes']/following-sibling::center[1][*/@class='gallery "
                                       "mw-gallery-traditional']").getall()
        if len(results) != 0:

            if len(results) != 1:
                print(f"error while parsing forms of pokemon {unique_id}, multiple galleries found")
                return

            xpath_selector = parsel.Selector(results[0])
            results = xpath_selector.xpath("//div[@class='gallerytext']").getall()
            for form in results:

                form = re.sub(r"<br ?/?>", " ", form)
                form = re.sub(r"<.*?>", "", form)
                form = form.strip()
                if form:
                    print(f"found form for pokemon {unique_id} = {form}")

        # else:
        #     results = xpath_selector.xpath("//h3[*/@id = 'Formes']/following-sibling::table[1][contains(@class, 'tableaustandard')]").getall()
        #
        #     if len(results) != 1:
        #         print(f"error while parsing forms of pokemon {unique_id}, no gallery found")
        #         return
        #
        #     xpath_selector = parsel.Selector(results[0])
        #     results = xpath_selector.xpath("//td/text()").getall()
        #     for form in results:
        #         form = form.strip()
        #         if form:
        #             print(f"found form for pokemon {unique_id} = {form}")

    def _process_description_page(self, unique_id: str, url: str):

        # Fetch pokepedia page
        base_url = "https://www.pokepedia.fr"

        full_url = base_url + url
        html = utils.download_page(full_url)
        if not html:
            print(f"error while downloading page '{full_url}'")
            return

        # Parse games and descriptions
        xpath_selector = parsel.Selector(html)

        results = xpath_selector.xpath("//table[@data-section = 'Descriptions']/following-sibling::*[position()=1][name()='dl']").getall()

        results2 = xpath_selector.xpath("//*[preceding-sibling::table[@data-section='Descriptions'] and "
                                       "following-sibling::table[starts-with(@data-section, 'Localisation')] "
                                       "and ("
                                       "((self::h3 or self::h4) and following-sibling::dl)"
                                       " or "
                                       "((preceding-sibling::h3 or preceding-sibling::h4) and self::dl)"
                                       ")]").getall()

        if len(results) != 0 and len(results2) != 0 and len(results) != len(results2):
            print(f"pokemon {unique_id}, len(results) = {len(results)} and len(results2) = {len(results2)}")

        if len(results) == 0:
            # Found no descriptions as direct sibling of "Descriptions du Pokédex"
            # Try to find subtitles of pokemon forms (see Pikachu's page)
            # results = xpath_selector.xpath("//*[self::h3 or self::h4][preceding-sibling::table["
            #                                 "@data-section='Descriptions'] and "
            #                                 "following-sibling::table[@data-section='Localisations']]/span["
            #                                 "@class='mw-headline']/text()").getall()
            results = xpath_selector.xpath("//*[preceding-sibling::table[@data-section='Descriptions'] and "
                                           "following-sibling::table[starts-with(@data-section, 'Localisation')] "
                                           "and ("
                                                "((self::h3 or self::h4) and following-sibling::dl)"
                                                " or "
                                                "((preceding-sibling::h3 or preceding-sibling::h4) and self::dl)"
                                           ")]").getall()

            if len(results) == 0 or len(results) % 2 != 0:
                # raise Exception("error while parsing pokemon descriptions")
                print(f"error while parsing pokemon {unique_id} descriptions, found no descriptions at url {url}")
                return

            for i in range(0, len(results), 2):
                form = results[i]
                lines = results[i+1]

                # self._process_form_line(unique_id, form)
                self._process_description_lines(unique_id, lines)

        else:
            if len(results) != 1:
                print(f"error while parsing pokemon {unique_id} descriptions at url {url}")
                return

            self.forms["noform"] += 1

            self._process_description_lines(unique_id, results[0])

    def _process_form_line(self, unique_id: str, line):
        xpath_selector = parsel.Selector(line)
        line_result = xpath_selector.xpath("//span[@class='mw-headline']/text()").getall()

        if len(line_result) != 1:
            print(f"error while parsing pokemon {unique_id} descriptions")
            return

        if line_result[0] not in self.forms:
            self.forms[line_result[0]] = 0

        self.forms[line_result[0]] += 1
    def _process_description_lines(self, unique_id: str, lines):

        # Clean games (<a>) and descriptions (<dd>)
        xpath_selector = parsel.Selector(lines)
        line_results = xpath_selector.xpath("//dl/dt//a|//dl/dd").getall()

        if len(line_results) == 0:# or len(line_results) % 2 != 0:
            print(f"error while parsing pokemon {unique_id} descriptions")
            return

        pokepedia_games_descs = []
        games = []
        descriptions = []
        for line in line_results:

            def clean_line(line: str):
                line = re.sub(r"<.*?>", "", line)
                line = line.replace(u'\xa0', u' ')
                return line

            if not line.startswith("<dd>"):

                if len(descriptions) != 0 and len(games) != 0:
                    pokepedia_games_descs.append((games, descriptions))
                    games = []
                    descriptions = []
                elif len(descriptions) == 0 and len(games) == 0:
                    pass
                # else:
                #     print("error")

                line = clean_line(line)
                games.append(line)

                if line not in self.games:
                    self.games.append(line)
            else:
                if len(games) == 0:
                    print("error")

                line = clean_line(line)
                descriptions.append(line)

        if len(games) != 0 and len(descriptions) != 0:
            pokepedia_games_descs.append((games, descriptions))
        elif len(games) == 0 and len(descriptions) == 0:
            pass
        else:
            print(f"error, games {games} have no descriptions")

        # for line in line_results:
        #     if line.startswith("Épisode"):
        #         print("found")
        #
        #     if not line.startswith("<dd>") and line not in self.games:
        #         self.games.append(line)

        POKEPEDIA_GAME_TO_GAMES = {
            "Pokémon Versions Noire et Blanche": [Game.BLACK, Game.WHITE],
            "Pokémon Versions Noire 2 et Blanche 2": [Game.BLACK2, Game.WHITE2],
            "Pokémon Noir 2 et Blanc 2": [Game.BLACK2, Game.WHITE2],
            "Pokémon Noir et Blanc 2": [Game.BLACK2, Game.WHITE2],

            "Pokémon Versions Diamant et Perle": [Game.DIAMOND, Game.PEARL],
            "Pokémon Diamant et Perle": [Game.DIAMOND, Game.PEARL],
            "Pokémon Perle": [Game.PEARL],

            "Pokémon Rubis": [Game.RUBY],
            "Pokémon Saphir": [Game.SAPPHIRE],
            "Pokémon Rubis et Saphir": [Game.RUBY, Game.SAPPHIRE],
            "Pokémon Versions Rubis et Saphir": [Game.RUBY, Game.SAPPHIRE],
            "Pokémon Rubis Oméga et Saphir Alpha": [Game.OMEGA_RUBY, Game.ALPHA_SAPPHIRE],

            "Pokémon Émeraude": [Game.EMERALD],
            "Pokémon Version Émeraude": [Game.EMERALD],

            "Pokémon Rouge et Bleu": [Game.RED, Game.BLUE],
            "Pokémon Versions Rouge et Bleue": [Game.RED, Game.BLUE],
            "Pokémon Rouge Feu et Vert Feuille": [Game.FIRERED, Game.LEAFGREEN],
            "Pokémon Versions Rouge Feu et Vert Feuille": [Game.FIRERED, Game.LEAFGREEN],
            "Pokémon Rouge Feu": [Game.FIRERED],
            "Pokémon Vert Feuille": [Game.LEAFGREEN],

            "Pokémon Jaune": [Game.YELLOW],
            "Pokémon Version Jaune": [Game.YELLOW],

            "Pokémon Or": [Game.GOLD],
            "Pokémon Argent": [Game.SILVER],
            "Pokémon Or et Argent": [Game.GOLD, Game.SILVER],
            "Pokémon Versions Or et Argent": [Game.GOLD, Game.SILVER],
            "Pokémon Versions Or HeartGold et Argent SoulSilver": [Game.HEARTGOLD, Game.SOULSILVER],
            "Or HeartGold": [Game.HEARTGOLD],

            "Pokémon Version Platine": [Game.PLATINUM],
            "Pokémon Platine": [Game.PLATINUM],
            "Pokémon Cristal": [Game.CRYSTAL],
            "Pokémon Version Cristal": [Game.CRYSTAL],

            "Pokémon Écarlate": [Game.SCARLET],
            "Pokémon Violet": [Game.VIOLET],
            "Pokémon Écarlate et Violet": [Game.SCARLET, Game.VIOLET],
            "Pokémon : Let's Go, Pikachu et Let's Go, Évoli": [Game.LETS_GO_PIKACHU, Game.LETS_GO_EEVEE],
            "Pokémon Épée et Bouclier": [Game.SWORD, Game.SHIELD],
            "Pokémon X et Y": [Game.X, Game.Y],
            "Pokémon X": [Game.X],

            "Pokémon Soleil et Lune": [Game.SUN, Game.MOON],
            "Pokémon Ultra-Soleil et Ultra-Lune": [Game.ULTRA_SUN, Game.ULTRA_MOON],
            "Pokémon Diamant Étincelant et Perle Scintillante": [Game.BRILLIANT_DIAMOND, Game.SHINING_PEARL],
            "Légendes Pokémon : Arceus": [Game.LEGENDS_ARCEUS],
        }

        for (pokepedia_games, descriptions) in pokepedia_games_descs:
            for pokepedia_game in pokepedia_games:
                if pokepedia_game in POKEPEDIA_GAME_TO_GAMES:
                    games = POKEPEDIA_GAME_TO_GAMES[pokepedia_game]
                    for game in games:
                        for description in descriptions:
                            self._pokedex.get_pokemon(unique_id).add_description("fr", game, description)
