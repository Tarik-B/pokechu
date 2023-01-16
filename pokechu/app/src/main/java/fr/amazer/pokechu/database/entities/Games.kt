package fr.amazer.pokechu.database.entities

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Query

@Entity(
    tableName = "games",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(entity = EntityRegion::class, parentColumns = arrayOf("id"), childColumns = arrayOf("region_id"), onDelete = ForeignKey.CASCADE)
    ]
)
class EntityGame(val id: Int, val name: String, val region_id: Int)

@Dao
interface DaoGames {
    @Query("SELECT id FROM games")
    fun findAllIds(): List<Int>

    @Query("SELECT * FROM games")
    fun findAll(): LiveData<List<EntityGame>>
}
