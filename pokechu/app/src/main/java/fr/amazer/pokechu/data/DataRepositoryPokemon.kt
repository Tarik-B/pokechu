package fr.amazer.pokechu.data

import androidx.lifecycle.LiveData
import fr.amazer.pokechu.enums.EntityPokemon
import fr.amazer.pokechu.enums.PokechuDatabase

class DataRepositoryPokemon private constructor(
    private val database: PokechuDatabase
) {

    fun getPokemon(pokemonId: Int): LiveData<EntityPokemon> {
        return database.getPokemonsDao().findById(pokemonId)
    }

    fun getPokemonTypes(pokemonId: Int): LiveData<List<Int>> {
        return database.getPokemonTypesDao().findPokemonTypes(pokemonId)
    }

    companion object {
        private var sInstance: DataRepositoryPokemon? = null
        fun getInstance(database: PokechuDatabase?): DataRepositoryPokemon? {
            if (sInstance == null) {
                synchronized(DataRepositoryPokemon::class.java) {
                    if (sInstance == null) {
                        sInstance =
                            database?.let { DataRepositoryPokemon(it) }
                    }
                }
            }
            return sInstance
        }
    }
}