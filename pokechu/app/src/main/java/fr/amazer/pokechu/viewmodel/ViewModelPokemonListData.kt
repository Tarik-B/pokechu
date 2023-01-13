package fr.amazer.pokechu.viewmodel

data class ViewModelPokemonListData(
    val pokemonId: Int,
    val localId: Int,

    val names: Map<String, String>,

    val isDiscovered: Boolean,
    val isCaptured: Boolean,
//    val hasEvolutionTree: Boolean,

    val thumbnailPath: String,
    val typeImagePaths: List<String>,
)