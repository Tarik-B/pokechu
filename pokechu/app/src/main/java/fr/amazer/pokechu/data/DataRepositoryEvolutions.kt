package fr.amazer.pokechu.data

import androidx.lifecycle.LiveData
import fr.amazer.pokechu.enums.BaseIdEvolvedIdCondition
import fr.amazer.pokechu.enums.PokechuDatabase

class DataRepositoryEvolutions private constructor(
    private val database: PokechuDatabase
) {

    fun getEvolutionRoot(pokemonId: Int): LiveData<Int> {
        return database.getPokemonEvolutionsDao().findPokemonEvolutionRoot(pokemonId)
    }

    fun getEvolutionChain(rootId: Int): LiveData<List<BaseIdEvolvedIdCondition>> {
        return database.getPokemonEvolutionsDao().findPokemonEvolutions(rootId)
    }

    companion object {
        private var sInstance: DataRepositoryEvolutions? = null
        fun getInstance(database: PokechuDatabase?): DataRepositoryEvolutions? {
            if (sInstance == null) {
                synchronized(DataRepositoryEvolutions::class.java) {
                    if (sInstance == null) {
                        sInstance =
                            database?.let { DataRepositoryEvolutions(it) }
                    }
                }
            }
            return sInstance
        }
    }
}