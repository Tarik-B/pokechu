package fr.amazer.pokechu.data

import androidx.room.*
import fr.amazer.pokechu.managers.DatabaseManager


//--------------------------------------------------------------------------------------------------
// Database
//--------------------------------------------------------------------------------------------------
@Database(
    entities = [
        Pokemon::class, Region::class, Type::class,
        PokemonRegionsJoin::class, PokemonEvolutionsJoin::class, PokemonTypesJoin::class
    ],
    version = 2
)
abstract class PokechuDatabase: RoomDatabase() {
    abstract fun getPokemonsDao(): PokemonsDao
    abstract fun getRegionsDao(): RegionsDao

    abstract fun getPokemonRegionsDao(): PokemonRegionsDao
    abstract fun getPokemonEvolutionsDao(): PokemonEvolutionsDao
    abstract fun getPokemonTypesDao(): PokemonTypesDao
}

//--------------------------------------------------------------------------------------------------
// Pokemons
//--------------------------------------------------------------------------------------------------
//@Entity(tableName = "pokemons")
//class Pokemons(@PrimaryKey val id: Int, val name: String)
@Entity(tableName = "pokemons")
data class Pokemon(
    @PrimaryKey
    val id: Int,
    val name: String,
    val height: Float,
    val weight: Float
)
@Dao
interface PokemonsDao {
    @Query("SELECT * FROM pokemons")
    fun findAll(): List<Pokemon>

    @Query("SELECT * FROM pokemons WHERE id = :id")
    fun findById(id: Int): Pokemon?

    @Query("SELECT id FROM pokemons")
    fun findAllIds(): List<Int>

//    @Query("SELECT * FROM pokemons WHERE id IN (:ids)")
//    fun findAllByIds(ids: IntArray): List<Pokemons>
//    @Query("SELECT * FROM pokemons WHERE name LIKE :searched_name")
//    fun findByName(searched_name: String): Pokemons
}

//--------------------------------------------------------------------------------------------------
// Regions
//--------------------------------------------------------------------------------------------------
//@Entity(tableName = "regions")
//class Regions(@field:PrimaryKey val id: Int, val name: String)
@Entity(tableName = "regions")
data class Region(
    @PrimaryKey
    val id: Int,
    val name: String,
)

@Dao
interface RegionsDao {
    @Query("SELECT * FROM regions")
    fun findAll(): List<Region>

//    @Query("SELECT id FROM regions")
//    fun findAllIds(): List<Int>

//    @Query("SELECT * FROM regions WHERE id IN (:ids)")
//    fun findAllByIds(ids: IntArray): List<Regions>
//    @Query("SELECT * FROM regions WHERE name LIKE :searched_name")
//    fun findByName(searched_name: String): Regions

//    @Query("SELECT * FROM regions WHERE pokemon_id=:pokemon_id")
//    fun findRegionsForPokemon(pokemon_id: Int): List<Regions?>?
}

//--------------------------------------------------------------------------------------------------
// Types
//--------------------------------------------------------------------------------------------------
@Entity(tableName = "types")
data class Type(
    @PrimaryKey
    val id: Int,
    val name: String,
)
// No dao needed, table not queried, only used in foreign keys

//--------------------------------------------------------------------------------------------------
// Pokemon Regions
//--------------------------------------------------------------------------------------------------
@Entity(
    tableName = "pokemon_regions",
    primaryKeys = ["pokemon_id", "region_id"],
    foreignKeys = [
        ForeignKey(entity = Pokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("pokemon_id"), onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Region::class, parentColumns = arrayOf("id"), childColumns = arrayOf("region_id"), onDelete = ForeignKey.CASCADE)
    ]
)
class PokemonRegionsJoin(val pokemon_id: Int, val region_id: Int, val local_id: Int)

data class NationalIdLocalId(
    @ColumnInfo(name = "pokemon_id") val pokemon_id: Int,
    @ColumnInfo(name = "local_id") val local_id: Int,
)

