package fr.amazer.pokechu.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import fr.amazer.pokechu.enums.PreferenceData
import fr.amazer.pokechu.enums.PreferenceType

const val LIST_SETTING_SEPARATOR = ","

object SettingsManager {
    lateinit var preferences: SharedPreferences

    fun with(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    // Pokemon discovered/captured status
    fun isPokemonDiscovered(pokemonId: Int): Boolean { return getSetting(PreferenceType.DISCOVERED, pokemonId.toString()) }
    fun setPokemonDiscovered(pokemonId: Int, discovered: Boolean) { setSetting(PreferenceType.DISCOVERED, pokemonId.toString(), discovered) }
    fun togglePokemonDiscovered(pokemonId: Int) { setPokemonDiscovered(pokemonId, !isPokemonDiscovered(pokemonId) ) }
    fun isPokemonCaptured(pokemonId: Int): Boolean { return getSetting(PreferenceType.CAPTURED, pokemonId.toString()) }
    private fun setPokemonCaptured(pokemonId: Int, captured: Boolean) { setSetting(PreferenceType.CAPTURED, pokemonId.toString(), captured) }
    fun togglePokemonCaptured(pokemonId: Int) { setPokemonCaptured(pokemonId, !isPokemonCaptured(pokemonId) ) }
    fun clearPokemonDiscoveredAndCaptured() {
        val editor = preferences.edit()
        preferences.all.forEach { (key, value) ->
            val discoveredPrefix = PreferenceData.getKey(PreferenceType.DISCOVERED)
            if ( key.startsWith(discoveredPrefix) && value is Boolean )
                editor.putBoolean(key, false)

            val capturedPrefix = PreferenceData.getKey(PreferenceType.CAPTURED)
            if ( key.startsWith(capturedPrefix) && value is Boolean )
                editor.putBoolean(key, false)
        }
        editor.apply()
    }

    // Simple settings
    inline fun <reified T: Any> getSetting(type: PreferenceType): T {
        val key = PreferenceData.getKey(type)
        val defaultValue = PreferenceData.getDefaultValue(type) as T
        return getSettingValue(key, defaultValue)
    }
    inline fun <reified T: Any> getSetting(type: PreferenceType, suffix: String): T {
        val key = PreferenceData.getKey(type) + suffix
        val defaultValue = PreferenceData.getDefaultValue(type) as T
        return getSettingValue(key, defaultValue)
    }
    inline fun <reified T: Any>  setSetting(type: PreferenceType, value: T) {
        val key = PreferenceData.getKey(type)
        setSettingValue(key, value)
    }
    inline fun <reified T: Any>  setSetting(type: PreferenceType, suffix: String, value: T) {
        val key = PreferenceData.getKey(type) + suffix
        setSettingValue(key, value)
    }
    inline fun <reified T: Any> getSettingValue(key: String, defaultValue: T): T {
        return when(T::class) {
            Boolean::class -> preferences.getBoolean(key, defaultValue as Boolean) as T
            String::class -> preferences.getString(key, defaultValue as String) as T
            Integer::class -> preferences.getInt(key, defaultValue as Int) as T
            List::class -> {
                getListSettingValue(key, defaultValue as List<Any>) as T
            }
            else -> throw Exception()
        }
    }
    inline fun <reified T: Any> getListSettingValue(key: String, defaultValue: List<T>): List<T> {
        val defaultValueListString = (defaultValue as List<*>).joinToString(LIST_SETTING_SEPARATOR)
        val listString = preferences.getString(key, defaultValueListString )!!
        val stringList = listString.split(LIST_SETTING_SEPARATOR).toList()
        val valueList = List<T>(stringList.size){ index -> stringList[index].toInt() as T }

        return valueList
    }
    inline fun <reified T: Any> setSettingValue(key: String, value: T) {
        when(T::class) {
            Boolean::class -> preferences.edit().putBoolean(key, value as Boolean).apply()
            String::class -> preferences.edit().putString(key, value as String).apply()
            Integer::class -> preferences.edit().putInt(key, value as Int).apply()
            List::class -> {
                setListSettingValue(key, value as List<Any>)
            }
            else -> throw Exception()
        }
    }
    inline fun <reified T: Any> setListSettingValue(key: String, value: T) {
        val valueListString = (value as List<*>).joinToString(LIST_SETTING_SEPARATOR)
        preferences.edit().putString(key, valueListString).apply()
    }
}