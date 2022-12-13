package fr.amazer.pokechu.data

data class DataPokemon(
    val ids: List<DataPokemonId>,
    val thumbnail: String
)

data class DataPokemonId(
    val type: String,
    val id: String
)