package fr.amazer.pokechu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryPreferences
import fr.amazer.pokechu.data.DataRepositoryRegions
import fr.amazer.pokechu.data.preferences.LivePreference
import fr.amazer.pokechu.database.entities.EntityGame
import fr.amazer.pokechu.database.entities.EntityRegion
import fr.amazer.pokechu.enums.PreferenceType

class ViewModelRegions(application: Application) : AndroidViewModel(application) {
    private val repository: DataRepositoryRegions
    private val repositoryPreferences: DataRepositoryPreferences

    init {
        (application as PokechuApplication)
        repository = application.getRepositoryRegions()!!
        repositoryPreferences = application.getRepositoryPreference()!!
    }

    fun getRegions(): LiveData<List<EntityRegion>> {
        return repository.getRegions()
    }
    fun getGames(): LiveData<List<EntityGame>> {
        return repository.getGames()
    }
    fun getSelectedRegion(): LivePreference<Int> {
        return repositoryPreferences.getLiveSetting(PreferenceType.SELECTED_REGION)
    }
}