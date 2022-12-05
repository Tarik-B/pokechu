package com.example.pokechu_material3.data

data class PokemonData(
    val ids: PokemonDataIds,
    val images: PokemonDataImages
)

data class PokemonDataIds(
    val unique: String,
    val paldea: String
)

data class PokemonDataImages(
    val thumbnail: String
)