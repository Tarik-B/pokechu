package fr.amazer.pokechu.data

data class EvolutionTreeData(
    val id: String,
    val condition: String,
    val evolutions: List<EvolutionTreeData>
)