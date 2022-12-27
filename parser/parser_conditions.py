#!/usr/bin/env python3

import re

from pokedex import Pokedex
from data import ItemType
from data import EvolutionConditionType

class ConditionsParser:
    def __init__(self, pokedex: Pokedex, verbose: bool):
        self._pokedex = pokedex
        self._verbose = verbose

        self._processed_condition_count = 0

    def process_evolution_trees_conditions(self):

        for i in range(self._pokedex.get_evolution_trees_count()):
            tree = self._pokedex.get_evolution_tree(i)
            self.process_evolution_tree_conditions(tree)

    def process_evolution_tree_conditions(self, node: dict):

        # Convert condition string to tree of condition types
        if "condition_raw" in node:
            condition_fr = node["condition_raw"]

            # condition_fr = "Bonheur de jour +Gain de niveau ou Gain de niveau en tenant un Éclat Soleil"
            # OR(AND(AND(HAPPINESS)(DAY))(LEVEL_GAIN))(AND(LEVEL_GAIN)(ITEM_HOLD('14')))
            # 2(1(1(7)(10))(4))(1(4)(6(14)))

            if condition_fr:
                node["condition_encoded"] = self.clean_and_process_condition_string(condition_fr)
                # print(f"encoded condition '{condition_fr}' into {node['condition_encoded']}")
                self._processed_condition_count += 1

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

        # Cleanup string
        condition_string = condition_string.lower()

        # Remove everything between parenthesis
        # condition_string = re.sub(r"\(.*?\)", "", condition_string)
        condition_string = condition_string.strip()

        # Clean punctuation
        condition_string = re.sub(r"\,|\;|\.", "", condition_string)

        condition_encoded_string = self.split_into_or_condition(condition_string)

        return condition_encoded_string
    def split_into_or_condition(self, condition_string: str):
        condition_encoded_string = ""

        # Split in OR parts
        results = re.split(EvolutionConditionType.OR.patterns[0], condition_string)
        results = [result.strip() for result in results ]

        if len(results) > 1:
            condition_encoded_string = str(EvolutionConditionType.OR.value) + "("

            for i in range(len(results)):
                result = results[i]

                encoded_string = self.split_into_and_condition(result)

                condition_encoded_string += encoded_string
                if i != len(results)-1:
                    condition_encoded_string += ","

            condition_encoded_string += ")"

        else:
            encoded_string = self.split_into_and_condition(condition_string)

            condition_encoded_string += encoded_string

        return condition_encoded_string

    def split_into_and_condition(self, condition_string: str):
        condition_encoded_string = ""

        # Split in AND parts
        results = [s.strip() for s in condition_string.split(EvolutionConditionType.AND.patterns[0])]
        results = [result.strip() for result in results]

        if len(results) > 1:
            condition_encoded_string = str(EvolutionConditionType.AND.value) + "("

            for i in range(len(results)):
                result = results[i]

                encoded_string = self.process_condition(result)
                condition_encoded_string += encoded_string
                if i != len(results) - 1:
                    condition_encoded_string += ","

            condition_encoded_string += ")"

        else:
            encoded_string = self.process_condition(condition_string)
            condition_encoded_string += encoded_string

        return condition_encoded_string

    def process_condition(self, condition_string: str):
        USING_ITEM_STRING = "au contact"

        CLOSE_TO_LOCATION_STRING = "près"
        IN_LOCATION_STRING = "dans"
        AT_LOCATION_STRING = "à la"
        AT_LOCATION_STRING_ALT = "au"

        condition_encoded_strings = []

        full_string = condition_string
        # print(f"full condition string = '{full_string}'")

        keep_looking = True
        while keep_looking:

            condition_encoded_string = ""

            for condition_type in EvolutionConditionType:

                # Already treated
                if condition_type in (EvolutionConditionType.AND, EvolutionConditionType.OR):
                    continue

                pattern_list = condition_type.patterns

                for pattern in pattern_list:

                    condition_string, extracted = self.find_and_pop_pattern(r"^" + pattern, condition_string)
                    if extracted:
                        # print("condition_string = " + condition_string)
                        # print("extracted = " + extracted)

                        condition_encoded_string = str(condition_type.value)
                        if type(extracted) is str:
                            # Evolution type with extra data
                            match condition_type:
                                case EvolutionConditionType.LEVEL:
                                    pass
                                # Convert item name to id
                                case EvolutionConditionType.ITEM_USE | EvolutionConditionType.ITEM_HOLD:
                                    for item in ItemType:
                                        if item.name_fr == extracted:
                                            extracted = str(item.value)
                                            break
                                case EvolutionConditionType.KNOW_SKILL | EvolutionConditionType.LEARN_SKILL:
                                    pass

                            condition_encoded_string += "[" + extracted + "]"

                    if condition_encoded_string: break
                if condition_encoded_string: break

            if condition_encoded_string:
                condition_encoded_strings.append(condition_encoded_string)
                condition_string = condition_string.strip()
            else:
                keep_looking = False

        if condition_string:
            if self._verbose:
                print(f"no known condition pattern in '{condition_string}', full string = '{full_string}'")

            condition_encoded_strings.append(str(EvolutionConditionType.UNKNOWN.value))

        if len(condition_encoded_strings) == 1:
            return condition_encoded_strings[0]
        else:
            condition_encoded_string += str(EvolutionConditionType.AND.value) + "("
            condition_encoded_string += ",".join(condition_encoded_strings)
            condition_encoded_string += ")"
            return condition_encoded_string

    def find_and_pop_pattern(self, pattern: str, string: str) -> str:
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