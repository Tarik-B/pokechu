package fr.amazer.pokechu.viewmodel

import android.app.Application
import androidx.lifecycle.*
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryPokemons
import fr.amazer.pokechu.data.DataRepositoryPreferences
import fr.amazer.pokechu.data.preferences.LivePreference
import fr.amazer.pokechu.database.entities.NationalIdLocalId
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.database.joins.PokemonIdTypesId
import fr.amazer.pokechu.enums.PokemonType
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils

data class ViewModelFilters(
    val selectedRegion: Int,
    val discoveredOnly: Boolean = false,
    val capturedOnly: Boolean = false,
)

class ViewModelPokemons(application: Application) : AndroidViewModel(application) {
    private val repository: DataRepositoryPokemons
    private var repositoryPreferences: DataRepositoryPreferences

    private val pokemons: LiveData<List<NationalIdLocalId>>
    private val pokemonTypes: LiveData<List<PokemonIdTypesId>>
    private val pokemonData: MediatorLiveData<List<ViewModelPokemonListData>>
    private val totalCount: MutableLiveData<Int> = MutableLiveData(0)
    private val discoveredCount: MutableLiveData<Int> = MutableLiveData(0)
    private val capturedCount: MutableLiveData<Int> = MutableLiveData(0)

    init {
        (application as PokechuApplication) // TODO find out how this works
        repository = application.getRepositoryPokemons()!!
        repositoryPreferences = application.getRepositoryPreference()!!

        // Get pokemon list and types
        val selectedRegionSetting = repositoryPreferences.getLiveSetting<Int>(PreferenceType.SELECTED_REGION)
        pokemons = Transformations.switchMap(selectedRegionSetting) { selectedRegion ->
            if (selectedRegion == Region.NATIONAL.ordinal)
                repository.getPokemonsIds()
            else
                repository.getPokemonsIdsByRegion(selectedRegion)
        }
        pokemonTypes = Transformations.switchMap(pokemons) { _ ->
            val selectedRegion = SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION)
            if (selectedRegion == Region.NATIONAL.ordinal)
                repository.getPokemonsTypes()
            else
                repository.getPokemonsTypesByRegion(selectedRegion)
        }

        // Merge other filters
        val discoveredOnlySetting = repositoryPreferences.getLiveSetting<Boolean>(PreferenceType.SHOW_DISCOVERED_ONLY)
        val capturedOnlySetting = repositoryPreferences.getLiveSetting<Boolean>(PreferenceType.SHOW_CAPTURED_ONLY)
        val otherFilters = Transformations.switchMap(discoveredOnlySetting) { discoveredOnly ->
            Transformations.switchMap(capturedOnlySetting) { capturedOnly ->
                MediatorLiveData(ViewModelFilters(0, discoveredOnly, capturedOnly))
            }
        } as MediatorLiveData<ViewModelFilters>

        // Build pokemon data map
        fun combinePokemonData(
            idsLiveData: LiveData<List<NationalIdLocalId>>,
            typesLiveData: LiveData<List<PokemonIdTypesId>>,
            filtersLiveData: MediatorLiveData<ViewModelFilters>,
            paramsLiveData: LiveData<Boolean>
        ): List<ViewModelPokemonListData>? {

            val ids = idsLiveData.value
            val types = typesLiveData.value
            val filters = filtersLiveData.value
            val params = paramsLiveData.value

            // Don't send a success until we have all results
            if (ids == null || types == null || filters == null || params == null )
                return null

            if (ids.size != types.size)
                return null

            fun isPokemonDisplayed(id: Int): Boolean {
                if (id == 0 && !SettingsManager.getSetting<Boolean>(PreferenceType.DISPLAY_ZERO))
                    return false

                return ( (!filters.discoveredOnly || SettingsManager.isPokemonDiscovered(id))
                        && (!filters.capturedOnly || SettingsManager.isPokemonCaptured(id)) )
            }

            val filteredIds = ids.filter { isPokemonDisplayed(it.pokemon_id) }
            val filteredTypes = types.filter { isPokemonDisplayed(it.pokemon_id) }

            var countDiscovered = 0
            var countCaptured = 0
            val dataList = List(filteredIds.size) { i ->
                val pokemonId = filteredIds[i].pokemon_id

                val names = mutableMapOf<String, String>()
                LocalizationManager.getLanguages().forEach { lang ->
                    names[lang] = LocalizationManager.getPokemonName(filteredIds[i].pokemon_id, lang) ?: ""
                }

                val typeImagePaths = List(filteredTypes[i].type_id_list.size){ index ->
                    AssetUtils.getTypeThumbnailPathRound(PokemonType.values()[filteredTypes[i].type_id_list[index].ordinal])
                }

                val isDiscovered = SettingsManager.getSetting<Boolean>(PreferenceType.DISCOVERED, pokemonId.toString())
                val isCaptured = SettingsManager.getSetting<Boolean>(PreferenceType.CAPTURED, pokemonId.toString())

                if (isDiscovered)
                    ++countDiscovered
                if (isCaptured)
                    ++countCaptured

                ViewModelPokemonListData(
                    pokemonId = pokemonId,
                    localId = filteredIds[i].local_id,
                    names = names,
                    isDiscovered = isDiscovered,
                    isCaptured = isCaptured,
                    thumbnailPath = AssetUtils.getPokemonThumbnailPath(pokemonId),
                    typeImagePaths = typeImagePaths,
                )
            }

            // Discovered/captured counts
            discoveredCount.postValue(countDiscovered)
            capturedCount.postValue(countCaptured)
            totalCount.postValue(dataList.size)

            return dataList
        }

