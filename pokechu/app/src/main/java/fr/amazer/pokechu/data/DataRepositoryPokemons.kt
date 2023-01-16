package fr.amazer.pokechu.data

import androidx.lifecycle.LiveData
import fr.amazer.pokechu.database.PokechuDatabase
import fr.amazer.pokechu.database.entities.NationalIdLocalId
import fr.amazer.pokechu.database.joins.PokemonIdTypeIds

class DataRepositoryPokemons private constructor(
    private val database: PokechuDatabase
) {

    fun getPokemonsIds(): LiveData<List<NationalIdLocalId>> {
        return database.getPokemonsDao().findAllNationalLocalIds()
    }
    fun getPokemonsIdsByRegion(regionId: Int): LiveData<List<NationalIdLocalId>> {
        return database.getPokemonRegionsDao().findPokemonRegions(regionId)
    }
    fun getPokemonsTypes(): LiveData<List<PokemonIdTypeIds>> {
        return database.getPokemonTypesDao().findPokemonsTypes()
    }
    fun getPokemonsTypesByRegion(regionId: Int): LiveData<List<PokemonIdTypeIds>> {
        return database.getPokemonTypesDao().findPokemonsTypesByRegion(regionId)
    }
    fun localToNationalId(region_id: Int, local_id: Int): LiveData<Int> {
        return database.getPokemonRegionsDao().localToNationalId(region_id, local_id)
    }

    companion object {
        private var sInstance: DataRepositoryPokemons? = null
        fun getInstance(database: PokechuDatabase): DataRepositoryPokemons? {
            if (sInstance == null) {
                synchronized(DataRepositoryPokemons::class.java) {
                    if (sInstance == null) {
                        sInstance = DataRepositoryPokemons(database)
                    }
                }
            }
            return sInstance
        }
    }
}