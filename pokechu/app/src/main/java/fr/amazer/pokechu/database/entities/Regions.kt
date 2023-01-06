package fr.amazer.pokechu.database.entities

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "regions")
data class EntityRegion(
    @PrimaryKey
    val id: Int,
    val name: String,
)

@Dao
interface DaoRegions {
    @Query("SELECT id FROM regions")
    fun findAllIds(): List<Int>

    @Query("SELECT * FROM regions")
    fun findAll(): LiveData<List<EntityRegion>>
}