        // Merge other params
        val discoveredSetting = repositoryPreferences.getLivePrefixedSettings<Boolean>(PreferenceType.DISCOVERED)
        val capturedSetting = repositoryPreferences.getLivePrefixedSettings<Boolean>(PreferenceType.CAPTURED)
        val displayZeroSetting = repositoryPreferences.getLiveSetting<Boolean>(PreferenceType.DISPLAY_ZERO)
        val otherParams = Transformations.switchMap(discoveredSetting) { discoveredMap ->
            Transformations.switchMap(capturedSetting) { capturedMap ->
                Transformations.switchMap(displayZeroSetting) { displayZero ->
                    MediatorLiveData(true)
                }
            }
        }

        pokemonData = MediatorLiveData()
        fun checkAndCombinePokemonData() {
            val map = combinePokemonData(pokemons, pokemonTypes, otherFilters, otherParams)
            if (map != null)
                pokemonData.postValue(map!!)
        }
        pokemonData.addSource(pokemons) { _ -> checkAndCombinePokemonData() }
        pokemonData.addSource(pokemonTypes) { _ -> checkAndCombinePokemonData() }
        pokemonData.addSource(otherFilters) { _ -> checkAndCombinePokemonData() }
        pokemonData.addSource(otherParams) { _ -> checkAndCombinePokemonData() }
    }

    fun getPokemonData(): LiveData<List<ViewModelPokemonListData>> {
        return pokemonData
    }
    fun getPokemonCount(): LiveData<Int> {
        return totalCount
    }
    fun getPokemonDiscoveredCount(): LiveData<Int> {
        return discoveredCount
    }
    fun getPokemonCapturedCount(): LiveData<Int> {
        return capturedCount
    }
    fun localToNationalId(region_id: Int, local_id: Int): LiveData<Int> {
        return repository.localToNationalId(region_id, local_id)
    }
    fun <T: Any> getLiveSetting(type: PreferenceType): LivePreference<T> {
        return repositoryPreferences.getLiveSetting(type)
    }
    fun getSelectedRegion(): LivePreference<Int> {
        return repositoryPreferences.getLiveSetting(PreferenceType.SELECTED_REGION)
    }
    fun getShowUndiscoveredInfo(): LivePreference<Boolean> {
        return repositoryPreferences.getLiveSetting(PreferenceType.SHOW_UNDISCOVERED_INFO)
    }
    fun getShowDiscoveredOnly(): LivePreference<Boolean> {
        return repositoryPreferences.getLiveSetting(PreferenceType.SHOW_DISCOVERED_ONLY)
    }
    fun getShowCapturedOnly(): LivePreference<Boolean> {
        return repositoryPreferences.getLiveSetting(PreferenceType.SHOW_CAPTURED_ONLY)
    }
    fun getDataLanguage(): LivePreference<String> {
        return repositoryPreferences.getLiveSetting(PreferenceType.DATA_LANGUAGE)
    }
    fun getListViewEnabled(): LivePreference<Boolean> {
        return repositoryPreferences.getLiveSetting(PreferenceType.LIST_VIEW)
    }
}