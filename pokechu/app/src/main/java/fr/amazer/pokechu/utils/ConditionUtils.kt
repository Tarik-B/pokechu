package fr.amazer.pokechu.utils

import fr.amazer.pokechu.enums.EvolutionConditionType
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

        private fun parseEncodedCondition(chars: CharArray): Pair<EvolutionConditionData, Int> {
            val result = EvolutionConditionData()
            var typeString = ""
            var length = 0

            var i = 0
            while (i < chars.size) {
                length++
                when(chars[i]) {
                    '(' ->  {
                        do {
                            val (data,dataLength) = parseEncodedCondition(Arrays.copyOfRange(chars, i+1, chars.size))
                            i += dataLength
                            length += dataLength
                            result.nested.add(data)
                        }
                        while( chars[i] == ',')
                    }
                    '[' -> {
                        do {
                            ++i
                            ++length
                            result.data += chars[i]
                        }
                        while(chars[i+1] != ']')
                    }
                    ')' -> { break }
                    ',' -> { break }
                    ']' -> { }
                    else -> { typeString += chars[i] }
                }

                i++
            }

            result.type = EvolutionConditionType.values()[typeString.toInt()]

            return Pair(result, length)
        }
    }
}