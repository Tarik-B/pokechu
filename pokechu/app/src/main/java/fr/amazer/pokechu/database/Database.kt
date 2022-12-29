package fr.amazer.pokechu.enums

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        EntityPokemon::class, EntityRegion::class, EntityType::class,
        PokemonRegionsJoin::class, PokemonEvolutionsJoin::class, PokemonTypesJoin::class
    ],
    version = 2
)
abstract class PokechuDatabase: RoomDatabase() {
    abstract fun getPokemonsDao(): DaoPokemons
    abstract fun getRegionsDao(): DaoRegions

    abstract fun getPokemonRegionsDao(): PokemonRegionsDao
    abstract fun getPokemonEvolutionsDao(): PokemonEvolutionsDao
    abstract fun getPokemonTypesDao(): PokemonTypesDao
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

