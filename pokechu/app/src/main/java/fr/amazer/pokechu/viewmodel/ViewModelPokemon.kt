package fr.amazer.pokechu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryPokemon
import fr.amazer.pokechu.database.entities.EntityPokemon

class ViewModelPokemon(application: Application) : AndroidViewModel(application) {
    private val repository: DataRepositoryPokemon

    init {
        repository = (application as PokechuApplication).getRepositoryPokemon()!!
    }

    fun getPokemon(pokemonId: Int): LiveData<EntityPokemon> {
        return repository.getPokemon(pokemonId)
    }

    fun getPokemonTypes(pokemonId: Int): LiveData<List<Int>> {
        return repository.getPokemonTypes(pokemonId)
    }

}