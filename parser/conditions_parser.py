#!/usr/bin/env python3

import re
import pokedex
from data import Data
from enums import EvolutionConditionType


class ConditionsParser:
    def __init__(self, pokedex: pokedex.Pokedex, verbose: bool):
        self.pokedex = pokedex
        self.verbose = verbose

        self.processed_condition_count = 0

    def process_evolution_trees_conditions(self):

        for tree in self.pokedex.evolution_trees:
            self.process_evolution_tree_conditions(tree)

    def process_evolution_tree_conditions(self, node: dict):

        # Convert condition string to tree of condition types
        if "condition_raw" in node:
            condition_fr = node["condition_raw"]

            if condition_fr:
                node["conditions"] = self.clean_and_process_condition_string(condition_fr)
                self.processed_condition_count += 1

                # Remove condition string from dict if it was perfectly parsed (no UNKNOWN condition)
                # if self.is_condition_ok(node["conditions"]):
                    # del node["*condition_raw"]

        if "evolutions" in node:
            for child in node["evolutions"]:
                self.process_evolution_tree_conditions(child)

    def clean_and_process_condition_string(self, condition_string: str):
        # We assume that conditions are as follows: (condition AND condition) OR (condition AND condition)
        # with AND = "+"/"dans"/"en"/etc. and OR = "ou"
        # Example: "Gain de niveau dans un champ magnétique spécial ou Pierre Foudre"
        # is: "(Gain de niveau [AND] un champ magnétique spécial) [OR] Pierre Foudre"

        # Build conditions expression tree
        # current_conditions = {}

        # Cleanup string
        condition_string = condition_string.lower()

        # Remove everything between parenthesis
        condition_string = re.sub(r"\(.*?\)", "", condition_string)
        condition_string = condition_string.strip()

        # Clean punctuation
        condition_string = re.sub(r"\,|\;|\.", "", condition_string)

        condition = self.split_into_or_condition(condition_string)

        # print(f"condition = {condition}")

        return condition
    def split_into_or_condition(self, condition_string: str):
        OR_OPERATOR_STRING = " ou "

        condition = None

        # Split in OR parts
        results = re.split(OR_OPERATOR_STRING, condition_string)
        results = [result.strip() for result in results ]

        if len(results) > 1:
            condition = {"type": EvolutionConditionType.OR.name, "children": []}
            # conditions = self.create_operator_condition(results, EvolutionConditionType.OR)

            for result in results:
                subcondition = self.split_into_and_condition(result)
                condition["children"].append(subcondition)
        else:
            condition = self.split_into_and_condition(condition_string)

        return condition

    def split_into_and_condition(self, condition_string: str):
        AND_OPERATOR_STRING = "+"

        condition = None

        # Split in AND parts
        results = [s.strip() for s in condition_string.split(AND_OPERATOR_STRING)]
        results = [result.strip() for result in results]

        if len(results) > 1:
            condition = {"type": EvolutionConditionType.AND.name, "children": []}

            for result in results:
                subcondition = self.process_condition(result)

                condition["children"].append(subcondition)
        else:
            condition = self.process_condition(condition_string)

        return condition

    def split_into_conditions(self, condition_string: str):

        condition = self.process_condition(condition_string)
        if self.verbose and condition["type"] == EvolutionConditionType.UNKNOWN.name:
            print(f"unknown condition '{condition_string}'")

        return condition

    def process_condition(self, condition_string: str):
        USING_ITEM_STRING = "au contact"

        CLOSE_TO_LOCATION_STRING = "près"
        IN_LOCATION_STRING = "dans"
        AT_LOCATION_STRING = "à la"
        AT_LOCATION_STRING_ALT = "au"

        conditions = []
        full_string = condition_string
        # print(f"full condition string = '{full_string}'")

        item_names = [Data.ITEMS[item_type].name_fr for item_type in Data.ITEMS ]
        patterns = {
            EvolutionConditionType.LEVEL: [r"niveau ([0-9]+)"],

            EvolutionConditionType.FRIENDSHIP: [r"bonheur"],

            EvolutionConditionType.GENDER: [r"(mâle|femelle)"],
            # EvolutionConditionType.GENDER_FEMALE: [r"femelle"],

            # ?: = non-capturing groups, is required to do an OR without capturing a group
            EvolutionConditionType.LEVEL_GAIN: [r"(?:gagner|gain|monter) (?:un|de|d'un) niveau"], # Merge both
            # with or

            EvolutionConditionType.DAY: [r"(?:de|en|pendant)?\s?(?:\w*\s)?(?:journée|jour)"],
            EvolutionConditionType.NIGHT: [r"(?:de|pendant)?\s?(?:\w*\s)?nuit"],

            # EvolutionConditionType.ITEM_USE: [r"(?:au contact)+\s+\w+[\s|\']+" + item_name for item_name in
            #                                   item_names ],
            EvolutionConditionType.ITEM_USE: [r"(?:au contact)?(?:\s\w[\s|\']\s)?(" + item_name + r")" for item_name in
                                              item_names ],
            EvolutionConditionType.ITEM_HOLD: [r"en tenant\s(?:\w*[\s|\'])?(" + item_name + r")" for item_name in
                                               item_names ],

            EvolutionConditionType.TRADE: ["échange"],

            EvolutionConditionType.KNOW_SKILL: ["apprendre (?:la|une) capacité (.*)"],
            EvolutionConditionType.LEARN_SKILL: ["(?:connaître|en connaissant) (?:la|une) capacité (.*)"],
        }

        keep_looking = True
        while keep_looking:

            condition = None

            for condition_type in patterns:

                pattern_list = patterns[condition_type]

                for pattern in pattern_list:

                    condition_string, extracted = self.find_and_pop_pattern(r"^" + pattern, condition_string)
                    if extracted:
                        # print("condition_string = " + condition_string)
                        # print("extracted = " + extracted)

                        condition = {"type": EvolutionConditionType(condition_type).name}
                        if type(extracted) is str:
                            condition["data"] = extracted

                        match condition_type:
                            case EvolutionConditionType.LEVEL:
                                pass
                            case EvolutionConditionType.ITEM_USE:
                                pass
                            case EvolutionConditionType.ITEM_HOLD:
                                pass
                            case EvolutionConditionType.GENDER:
                                pass
                            case EvolutionConditionType.LOCATION:
                                pass
                            case EvolutionConditionType.DAY:
                                pass
                            case EvolutionConditionType.NIGHT:
                                pass
                            case EvolutionConditionType.KNOW_SKILL:
                                pass
                            case EvolutionConditionType.LEARN_SKILL:
                                pass
                            case EvolutionConditionType.TRADE:
                                pass


                    if condition: break
                if condition: break

            if condition:
                conditions.append(condition)
                condition_string = condition_string.strip()
            else:
                keep_looking = False

        if condition_string:
            if self.verbose:
                print(f"no known condition pattern in '{condition_string}', full string = '{full_string}'")

            condition = {"type": EvolutionConditionType.UNKNOWN.name}
            conditions.append(condition)

        # if not conditions:
        #     print(f"unknown condition '{condition_string}'")

        if len(conditions) == 1:
            return conditions[0]
        else:
            condition = {"type": EvolutionConditionType.AND.name, "children": conditions}
            return condition

    def find_and_pop_pattern(self, pattern: str, string: str) -> str:
        # matches = re.finditer(pattern, string)
        match = re.search(pattern, string)
        if match:

            start, end = match.span()

            # extracted is a str (if something was captured) or a bool
            if len(match.groups()):
                extracted = match.group(1)
            else:
                extracted = True
            # else:
            #     extracted = string[start:end]

            newstring = string[end:]

            return newstring, extracted

        return string, False

    def is_condition_ok(self, condition: dict):
        if condition["type"] == EvolutionConditionType.UNKNOWN.name:
            return False

        if "children" in condition:
            for subcondition in condition["children"]:
                if not self.is_condition_ok(subcondition):
                    return False

        return True