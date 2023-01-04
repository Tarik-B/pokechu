package fr.amazer.pokechu.viewmodel

import android.app.Application
import androidx.lifecycle.*
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryEvolutions
import fr.amazer.pokechu.database.joins.BaseIdEvolvedIdCondition
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.managers.settings.MultiPrefixedLivePreference
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.utils.ConditionUtils

class ViewModelEvolutions(application: Application,
private val pokemonId: Int
) : AndroidViewModel(application) {
    private var repository: DataRepositoryEvolutions

    private val evolutionChain: MediatorLiveData<List<BaseIdEvolvedIdCondition>>
    private val evolutionData: MediatorLiveData<List<ViewModelEvolutionData>>

    init {
        repository = (application as PokechuApplication).getRepositoryEvolutions()!!

        fun fillEvolutionData(
            data: ViewModelEvolutionData,
            evolution: BaseIdEvolvedIdCondition?
        ) {
            if (evolution != null) {
                data.baseId = evolution.base_id
                data.evolutionConditions = ConditionUtils.parseEncodedCondition(evolution.condition_encoded)
            }

            data.isDiscovered = SettingsManager.getSetting(SettingType.DISCOVERED, data.pokemonId.toString())
            data.localizedName = LocalizationManager.getPokemonName(application.applicationContext, data.pokemonId).toString()
            data.thumbnailPath = AssetUtils.getPokemonThumbnailPath(data.pokemonId)
        }

        evolutionChain = Transformations.switchMap(repository.getEvolutionRoot(pokemonId)) { rootId ->
            val id = if (rootId != 0) rootId else pokemonId
            Transformations.switchMap(repository.getEvolutionChain(id)) { evolutions ->
                MediatorLiveData(evolutions)
            }
        } as MediatorLiveData<List<BaseIdEvolvedIdCondition>>


        fun combineEvolutionData(
            evolutionLiveData: MediatorLiveData<List<BaseIdEvolvedIdCondition>>,
            discoveredLiveData: MultiPrefixedLivePreference<Boolean>
        ): List<ViewModelEvolutionData>? {

            val evolutions = evolutionLiveData.value
            val discoveredSetting = discoveredLiveData.value

            // Don't send a success until we have all results
            if (evolutions == null || discoveredSetting == null) {
                return null
            }

            val evolutionData = mutableListOf<ViewModelEvolutionData>()

            val rootId = if (evolutions.isNotEmpty()) evolutions[0].base_id else pokemonId
            val rootData = ViewModelEvolutionData(rootId)
            fillEvolutionData(rootData, null)
            evolutionData.add(rootData)

            evolutions.forEach { evolution ->
                val nodeData = ViewModelEvolutionData(evolution.evolved_id)
                fillEvolutionData(nodeData, evolution)
                evolutionData.add(nodeData)
            }

            return evolutionData
        }

        val discoveredSetting = SettingsManager.getLivePrefixedSettings<Boolean>(SettingType.DISCOVERED)

        evolutionData = MediatorLiveData()
        fun checkAndCombineEvolutionData() {
            val dataList = combineEvolutionData(evolutionChain, discoveredSetting)
            if (dataList != null)
                evolutionData.postValue(dataList!!)
        }
        evolutionData.addSource(evolutionChain) { _ -> checkAndCombineEvolutionData() }
        evolutionData.addSource(discoveredSetting) { _ -> checkAndCombineEvolutionData() }
    }

    fun getEvolutionData(): LiveData<List<ViewModelEvolutionData>> {
        return evolutionData
    }

    class Factory(private val application: Application, private val pokemonId: Int) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewModelEvolutions(application, pokemonId) as T
        }
    }
}