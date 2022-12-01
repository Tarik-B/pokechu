package com.example.pokechu_material3.data

data class EvolutionTreeData(
    val id: String,
    val condition: String,
    val evolutions: List<EvolutionTreeData>
)