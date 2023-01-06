package fr.amazer.pokechu.viewmodel

import fr.amazer.pokechu.utils.EvolutionConditionData

data class ViewModelEvolutionData(
    val pokemonId: Int,
    val baseId: Int?,
    val evolutionConditions: EvolutionConditionData?,
    val isDiscovered: Boolean,
    val localizedName: String,
    val thumbnailPath: String,
)