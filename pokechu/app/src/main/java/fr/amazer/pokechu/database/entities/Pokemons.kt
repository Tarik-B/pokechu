package fr.amazer.pokechu.database.entities

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "pokemons")
data class EntityPokemon(
    @PrimaryKey
    val id: Int,
    val name: String,
    val height: Float,
    val weight: Float
)

data class NationalIdLocalId(
    @ColumnInfo(name = "pokemon_id") val pokemon_id: Int,
    @ColumnInfo(name = "local_id") val local_id: Int,
)

@Dao
interface DaoPokemons {
    @Query("SELECT id FROM pokemons")
    fun findAllIds(): List<Int>

    @Query("SELECT COUNT(id) FROM pokemons")
    fun getCount(): LiveData<Int>

    @Query("SELECT * FROM pokemons WHERE id = :id")
    fun findById(id: Int): LiveData<EntityPokemon>

    @Query("SELECT * FROM pokemons WHERE id = :id")
    fun findByIdNoFlow(id: Int): EntityPokemon

    @Query("SELECT id as pokemon_id, id as local_id FROM pokemons")
    fun findAllNationalLocalIds(): LiveData<List<NationalIdLocalId>>
}