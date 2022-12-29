package fr.amazer.pokechu.enums

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Query
import androidx.room.ColumnInfo
import androidx.room.Ignore

@Entity(
    tableName = "pokemon_types",
    primaryKeys = ["pokemon_id", "type_id"],
    foreignKeys = [
        ForeignKey(entity = EntityPokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("pokemon_id"), onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EntityType::class, parentColumns = arrayOf("id"), childColumns = arrayOf("type_id"), onDelete = ForeignKey.CASCADE)
    ]
)
class PokemonTypesJoin(val pokemon_id: Int, val type_id: Int)

data class PokemonIdTypesId(
    @ColumnInfo(name = "pokemon_id") val pokemon_id: Int,
    @ColumnInfo(name = "type_ids") val type_ids: String,
) {
    @Ignore
    val type_id_list: List<PokemonType> = type_ids.split(",").map { PokemonType.values()[it.toInt()] }
}

@Dao
interface PokemonTypesDao {
    @Query(
        "SELECT pokemon_id, GROUP_CONCAT(type_id) as type_ids FROM pokemon_types " +
                "GROUP BY pokemon_id " +
                "ORDER BY pokemon_id ASC"
    )
    fun findPokemonsTypes(): List<PokemonIdTypesId>

    @Query(
        "SELECT type_id FROM pokemon_types " +
                "WHERE pokemon_id = :pokemon_id " +
                "ORDER BY type_id ASC"
    )
    fun findPokemonTypes(pokemon_id: Int): List<Int>
}