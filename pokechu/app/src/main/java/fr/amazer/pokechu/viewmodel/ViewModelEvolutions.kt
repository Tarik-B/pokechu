package fr.amazer.pokechu.viewmodel

import android.app.Application
import androidx.lifecycle.*
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryEvolutions
import fr.amazer.pokechu.database.joins.BaseIdEvolvedIdCondition

class ViewModelEvolutions(application: Application) : AndroidViewModel(application) {
    private var repository: DataRepositoryEvolutions

    init {
        repository = (application as PokechuApplication).getRepositoryEvolutions()!!
    }

    fun getEvolutionRoot(pokemonId: Int): LiveData<Int> {
        return repository.getEvolutionRoot(pokemonId)
    }

    fun getEvolutions(rootId: Int): LiveData<List<BaseIdEvolvedIdCondition>> {
        return repository.getEvolutionChain(rootId)
    }
}