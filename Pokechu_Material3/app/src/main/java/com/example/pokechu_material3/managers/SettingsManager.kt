package com.example.pokechu_material3.managers

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder

object SettingsManager {

    lateinit var preferences: SharedPreferences
    private const val PREFERENCES_FILE_NAME = "settings"

//    data class PokemonPreferenceData(
//        val discovered: Boolean
//    )
//    private var data = HashMap<String, PokemonPreferenceData>()

    private const val KEY_SETTING_SEARCH_ALL = "search_all_fields"
    private const val KEY_SETTING_LIST_VIEW = "list_view"

    enum class SettingType {
        SEARCH_ALL,
        LIST_VIEW
    }

    fun with(application: Application) {
        preferences = application.getSharedPreferences(
            PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

//        loadPreferences()
    }

    public fun isPokemonDiscovered(pokemonId: String): Boolean {
        return preferences.getBoolean("pokemon_${pokemonId}_discovered", false)
//        return data[pokemonId]?.discovered ?: false
    }

    public fun setPokemonDiscovered(pokemonId: String, discovered: Boolean) {
        preferences.edit().putBoolean("pokemon_${pokemonId}_discovered", discovered).apply()
    }

    public fun togglePokemonDiscovered(pokemonId: String) {
        val discovered = isPokemonDiscovered(pokemonId)
        setPokemonDiscovered(pokemonId, !discovered )
    }

    public fun getPokemonDiscoveredCount(): Int {
        var count = 0
        preferences.all.forEach { (key, value) ->
            if ( key.endsWith("_discovered") && (value as Boolean) )
                count = count + 1
        }

        return count
    }

    public fun isSearchAllFieldsEnabled(context: Context): Boolean {
        return getBoolSetting(KEY_SETTING_SEARCH_ALL)
    }

    public fun setSearchAllFieldsEnabled(context: Context, enabled: Boolean) {
        setBoolSetting(KEY_SETTING_SEARCH_ALL, enabled)
    }

    public fun isListViewEnabled(): Boolean {
        return getBoolSetting(KEY_SETTING_LIST_VIEW)
    }

    public fun setListViewEnabled(enabled: Boolean) {
        setBoolSetting(KEY_SETTING_LIST_VIEW, enabled)
    }

    private fun getBoolSetting(key: String): Boolean {
        val enabled = preferences.getBoolean(key, true)

        return enabled
    }

    private fun setBoolSetting(key: String, enabled: Boolean) {
        preferences.edit().putBoolean(key, enabled).apply()
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