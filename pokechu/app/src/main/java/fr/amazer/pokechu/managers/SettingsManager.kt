package fr.amazer.pokechu.managers

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceManager
import fr.amazer.pokechu.data.PokedexType
import java.util.*


object SettingsManager {

    private lateinit var preferences: SharedPreferences
    private lateinit var listener: OnSharedPreferenceChangeListener
    private const val PREFERENCES_FILE_NAME = "settings"

    fun with(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        listener = OnSharedPreferenceChangeListener { prefs, key ->
            if (key == KEY_SETTING_SELECTED_REGION)
                selectedRegionListeners.forEach{ it() }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    private const val KEY_POKEMON_PREFIX = "pokemon_"
    private const val KEY_POKEMON_DISCOVERED_SUFFIX = "_discovered"
    private const val KEY_POKEMON_CAPTURED_SUFFIX = "_captured"
    private const val KEY_SETTING_SEARCH_ALL_FIELDS = "setting_search_all_fields"
    private const val KEY_SETTING_LIST_VIEW = "setting_list_view"
//    private const val KEY_SETTING_APP_LANGUAGE = "setting_app_language"
    private const val KEY_SETTING_DATA_LANGUAGE = "setting_data_language"
    private const val KEY_SETTING_SELECTED_REGION = "setting_selected_region"

    private const val DEFAULT_POKEMON_DISCOVERED = false
    public fun isPokemonDiscovered(pokemonId: Int): Boolean { return getSetting(KEY_POKEMON_PREFIX + pokemonId + KEY_POKEMON_DISCOVERED_SUFFIX, DEFAULT_POKEMON_DISCOVERED)!! }
    public fun setPokemonDiscovered(pokemonId: Int, discovered: Boolean) { setSetting(KEY_POKEMON_PREFIX + pokemonId + KEY_POKEMON_DISCOVERED_SUFFIX, discovered) }
    public fun togglePokemonDiscovered(pokemonId: Int) { setPokemonDiscovered(pokemonId, !isPokemonDiscovered(pokemonId) ) }

    private const val DEFAULT_POKEMON_CAPTURED = false
    public fun isPokemonCaptured(pokemonId: Int): Boolean { return getSetting(KEY_POKEMON_PREFIX + pokemonId + KEY_POKEMON_CAPTURED_SUFFIX, DEFAULT_POKEMON_CAPTURED)!! }
    public fun setPokemonCaptured(pokemonId: Int, captured: Boolean) { setSetting(KEY_POKEMON_PREFIX + pokemonId + KEY_POKEMON_CAPTURED_SUFFIX, captured) }
    public fun togglePokemonCaptured(pokemonId: Int) { setPokemonCaptured(pokemonId, !isPokemonCaptured(pokemonId) ) }

    private const val DEFAULT_SEARCH_ALL_FIELDS = true
    public fun isSearchAllFieldsEnabled(context: Context): Boolean { return getSetting(KEY_SETTING_SEARCH_ALL_FIELDS, DEFAULT_SEARCH_ALL_FIELDS)!! }
    public fun setSearchAllFieldsEnabled(context: Context, enabled: Boolean) { setSetting(KEY_SETTING_SEARCH_ALL_FIELDS, enabled) }

    private const val DEFAULT_LIST_VIEW = true
    public fun isListViewEnabled(): Boolean { return getSetting(KEY_SETTING_LIST_VIEW, DEFAULT_LIST_VIEW)!! }
    public fun setListViewEnabled(enabled: Boolean) { setSetting(KEY_SETTING_LIST_VIEW, enabled) }

//    private val DEFAULT_APP_LANGUAGE = Locale.getDefault().language // "sys_def" // LocaleUtils.OPTION_PHONE_LANGUAGE
//    public fun getAppLanguage(): String { return getSetting(KEY_SETTING_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)!! }
//    public fun setAppLanguage(language: String) { setSetting(KEY_SETTING_APP_LANGUAGE, language) }

    private val DEFAULT_DATA_LANGUAGE = Locale.getDefault().language
    public fun getDataLanguage(): String { return getSetting(KEY_SETTING_DATA_LANGUAGE, DEFAULT_DATA_LANGUAGE)!! }
//    public fun setDataLanguage(language: String) { setSetting(KEY_SETTING_DATA_LANGUAGE, language) }

    private val DEFAULT_SELECTED_REGION = PokedexType.NATIONAL.ordinal
    public fun getSelectedRegion(): Int { return getSetting(KEY_SETTING_SELECTED_REGION, DEFAULT_SELECTED_REGION)!! }
    public fun setSelectedRegion(region: PokedexType) { setSetting(KEY_SETTING_SELECTED_REGION, region.ordinal) }
    private val selectedRegionListeners = mutableListOf<() -> Unit>()
    fun addSelectedRegionListener(listener: () -> Unit) {
        selectedRegionListeners.add(listener)
    }


    private inline fun <reified T: Any> getSetting(key: String, defaultValue: T): T? {
        return when(T::class) {
            Boolean::class -> preferences.getBoolean(key, defaultValue as Boolean) as T
            String::class -> preferences.getString(key, defaultValue as String) as T
            Integer::class -> preferences.getInt(key, defaultValue as Int) as T
            else -> null
        }
    }

    private inline fun <reified T: Any> setSetting(key: String, value: T) {
        when(T::class) {
            Boolean::class -> preferences.edit().putBoolean(key, value as Boolean).apply()
            String::class -> preferences.edit().putString(key, value as String).apply()
            Integer::class -> preferences.edit().putInt(key, value as Int).apply()
        }
    }

    public fun getPokemonDiscoveredCount(): Int {
        var count = 0
        preferences.all.forEach { (key, value) ->
            if ( key.endsWith(KEY_POKEMON_DISCOVERED_SUFFIX) && (value as Boolean) )
                count += 1
        }

        return count
    }

    public fun clearPokemonDiscoveredAndCaptured() {
        val editor = preferences.edit()
        preferences.all.forEach { (key, value) ->
            if ( key.endsWith(KEY_POKEMON_DISCOVERED_SUFFIX) && value is Boolean )
                editor.putBoolean(key, false)
            if ( key.endsWith(KEY_POKEMON_CAPTURED_SUFFIX) && value is Boolean )
                editor.putBoolean(key, false)
        }
        editor.apply()
    }
}