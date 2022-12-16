package fr.amazer.pokechu.managers

import android.content.Context
import androidx.room.Room
import fr.amazer.pokechu.data.*


object DatabaseManager {
    private lateinit var database: PokechuDatabase
    private lateinit var pokemonsDao: PokemonsDao
    private lateinit var regionsDao: RegionsDao
    private lateinit var pokemonRegionDao: PokemonRegionsDao
    private lateinit var pokemonEvolutionsDao: PokemonEvolutionsDao

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
    }


    fun findPokemons(): List<Pokemons> { return pokemonsDao.findAll() }
    fun findPokemonIds(): List<Int> { return pokemonsDao.findAllIds() }
    fun findPokemonsCount(): Int { return pokemonsDao.findAll().count() } // TODO Replace by COUNT query
    fun findPokemonById(id: Int): Pokemons? { return pokemonsDao.findById(id) }
//    fun getPokemonsByIds(ids: IntArray): List<Pokemons> { return pokemonsDao.findAllByIds(ids) }
//    fun getPokemonByName(pokemon_name: String): Pokemons { return pokemonsDao.findByName(pokemon_name) }

    fun findRegions(): List<Regions> { return regionsDao.findAll() }
//    fun findRegionIds(): List<Int> { return regionsDao.findAllIds() }
//    fun getRegionsByIds(ids: IntArray): List<Regions> { return regionsDao.getAllByIds(ids) }
//    fun getRegionByName(region_name: String): Regions { return regionsDao.findByName(region_name) }

    fun findLocalIdsByRegion(region_id: Int): List<NationalIdLocalId> { return pokemonRegionDao.findLocalIdsByRegion(region_id) }
//    fun getRegionsByPokemon(pokemon_id: Int):List<Regions> { return pokemonRegionDao.getRegionsByPokemon(pokemon_id) }

    fun findPokemonEvolutionRoot(pokemon_id: Int): Int { return pokemonEvolutionsDao.findPokemonEvolutionRoot(pokemon_id) }
    fun findPokemonEvolutions(pokemon_id: Int): List<BaseIdEvolvedIdCondition> { return pokemonEvolutionsDao.findPokemonEvolutions(pokemon_id) }
}