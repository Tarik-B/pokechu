package fr.amazer.pokechu.enums

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "pokemons")
data class EntityPokemon(
    @PrimaryKey
    val id: Int,
    val name: String,
    val height: Float,
    val weight: Float
)

@Dao
interface DaoPokemons {
    @Query("SELECT * FROM pokemons")
    fun findAll(): List<EntityPokemon>

    @Query("SELECT * FROM pokemons WHERE id = :id")
    fun findById(id: Int): EntityPokemon?

    @Query("SELECT id FROM pokemons")
    fun findAllIds(): List<Int>
}