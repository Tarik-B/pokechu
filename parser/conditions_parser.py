#!/usr/bin/env python3

import re
import pokedex
from data import Data
from enums import EvolutionConditionType


class ConditionsParser:
    def __init__(self, pokedex: pokedex.Pokedex):
        self.pokedex = pokedex

    def process_evolution_trees_conditions(self):

        for tree in self.pokedex.evolution_trees:
            self.process_evolution_tree_conditions(tree)

    def process_evolution_tree_conditions(self, node: dict):

        condition_fr = node["condition"]
        # condition_en = node["condition_en"]

        if condition_fr:
            node["conditions"] = self.process_condition_string(condition_fr)

        for child in node["evolutions"]:
            self.process_evolution_tree_conditions(child)

    def process_condition_string(self, condition_string: str):
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
            condition = {"type": EvolutionConditionType.OR.name, "conditions": []}
            # conditions = self.create_operator_condition(results, EvolutionConditionType.OR)

            for result in results:
                subcondition = self.split_into_and_condition(result)
                condition["conditions"].append(subcondition)
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
            condition = {"type": EvolutionConditionType.AND.name, "conditions": []}

            for result in results:
                subcondition = self.process_condition(result)

                condition["conditions"].append(subcondition)
        else:
            condition = self.process_condition(condition_string)

        return condition

    def split_into_conditions(self, condition_string: str):

        condition = self.process_condition(condition_string)
        if condition["type"] == EvolutionConditionType.UNKNOWN.name:
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
            EvolutionConditionType.LEVEL_GAIN: [r"(?:gagner|gain) (?:un|de) niveau"], # Merge both
            # with or

            EvolutionConditionType.DAY: [r"jour", r"de jour", r"en journée"],
            EvolutionConditionType.NIGHT: [r"nuit", r"de nuit"],

            # EvolutionConditionType.ITEM_USE: [r"(?:au contact)+\s+\w+[\s|\']+" + item_name for item_name in
            #                                   item_names ],
            EvolutionConditionType.ITEM_USE: item_names,
            EvolutionConditionType.ITEM_HOLD: [r"en tenant\s\w+[\s|\']+" + item_name for item_name in item_names ],

            EvolutionConditionType.TRADE: ["échange"],

            EvolutionConditionType.KNOW_SKILL: ["apprendre (?:la|une) capacité (.*)"],
            EvolutionConditionType.LEARN_SKILL: ["connaître (?:la|une) capacité (.*)"],
        }

        keep_looking = True
        while keep_looking:

            condition = None

            for type in patterns:

                pattern_list = patterns[type]

                for pattern in pattern_list:

                    condition_string, extracted = self.find_and_pop_pattern(r"^" + pattern, condition_string)
                    if extracted:
                        # print("condition_string = " + condition_string)
                        # print("extracted = " + extracted)

                        if extracted and extracted != pattern:
                            condition = {"type": EvolutionConditionType(type).name, "data": extracted}
                        else:
                            condition = {"type": EvolutionConditionType(type).name}

                    if condition: break
                if condition: break

            if condition:
                conditions.append(condition)
                condition_string = condition_string.strip()
            else:
                keep_looking = False

        if condition_string:
            print(f"no known condition pattern in '{condition_string}', full string = '{full_string}'")

        if not conditions:
            print(f"unknown condition '{condition_string}'")

            condition = {"type": EvolutionConditionType.UNKNOWN.name}
            return condition
        elif len(conditions) == 1:
            return conditions[0]
        else:
            condition = {"type": EvolutionConditionType.AND.name, "conditions": conditions}
            return condition

    def find_and_pop_pattern(self, pattern: str, string: str) -> str:
        # matches = re.finditer(pattern, string)
        match = re.search(pattern, string)
        if match:

            start, end = match.span()

            # if len(match.groups()) == 1:
            if match.groups():
                extracted = match.group(0)
            else:
                extracted = string[start:end]

            newstring = string[end:]

            return newstring, extracted

        return string, None
