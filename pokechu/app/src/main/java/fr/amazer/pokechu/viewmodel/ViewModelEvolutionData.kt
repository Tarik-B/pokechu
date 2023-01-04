package fr.amazer.pokechu.viewmodel

import fr.amazer.pokechu.utils.EvolutionConditionData

data class ViewModelEvolutionData(
    val pokemonId: Int,
    var baseId: Int = 0,
    var evolutionConditions: EvolutionConditionData? = null,

    var isDiscovered: Boolean = false,
    var localizedName: String = "",
    var thumbnailPath: String = "",
)