package fr.amazer.pokechu.managers

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceManager
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.managers.settings.LivePreference
import fr.amazer.pokechu.managers.settings.MultiPrefixedLivePreference
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

enum class SettingType {
    SHOW_UNDISCOVERED_INFO,
    SHOW_DISCOVERED_ONLY,
    SHOW_CAPTURED_ONLY,
    LIST_VIEW,
    DATA_LANGUAGE,
    SELECTED_REGION,
    CAPTURED,
    DISCOVERED
}

object SettingsManager {

    lateinit var preferences: SharedPreferences
    val updates = PublishSubject.create<String>()
    private val listener = OnSharedPreferenceChangeListener { _, key ->
        updates.onNext(key)
    }

    val settingsData = mapOf(
        SettingType.SHOW_UNDISCOVERED_INFO  to Pair("setting_show_undiscovered_info", false),
        SettingType.SHOW_DISCOVERED_ONLY    to Pair("setting_show_discovered_only", false),
        SettingType.SHOW_CAPTURED_ONLY      to Pair("setting_show_captured_only", false),
        SettingType.LIST_VIEW               to Pair("setting_list_view", false),
        SettingType.DATA_LANGUAGE           to Pair("setting_data_language", Locale.getDefault().language),
        SettingType.SELECTED_REGION         to Pair("setting_selected_region", Region.NATIONAL.ordinal),
        SettingType.DISCOVERED              to Pair("pokemon_discovered_", false),
        SettingType.CAPTURED                to Pair("pokemon_captured_", false),
    )

    fun with(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    // Pokemon discovered/captured status
    fun isPokemonDiscovered(pokemonId: Int): Boolean { return getSettingValue<Boolean>(settingsData[SettingType.DISCOVERED]!!.first + pokemonId, settingsData[SettingType.DISCOVERED]!!.second as Boolean) }
    fun setPokemonDiscovered(pokemonId: Int, discovered: Boolean) { setSettingValue<Boolean>(settingsData[SettingType.DISCOVERED]!!.first + pokemonId, discovered) }
    fun togglePokemonDiscovered(pokemonId: Int) { setPokemonDiscovered(pokemonId, !isPokemonDiscovered(pokemonId) ) }
    fun isPokemonCaptured(pokemonId: Int): Boolean { return getSettingValue<Boolean>(settingsData[SettingType.CAPTURED]!!.first + pokemonId, settingsData[SettingType.DISCOVERED]!!.second as Boolean) }
    private fun setPokemonCaptured(pokemonId: Int, captured: Boolean) { setSettingValue<Boolean>(settingsData[SettingType.CAPTURED]!!.first + pokemonId, captured) }
    fun togglePokemonCaptured(pokemonId: Int) { setPokemonCaptured(pokemonId, !isPokemonCaptured(pokemonId) ) }
    fun clearPokemonDiscoveredAndCaptured() {
        val editor = preferences.edit()
        preferences.all.forEach { (key, value) ->
            if ( key.startsWith(settingsData[SettingType.DISCOVERED]!!.first) && value is Boolean )
                editor.putBoolean(key, false)
            if ( key.startsWith(settingsData[SettingType.CAPTURED]!!.first) && value is Boolean )
                editor.putBoolean(key, false)
        }
        editor.apply()
    }

    // Simple settings
    inline fun <reified T: Any> getSetting(type: SettingType): T {
        val key = settingsData[type]!!.first
        val defaultValue = settingsData[type]!!.second as T
        return getSettingValue(key, defaultValue)
    }
    inline fun <reified T: Any> getSetting(type: SettingType, suffix: String): T {
        val key = settingsData[type]!!.first + suffix
        val defaultValue = settingsData[type]!!.second as T
        return getSettingValue(key, defaultValue)
    }
    inline fun <reified T: Any>  setSetting(type: SettingType, value: T) {
        val key = settingsData[type]!!.first
        setSettingValue(key, value)
    }
    inline fun <reified T: Any> getSettingValue(key: String, defaultValue: T): T {
        return when(T::class) {
            Boolean::class -> preferences.getBoolean(key, defaultValue as Boolean) as T
            String::class -> preferences.getString(key, defaultValue as String) as T
            Integer::class -> preferences.getInt(key, defaultValue as Int) as T
            else -> throw Exception()
        }
    }
    inline fun <reified T: Any> setSettingValue(key: String, value: T) {
        when(T::class) {
            Boolean::class -> preferences.edit().putBoolean(key, value as Boolean).apply()
            String::class -> preferences.edit().putString(key, value as String).apply()
            Integer::class -> preferences.edit().putInt(key, value as Int).apply()
            else -> throw Exception()
        }
    }

    // LiveData observable settings
    inline fun <reified T: Any> getLiveSetting(type: SettingType): LivePreference<T> {
        val key = settingsData[type]!!.first
        val defaultValue = settingsData[type]!!.second as T

        return LivePreference(updates, preferences, key, defaultValue)
    }
//    inline fun <reified T: Any> getLiveSetting(type: SettingType, suffix: String): LivePreference<T> {
//        val key = settingsData[type]!!.first + suffix
//        val defaultValue = settingsData[type]!!.second as T
//
//        return LivePreference(updates, preferences, key, defaultValue)
//    }
    inline fun <reified T: Any> getLivePrefixedSettings(type: SettingType): MultiPrefixedLivePreference<T> {
        val prefix = settingsData[type]!!.first
        val defaultValue = settingsData[type]!!.second as T

        return MultiPrefixedLivePreference(updates, preferences, prefix, defaultValue)
    }
}