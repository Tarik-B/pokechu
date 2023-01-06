package fr.amazer.pokechu.viewmodel

import android.app.Application
import androidx.lifecycle.*
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryEvolutions
import fr.amazer.pokechu.data.DataRepositoryPreferences
import fr.amazer.pokechu.data.preferences.MultiPrefixedLivePreference
import fr.amazer.pokechu.database.joins.BaseIdEvolvedIdCondition
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.utils.ConditionUtils
import fr.amazer.pokechu.utils.EvolutionConditionData

class ViewModelEvolutions(application: Application) : AndroidViewModel(application) {
    private var repository: DataRepositoryEvolutions
    private var repositoryPreferences: DataRepositoryPreferences

    // TODO should not be initialized to 0 and initial id should be passed as constructor param
    private var pokemonId: MutableLiveData<Int> = MutableLiveData(0)
    private val evolutionChain: MediatorLiveData<List<BaseIdEvolvedIdCondition>>
    private val evolutionData: MediatorLiveData<List<ViewModelEvolutionData>>

    init {
        (application as PokechuApplication)
        repository = application.getRepositoryEvolutions()!!
        repositoryPreferences = application.getRepositoryPreference()!!

        evolutionChain = Transformations.switchMap(pokemonId) { id ->
            Transformations.switchMap(repository.getEvolutionRoot(id)) { rootId ->
                val actualRootId = if (rootId != 0) rootId else id
                Transformations.switchMap(repository.getEvolutionChain(actualRootId)) { evolutions ->
                    MediatorLiveData(evolutions)
                }
            }
        } as MediatorLiveData<List<BaseIdEvolvedIdCondition>>


        fun combineEvolutionData(
            evolutionLiveData: LiveData<List<BaseIdEvolvedIdCondition>>,
            discoveredLiveData: MultiPrefixedLivePreference<Boolean>
        ): List<ViewModelEvolutionData>? {

            val pokemonId = pokemonId.value
            val evolutions = evolutionLiveData.value
            val discoveredSetting = discoveredLiveData.value

            // Don't send a success until we have all results
            if (pokemonId == null || evolutions == null || discoveredSetting == null) {
                return null
            }

            if ( pokemonId == 0)
                return null

            val evolutionData = mutableListOf<ViewModelEvolutionData>()

            fun buildEvolutionData(id: Int, evolution: BaseIdEvolvedIdCondition?): ViewModelEvolutionData {
                var baseId: Int? = null
                var evolutionConditions: EvolutionConditionData? = null
                if (evolution != null) {
                    baseId = evolution.base_id
                    evolutionConditions = ConditionUtils.parseEncodedCondition(evolution.condition_encoded)
                }

                val isDiscovered = SettingsManager.getSetting<Boolean>(PreferenceType.DISCOVERED, id.toString())
                val localizedName = LocalizationManager.getPokemonName(id).toString()
                val thumbnailPath = AssetUtils.getPokemonThumbnailPath(id)

                return ViewModelEvolutionData(
                    id, baseId,
                    evolutionConditions,
                    isDiscovered,
                    localizedName,
                    thumbnailPath
                )
            }

            val rootId = if (evolutions.isNotEmpty()) evolutions[0].base_id else pokemonId
            val rootData = buildEvolutionData(rootId, null)
            evolutionData.add(rootData)

            evolutions.forEach { evolution ->
                val nodeData = buildEvolutionData(evolution.evolved_id, evolution)
                evolutionData.add(nodeData)
            }

            return evolutionData
        }

        val discoveredSetting = repositoryPreferences.getLivePrefixedSettings<Boolean>(PreferenceType.DISCOVERED)

        evolutionData = MediatorLiveData()
        fun checkAndCombineEvolutionData() {
            val dataList = combineEvolutionData(//pokemonId,
                evolutionChain, discoveredSetting)
            if (dataList != null)
                evolutionData.postValue(dataList!!)
        }
        evolutionData.addSource(evolutionChain) { _ -> checkAndCombineEvolutionData() }
        evolutionData.addSource(discoveredSetting) { _ -> checkAndCombineEvolutionData() }
    }

    fun setPokemonId(id: Int) {
        pokemonId.postValue(id)
    }

    fun getEvolutionData(): LiveData<List<ViewModelEvolutionData>> {
        return evolutionData
    }

//    class Factory(private val application: PokechuApplication, private val initialId: Int) :
//        ViewModelProvider.NewInstanceFactory() {
//
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            return ViewModelEvolutions(application, initialId) as T
//        }
//    }
}