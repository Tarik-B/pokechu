package fr.amazer.pokechu.ui.fragments.tree

import fr.amazer.pokechu.utils.EvolutionConditionData

data class EvolutionNodeData(
    val pokemonId: Int,
    var conditions: EvolutionConditionData?
)