@Dao
public interface PokemonRegionsDao {
    @Query(
        "SELECT pokemon_id, local_id FROM regions " +
        "INNER JOIN pokemon_regions ON regions.id=pokemon_regions.region_id " +
        "WHERE pokemon_regions.region_id=:region_id "+
        "ORDER BY local_id ASC"
    )
    fun findPokemonRegions(region_id: Int): List<NationalIdLocalId>

    @Query(
        "SELECT pokemon_id FROM pokemon_regions " +
        "WHERE region_id = :region_id AND local_id = :local_id"
    )
    fun localToNationalId(region_id: Int, local_id: Int): Int

//
//
//
//;


//    @Query(
//        "SELECT pokemon_regions.region_id, pokemon_regions.local_id FROM pokemons " +
//        "JOIN pokemon_regions ON pokemon_regions.pokemon_id = pokemons.id " +
//        "WHERE pokemons.id=:pokemon_id")
//    fun findLocalIdsByPokemon(pokemon_id: Int):List<RegionIdLocalId>
}

//--------------------------------------------------------------------------------------------------
// Pokemon Evolution
//--------------------------------------------------------------------------------------------------
@Entity(
    tableName = "pokemon_evolutions",
    primaryKeys = ["base_id", "evolved_id"],
    foreignKeys = [
        ForeignKey(entity = Pokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("base_id"), onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Pokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("evolved_id"), onDelete = ForeignKey.CASCADE)
    ]
)
class PokemonEvolutionsJoin(val base_id: Int, val evolved_id: Int, val condition_raw: String, val condition_encoded: String)

data class BaseIdEvolvedIdCondition(
    @ColumnInfo(name = "base_id") val base_id: Int,
    @ColumnInfo(name = "evolved_id") val evolved_id: Int,
    @ColumnInfo(name = "condition_encoded") val condition_encoded: String
) {
//    @Ignore
//    val conditions: List<PokemonType> = type_ids.split(",").map { PokemonType.values()[it.toInt()] }
}

@Dao
public interface PokemonEvolutionsDao {
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
    fun findPokemonEvolutionRoot(pokemon_id: Int): Int

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
    fun findPokemonEvolutions(pokemon_id: Int): List<BaseIdEvolvedIdCondition>
}

//--------------------------------------------------------------------------------------------------
// Pokemon Types
//--------------------------------------------------------------------------------------------------
@Entity(
    tableName = "pokemon_types",
    primaryKeys = ["pokemon_id", "type_id"],
    foreignKeys = [
        ForeignKey(entity = Pokemon::class, parentColumns = arrayOf("id"), childColumns = arrayOf("pokemon_id"), onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Type::class, parentColumns = arrayOf("id"), childColumns = arrayOf("type_id"), onDelete = ForeignKey.CASCADE)
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
public interface PokemonTypesDao {
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

//data class PokemonWithRegions(
//    @Embedded val pokemon: Pokemons,
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "id",
//        associateBy = Junction(Pokemon_Regions::class)
//    )
//    val regions: List<Regions>
//)

//data class RegionIdLocalId(
//    @ColumnInfo(name = "id") val region_id: Int,
//    @ColumnInfo(name = "local_id") val local_id: Int
//)
//
//@Dao
//interface PokemonRegionsDao {
//    @Query("SELECT r.id, pr.local_id FROM pokemons p " +
//            "JOIN pokemon_regions pr ON pr.pokemon_id = p.id " +
//            "JOIN regions r ON pr.region_id = r.id " +
//            "WHERE p.id = pokemon_id")
//    fun getPokemonRegions(pokemon_id: Int): List<RegionIdLocalId>

//    @Transaction
//    @Query("SELECT * FROM pokemons")
//    fun getPokemonRegions(): List<PokemonWithRegions>
//
//    @Query("SELECT * FROM pokemons INNER JOIN pokemon_regions ON repo.id=user_repo_join.repoId WHERE user_repo_join.userId=:userId")
//    List<Repo> getRepositoriesForUsers(final int userId);
//}

