package fr.amazer.pokechu.enums

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
    @Query("SELECT * FROM regions")
    fun findAll(): List<EntityRegion>
}