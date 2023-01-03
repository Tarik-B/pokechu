package fr.amazer.pokechu.database.joins

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Query
import androidx.room.ColumnInfo
import fr.amazer.pokechu.database.entities.EntityPokemon

@Entity(
    tableName = "pokemon_evolutions",
    primaryKeys = ["base_id", "evolved_id"],
    foreignKeys = [
        ForeignKey(entity = EntityPokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("base_id"), onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EntityPokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("evolved_id"), onDelete = ForeignKey.CASCADE)
    ]
)
class PokemonEvolutionsJoin(val base_id: Int, val evolved_id: Int, val condition_raw: String, val condition_encoded: String)

data class BaseIdEvolvedIdCondition(
    @ColumnInfo(name = "base_id") val base_id: Int,
    @ColumnInfo(name = "evolved_id") val evolved_id: Int,
    @ColumnInfo(name = "condition_encoded") val condition_encoded: String
) {
}

@Dao
interface PokemonEvolutionsDao {
    @Query(
        "WITH RECURSIVE evolution_root AS ("+
            "SELECT pe.base_id, pe.evolved_id, 0 as depth "+
            "FROM pokemon_evolutions pe "+
            "WHERE pe.evolved_id = :pokemon_id "+
            "UNION ALL "+
            "SELECT pe.base_id, pe.evolved_id, evolution_root.depth + 1 "+
            "FROM evolution_root "+
            "JOIN pokemon_evolutions pe ON pe.evolved_id = evolution_root.base_id "+
        ")"+
        "SELECT base_id "+
        "FROM evolution_root "+
        "ORDER BY depth DESC LIMIT 1;"
    )
    fun findPokemonEvolutionRoot(pokemon_id: Int): LiveData<Int>

    @Query(
        "WITH RECURSIVE evolution_chain AS ( "+
            "SELECT pe.evolved_id, pe.base_id, pe.condition_encoded "+
            "FROM pokemon_evolutions pe "+
            "WHERE pe.base_id = :pokemon_id "+
            "UNION ALL "+
            "SELECT pe.evolved_id, pe.base_id, pe.condition_encoded "+
            "FROM evolution_chain "+
            "JOIN pokemon_evolutions pe ON pe.base_id = evolution_chain.evolved_id "+
        ")"+
        "SELECT base_id, evolved_id, condition_encoded FROM evolution_chain "+
        "JOIN pokemons p ON p.id = evolution_chain.evolved_id;"
    )
    fun findPokemonEvolutions(pokemon_id: Int): LiveData<List<BaseIdEvolvedIdCondition>>
}