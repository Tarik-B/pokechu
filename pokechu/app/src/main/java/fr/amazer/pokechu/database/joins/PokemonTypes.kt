package fr.amazer.pokechu.database.joins

import androidx.lifecycle.LiveData
import androidx.room.*
import fr.amazer.pokechu.database.entities.EntityPokemon
import fr.amazer.pokechu.database.entities.EntityType
import fr.amazer.pokechu.enums.PokemonType

@Entity(
    tableName = "pokemon_types",
    primaryKeys = ["pokemon_id", "type_id"],
    foreignKeys = [
        ForeignKey(entity = EntityPokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("pokemon_id"), onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EntityType::class, parentColumns = arrayOf("id"), childColumns = arrayOf("type_id"), onDelete = ForeignKey.CASCADE)
    ]
)
class JoinPokemonTypes(val pokemon_id: Int, val type_id: Int)

data class PokemonIdTypeIds(
    @ColumnInfo(name = "pokemon_id") val pokemon_id: Int,
    @ColumnInfo(name = "type_ids") val type_ids: String?,
) {
    @Ignore
    val type_id_list: List<PokemonType> = type_ids?.split(",")?.map { PokemonType.values()[it.toInt()] } ?: listOf()
}

@Dao
interface DaoPokemonTypes {
    @Query(
        "SELECT pokemons.id as pokemon_id, GROUP_CONCAT(type_id) as type_ids " +
        "FROM pokemons " +
            "LEFT JOIN pokemon_types ON pokemon_types.pokemon_id = pokemons.id " +
        "GROUP BY pokemons.id "
    )
    fun findPokemonsTypes(): LiveData<List<PokemonIdTypeIds>>

    @Query(
        "SELECT pokemons.id as pokemon_id, GROUP_CONCAT(type_id) as type_ids " +
        "FROM pokemons " +
            "JOIN pokemon_regions ON pokemon_regions.pokemon_id = pokemons.id " +
            "LEFT JOIN pokemon_types ON pokemon_types.pokemon_id = pokemons.id " +
        "WHERE pokemon_regions.region_id = :regionId " +
        "GROUP BY pokemons.id " +
        "ORDER BY pokemon_regions.local_id ASC"
    )
    fun findPokemonsTypesByRegion(regionId: Int): LiveData<List<PokemonIdTypeIds>>

    @Query(
        "SELECT type_id FROM pokemon_types " +
        "WHERE pokemon_id = :pokemon_id " +
        "ORDER BY type_id ASC"
    )
    fun findPokemonTypes(pokemon_id: Int): LiveData<List<Int>>
}