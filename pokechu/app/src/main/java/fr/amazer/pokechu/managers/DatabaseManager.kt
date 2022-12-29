package fr.amazer.pokechu.managers

import android.content.Context
import androidx.room.Room
import fr.amazer.pokechu.enums.*


object DatabaseManager {
    private lateinit var database: PokechuDatabase
    private lateinit var pokemonsDao: DaoPokemons
    private lateinit var regionsDao: DaoRegions
    private lateinit var pokemonRegionDao: PokemonRegionsDao
    private lateinit var pokemonEvolutionsDao: PokemonEvolutionsDao
    private lateinit var pokemonTypesDao: PokemonTypesDao

    fun with(context: Context) {
        database = Room.databaseBuilder(context,
            PokechuDatabase::class.java, "pokechu.db")
            .createFromAsset("db.sqlite")
            .fallbackToDestructiveMigration()
            .build()

        pokemonsDao = database.getPokemonsDao()
        regionsDao = database.getRegionsDao()
        pokemonRegionDao = database.getPokemonRegionsDao()
        pokemonEvolutionsDao = database.getPokemonEvolutionsDao()
        pokemonTypesDao = database.getPokemonTypesDao()
    }

    fun findPokemonIds(): List<Int> { return pokemonsDao.findAllIds() }
    fun findPokemonsCount(): Int { return pokemonsDao.findAll().count() } // TODO Replace by COUNT query
    fun findPokemonById(id: Int): EntityPokemon? { return pokemonsDao.findById(id) }

    fun findRegions(): List<EntityRegion> { return regionsDao.findAll() }

    fun findPokemonRegions(region_id: Int): List<NationalIdLocalId> { return pokemonRegionDao.findPokemonRegions(region_id) }
    fun localToNationalId(region_id: Int, local_id: Int): Int { return pokemonRegionDao.localToNationalId(region_id, local_id) }

    fun findPokemonEvolutionRoot(pokemon_id: Int): Int { return pokemonEvolutionsDao.findPokemonEvolutionRoot(pokemon_id) }
    fun findPokemonEvolutions(pokemon_id: Int): List<BaseIdEvolvedIdCondition> { return pokemonEvolutionsDao.findPokemonEvolutions(pokemon_id) }

    fun findPokemonsTypes(): List<PokemonIdTypesId> { return pokemonTypesDao.findPokemonsTypes() }
    fun findPokemonTypes(pokemon_id: Int): List<Int> { return pokemonTypesDao.findPokemonTypes(pokemon_id) }
}