package fr.amazer.pokechu.enums

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "types")
data class EntityType(
    @PrimaryKey
    val id: Int,
    val name: String,
)
// No dao needed, table not queried, only used in foreign keys
