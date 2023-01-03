package fr.amazer.pokechu.ui.details.evolution_tree

import fr.amazer.pokechu.utils.EvolutionConditionData

data class EvolutionNodeData(
    val pokemonId: Int,
    var conditions: EvolutionConditionData?
)