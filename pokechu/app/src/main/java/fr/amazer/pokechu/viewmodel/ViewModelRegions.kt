package fr.amazer.pokechu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryRegions
import fr.amazer.pokechu.database.entities.EntityRegion

class ViewModelRegions(application: Application) : AndroidViewModel(application) {
    private val repository: DataRepositoryRegions

    init {
        repository = (application as PokechuApplication).getRepositoryRegions()!!
    }

    fun getRegions(): LiveData<List<EntityRegion>> {
        return repository.getRegions()
    }
}