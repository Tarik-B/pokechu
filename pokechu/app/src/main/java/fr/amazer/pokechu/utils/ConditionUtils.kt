package fr.amazer.pokechu.utils

import fr.amazer.pokechu.data.EvolutionConditionType
import java.util.*


class EvolutionConditionData {
    var type = EvolutionConditionType.UNKNOWN
    var data = ""
    var nested = ArrayList<EvolutionConditionData>()
}

class ConditionUtils {
    companion object {

        fun parseEncodedCondition(string: String): EvolutionConditionData {
            val chars = string.toCharArray()
            val (data, _) = parseEncodedCondition(chars)
            return data
        }

        fun parseEncodedCondition(chars: CharArray): Pair<EvolutionConditionData, Int> {
            val result = EvolutionConditionData()
            var typeString = ""
            var length = 0

            var i = 0
            while (i < chars.size) {
                length++
                if (chars[i] == ')') {
                    return Pair(result, length)
                }
                if (chars[i] == '(') {
                    result.type = EvolutionConditionType.values()[typeString.toInt()]

                    when (result.type) {
                        EvolutionConditionType.LEVEL,
                        EvolutionConditionType.ITEM_USE,
                        EvolutionConditionType.ITEM_HOLD,
                        EvolutionConditionType.KNOW_SKILL,
                        EvolutionConditionType.LEARN_SKILL -> {
                            do {
                                ++i
                                result.data += chars[i]
                            }
                            while(chars[i+1] != ')')
                        }
                        else -> {
                            val (data,dataLength) = parseEncodedCondition(Arrays.copyOfRange(chars, i+1, chars.size))
                            i += dataLength
                            length += dataLength
                            result.nested.add(data)
                        }
                    }
                }
                else {
                    typeString += chars[i]
                }
                i++
            }
            return Pair(result, length)
        }
    }
}