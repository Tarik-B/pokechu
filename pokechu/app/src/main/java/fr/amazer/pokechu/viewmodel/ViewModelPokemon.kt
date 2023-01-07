package fr.amazer.pokechu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryPokemon
import fr.amazer.pokechu.data.DataRepositoryPreferences
import fr.amazer.pokechu.data.preferences.MultiPrefixedLivePreference
import fr.amazer.pokechu.database.entities.EntityPokemon
import fr.amazer.pokechu.enums.PreferenceType

class ViewModelPokemon(application: Application) : AndroidViewModel(application) {
    private val repository: DataRepositoryPokemon
    private var repositoryPreferences: DataRepositoryPreferences

    // TODO should not be initialized to 0 and initial id should be passed as constructor param
    private var pokemonId: MutableLiveData<Int> = MutableLiveData()
    private val pokemon: LiveData<EntityPokemon>
    private val pokemonTypes: LiveData<List<Int>>

    init {
        (application as PokechuApplication)
        repository = application.getRepositoryPokemon()!!
        repositoryPreferences = application.getRepositoryPreference()!!

        pokemon = Transformations.switchMap(pokemonId) { id ->
            repository.getPokemon(id)
        }
        pokemonTypes = Transformations.switchMap(pokemonId) { id ->
            repository.getPokemonTypes(id)
        }
    }

    fun getPokemonId(): LiveData<Int> {
        return pokemonId
    }
    fun setPokemonId(id: Int) {
        pokemonId.postValue(id)
    }

    fun getPokemon(): LiveData<EntityPokemon> {
        return pokemon
    }
    fun getPokemonTypes(): LiveData<List<Int>> {
        return pokemonTypes
    }

    fun getPokemonsDiscovered(): MultiPrefixedLivePreference<Boolean> {
        return repositoryPreferences.getLivePrefixedSettings<Boolean>(PreferenceType.DISCOVERED)
    }

//    class Factory(private val application: PokechuApplication, private val initialId: Int) :
//        ViewModelProvider.NewInstanceFactory() {
//
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            return ViewModelPokemon(application, initialId) as T
//        }
//    }
}