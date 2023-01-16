package fr.amazer.pokechu.database.joins

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Query
import fr.amazer.pokechu.database.entities.EntityPokemon
import fr.amazer.pokechu.database.entities.EntityRegion
import fr.amazer.pokechu.database.entities.NationalIdLocalId

@Entity(
    tableName = "pokemon_regions",
    primaryKeys = ["pokemon_id", "region_id"],
    foreignKeys = [
        ForeignKey(entity = EntityPokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("pokemon_id"), onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EntityRegion::class, parentColumns = arrayOf("id"), childColumns = arrayOf("region_id"), onDelete = ForeignKey.CASCADE)
    ]
)
class JoinPokemonRegions(val pokemon_id: Int, val region_id: Int, val local_id: Int)

@Dao
interface DaoPokemonRegions {
    @Query(
        "SELECT pokemon_id, local_id FROM regions " +
        "INNER JOIN pokemon_regions ON regions.id=pokemon_regions.region_id " +
        "WHERE :region_id=0 OR pokemon_regions.region_id=:region_id "+
        "ORDER BY local_id ASC"
    )
    fun findPokemonRegions(region_id: Int): LiveData<List<NationalIdLocalId>>

    @Query(
        "SELECT pokemon_id, local_id FROM regions " +
        "INNER JOIN pokemon_regions ON regions.id=pokemon_regions.region_id " +
        "WHERE pokemon_regions.region_id IN (:region_ids) "+
        "ORDER BY local_id ASC"
    )
    fun findPokemonRegions(region_ids: List<Int>): LiveData<List<NationalIdLocalId>>

    @Query(
        "SELECT pokemon_id FROM pokemon_regions " +
        "WHERE region_id = :region_id AND local_id = :local_id"
    )
    fun localToNationalId(region_id: Int, local_id: Int): LiveData<Int>
}
