package fr.amazer.pokechu.managers

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.preference.PreferenceManager
import java.util.*


object SettingsManager {

    lateinit var preferences: SharedPreferences
    private const val PREFERENCES_FILE_NAME = "settings"

//    data class PokemonPreferenceData(
//        val discovered: Boolean
//    )
//    private var data = HashMap<String, PokemonPreferenceData>()

    private const val KEY_POKEMON_DISCOVERED_PREFIX = "pokemon_"
    private const val KEY_POKEMON_DISCOVERED_SUFFIX = "_discovered"
    private const val KEY_SETTING_SEARCH_ALL_FIELDS = "setting_search_all_fields"
    private const val KEY_SETTING_LIST_VIEW = "setting_list_view"
    private const val KEY_SETTING_APP_LANGUAGE = "setting_app_language"
    private const val KEY_SETTING_DATA_LANGUAGE = "setting_data_language"

    fun with(context: Context) {
//        preferences = application.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private const val DEFAULT_POKEMON_DISCOVERED = false
    public fun isPokemonDiscovered(pokemonId: String): Boolean { return getSetting(KEY_POKEMON_DISCOVERED_PREFIX + pokemonId + KEY_POKEMON_DISCOVERED_SUFFIX, DEFAULT_POKEMON_DISCOVERED)!! }
    public fun setPokemonDiscovered(pokemonId: String, discovered: Boolean) { setSetting(KEY_POKEMON_DISCOVERED_PREFIX + pokemonId + KEY_POKEMON_DISCOVERED_SUFFIX, discovered) }
    public fun togglePokemonDiscovered(pokemonId: String) { setPokemonDiscovered(pokemonId, !isPokemonDiscovered(pokemonId) ) }

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
    public fun setDataLanguage(language: String) { setSetting(KEY_SETTING_DATA_LANGUAGE, language) }

    private inline fun <reified T: Any> getSetting(key: String, defaultValue: T): T? {
        return when(T::class) {
            Boolean::class -> preferences.getBoolean(key, defaultValue as Boolean) as T
            String::class -> preferences.getString(key, defaultValue as String) as T
            else -> null
        }
    }

    private inline fun <reified T: Any> setSetting(key: String, value: T) {
        when(T::class) {
            Boolean::class -> preferences.edit().putBoolean(key, value as Boolean).apply()
            String::class -> preferences.edit().putString(key, value as String).apply()
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

    public fun clearPokemonDiscovered() {
        val editor = preferences.edit()
        preferences.all.forEach { (key, value) ->
            if ( key.endsWith(KEY_POKEMON_DISCOVERED_SUFFIX) && value is Boolean )
                editor.putBoolean(key, false)
        }
        editor.apply()
    }

//    private fun loadPreferences() {
//        val jsonString = preferences.getString(KEY_DATA, null)
//        if ( jsonString == null )
//            return
//
//        data = GsonBuilder().create().fromJson(jsonString, Map::class.java) as HashMap<String, PokemonPreferenceData>
//    }
//
//    private fun savePreferences() {
//        val jsonString = GsonBuilder().create().toJson(data)
//
//        preferences.edit().putString(KEY_DATA, jsonString).apply()
//    }
}