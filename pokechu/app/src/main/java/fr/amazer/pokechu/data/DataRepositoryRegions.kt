package fr.amazer.pokechu.data

import androidx.lifecycle.LiveData
import fr.amazer.pokechu.database.entities.EntityRegion
import fr.amazer.pokechu.database.PokechuDatabase

class DataRepositoryRegions private constructor(
    private val database: PokechuDatabase
) {

    fun getRegions(): LiveData<List<EntityRegion>> {
        return database.getRegionsDao().findAll()
    }

    companion object {
        private var sInstance: DataRepositoryRegions? = null
        fun getInstance(database: PokechuDatabase): DataRepositoryRegions? {
            if (sInstance == null) {
                synchronized(DataRepositoryRegions::class.java) {
                    if (sInstance == null) {
                        sInstance = DataRepositoryRegions(database)
                    }
                }
            }
            return sInstance
        }
    }
}