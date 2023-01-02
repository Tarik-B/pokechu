package fr.amazer.pokechu.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.data.DataRepositoryPokemons
import fr.amazer.pokechu.enums.NationalIdLocalId
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.enums.PokemonIdTypesId
import fr.amazer.pokechu.enums.PokemonType
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingType
import fr.amazer.pokechu.managers.SettingsManager

data class ViewModelPokemonData(
//    val pokemonId: Int,
    val localId: Int,
    val names: Map<String, String>,
    val types: List<PokemonType>
)

data class ViewModelFilters(
    val selectedRegion: Int,
    val discoveredOnly: Boolean = false,
    val capturedOnly: Boolean = false,
)

class ViewModelPokemons(application: Application) : AndroidViewModel(application) {
    private val repository: DataRepositoryPokemons

    private val pokemons: LiveData<List<NationalIdLocalId>>
    private val pokemonTypes: LiveData<List<PokemonIdTypesId>>
    private val pokemonData: MediatorLiveData<Map<Int, ViewModelPokemonData>>
    private val discoveredCount: MediatorLiveData<Int>
    private val capturedCount: MediatorLiveData<Int>
    private val allFilters: MediatorLiveData<ViewModelFilters>

    init {
        repository = (application as PokechuApplication).getRepositoryPokemons()!!

        // Get pokemon list and types
        val selectedRegionSetting = SettingsManager.getLiveSetting<Int>(SettingType.SELECTED_REGION)
        pokemons = Transformations.switchMap(selectedRegionSetting) { selectedRegion ->
            if (selectedRegion == Region.NATIONAL.ordinal)
                return@switchMap repository.getPokemonsIds()
            else
                return@switchMap repository.getPokemonsIdsByRegion(selectedRegion)
        }
        pokemonTypes = Transformations.switchMap(pokemons) { _ ->
            val selectedRegion = SettingsManager.getSetting<Int>(SettingType.SELECTED_REGION)
            if (selectedRegion == Region.NATIONAL.ordinal)
                return@switchMap repository.getPokemonsTypes()
            else
                return@switchMap repository.getPokemonsTypesByRegion(selectedRegion)
        }

        // Merge other filters
        val discoveredOnlySetting = SettingsManager.getLiveSetting<Boolean>(SettingType.SHOW_DISCOVERED_ONLY)
        val capturedOnlySetting = SettingsManager.getLiveSetting<Boolean>(SettingType.SHOW_CAPTURED_ONLY)
        val otherFilters = Transformations.switchMap(discoveredOnlySetting) { discoveredOnly ->
            Transformations.switchMap(capturedOnlySetting) { capturedOnly ->
                return@switchMap MediatorLiveData(ViewModelFilters(0, discoveredOnly, capturedOnly))
            }
        } as MediatorLiveData<ViewModelFilters>

        // Build pokemon data map
        fun combinePokemonData(
            idsLiveData: LiveData<List<NationalIdLocalId>>,
            typesLiveData: LiveData<List<PokemonIdTypesId>>,
            filtersLiveData: MediatorLiveData<ViewModelFilters>
        ): Map<Int, ViewModelPokemonData>? {

            val ids = idsLiveData.value
            val types = typesLiveData.value
            val filters = filtersLiveData.value

            // Don't send a success until we have both results
            if (ids == null || types == null || filters == null) {
                return null
            }

            if (ids.size != types.size)
                return null

            fun isPokemonDisplayed(id: Int): Boolean {
                return ( (!filters.discoveredOnly || SettingsManager.isPokemonDiscovered(id))
                        && (!filters.capturedOnly || SettingsManager.isPokemonCaptured(id)) )
            }
            val filteredIds = ids.filter { isPokemonDisplayed(it.pokemon_id) }
            val filteredTypes = types.filter { isPokemonDisplayed(it.pokemon_id) }

            val keys = List(filteredIds.size) { i ->
                filteredIds[i].pokemon_id
            }
            val values = List(filteredIds.size) { i ->
                val names = mutableMapOf<String, String>()
                LocalizationManager.getLanguages().forEach { lang ->
                    names[lang] = LocalizationManager.getPokemonName(application.applicationContext, filteredIds[i].pokemon_id, lang) ?: ""
                }
                ViewModelPokemonData(filteredIds[i].local_id, names, filteredTypes[i].type_id_list)
            }

            return keys.zip(values).toMap()
        }

        pokemonData = MediatorLiveData()

        fun checkAndCombinePokemonData() {
            val map = combinePokemonData(pokemons, pokemonTypes, otherFilters)
            if (map != null)
                pokemonData.postValue(map!!)
        }

        pokemonData.addSource(pokemons) { value -> checkAndCombinePokemonData() }
        pokemonData.addSource(pokemonTypes) { value -> checkAndCombinePokemonData() }
        pokemonData.addSource(otherFilters) { value -> checkAndCombinePokemonData() }

        // Discovered/captured counts
        discoveredCount = MediatorLiveData()
        discoveredCount.addSource(SettingsManager.getLivePrefixedSettings<Boolean>(SettingType.DISCOVERED)) { map ->
            discoveredCount.postValue(map.count { it.value == true })
        }

        capturedCount = MediatorLiveData()
        capturedCount.addSource(SettingsManager.getLivePrefixedSettings<Boolean>(SettingType.CAPTURED)) { map ->
            capturedCount.postValue(map.count { it.value == true })
        }

        // Merge filter settings into one livedata for observation
        allFilters = Transformations.switchMap(selectedRegionSetting) { selectedRegion ->
            Transformations.switchMap(discoveredOnlySetting) { discoveredOnly ->
                Transformations.switchMap(capturedOnlySetting) { capturedOnly ->
                    return@switchMap MediatorLiveData(ViewModelFilters(selectedRegion, discoveredOnly, capturedOnly))
                }
            }
        } as MediatorLiveData<ViewModelFilters>
    }

    fun getPokemonData(): LiveData<Map<Int, ViewModelPokemonData>> {
        return pokemonData
    }
    fun getPokemonCount(): LiveData<Int> {
        return repository.getPokemonCount()
    }
    fun getPokemonFilters() : LiveData<ViewModelFilters> {
        return allFilters
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
}