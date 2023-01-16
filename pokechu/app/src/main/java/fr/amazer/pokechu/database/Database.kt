package fr.amazer.pokechu.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import fr.amazer.pokechu.ApplicationExecutors
import fr.amazer.pokechu.database.entities.*
import fr.amazer.pokechu.database.joins.*
import fr.amazer.pokechu.enums.*

@Database(
    entities = [
        EntityPokemon::class, EntityRegion::class, EntityType::class, EntityGame::class,
        JoinPokemonRegions::class, JoinPokemonEvolutions::class, JoinPokemonTypes::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PokechuDatabase: RoomDatabase() {
    abstract fun getPokemonsDao(): DaoPokemons
    abstract fun getRegionsDao(): DaoRegions
    abstract fun getGamesDao(): DaoGames

    abstract fun getPokemonRegionsDao(): DaoPokemonRegions
    abstract fun getPokemonEvolutionsDao(): DaoPokemonEvolutions
    abstract fun getPokemonTypesDao(): DaoPokemonTypes

    private val mIsDatabaseCreated = MutableLiveData<Boolean>()

    /**
     * Check whether the database already exists and expose it via [.getDatabaseCreated]
     */
    private fun updateDatabaseCreated(context: Context) {
        if (context.getDatabasePath(DATABASE_NAME).exists())
            setDatabaseCreated()
    }
    private fun setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true)
    }
    fun getDatabaseCreated(): LiveData<Boolean?>? {
        return mIsDatabaseCreated
    }

    companion object {
        private var sInstance: PokechuDatabase? = null

        @VisibleForTesting
        val DATABASE_NAME = "pokechu.db"
        fun getInstance(context: Context, executors: ApplicationExecutors): PokechuDatabase? {
            if (sInstance == null) {
                synchronized(PokechuDatabase::class.java) {
                    if (sInstance == null) {
                        sInstance = buildDatabase(context.getApplicationContext(), executors)
                        sInstance!!.updateDatabaseCreated(context.getApplicationContext())
                    }
                }
            }
            return sInstance
        }

        /**
         * Build the database. [Builder.build] only sets up the database configuration and
         * creates a new instance of the database.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context, executors: ApplicationExecutors): PokechuDatabase {
            return Room.databaseBuilder(
                appContext,
                PokechuDatabase::class.java,
                DATABASE_NAME
            )
            .createFromAsset("db.sqlite")
            .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        executors.diskIO().execute {

                            // Add a delay to simulate a long-running operation
//                            PokechuDatabase.Companion.addDelay()
                            // Generate the data for pre-population
                            val database: PokechuDatabase? =
                                getInstance(
                                    appContext,
                                    executors
                                )
                            // notify that the database was created and it's ready to be used
                            if (database != null) {
                                database.setDatabaseCreated()
                            }
                        }
                    }
                })
                .build()
        }
    }
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

