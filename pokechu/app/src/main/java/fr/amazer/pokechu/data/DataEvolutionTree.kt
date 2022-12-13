package fr.amazer.pokechu.data

data class DataEvolutionTree(
    val id: String,
    val condition_raw: String,
    val conditions: DataEvolutionCondition,
    val evolutions: List<DataEvolutionTree>
)

data class DataEvolutionCondition(
    val type: String,
    val data: String,
    val children: List<DataEvolutionCondition>,


)