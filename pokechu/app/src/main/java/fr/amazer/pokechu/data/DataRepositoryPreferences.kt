package fr.amazer.pokechu.data

import android.content.SharedPreferences
import fr.amazer.pokechu.data.preferences.LiveListPreference
import fr.amazer.pokechu.data.preferences.LivePreference
import fr.amazer.pokechu.data.preferences.MultiPrefixedLivePreference
import fr.amazer.pokechu.enums.PreferenceData
import fr.amazer.pokechu.enums.PreferenceType
import io.reactivex.rxjava3.subjects.PublishSubject

class DataRepositoryPreferences private constructor(
    private val preferences: SharedPreferences
) {

    val updates = PublishSubject.create<String>()
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        updates.onNext(key)
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)

    }

    // LiveData observable settings
    fun <T: Any> getLiveSetting(type: PreferenceType): LivePreference<T> {
        val key = PreferenceData.getKey(type)
        val defaultValue = PreferenceData.getDefaultValue(type) as T

        return LivePreference(updates, preferences, key, defaultValue)
    }
    fun <T: Any> getLiveListSetting(type: PreferenceType): LiveListPreference<T> {
        val key = PreferenceData.getKey(type)
        val defaultValue = PreferenceData.getDefaultValue(type) as List<T>

        return LiveListPreference(updates, preferences, key, defaultValue)
    }
    //    inline fun <reified T: Any> getLiveSetting(type: SettingType, suffix: String): LivePreference<T> {
//        val key = settingsData[type]!!.first + suffix
//        val defaultValue = settingsData[type]!!.second as T
//
//        return LivePreference(updates, preferences, key, defaultValue)
//    }
    fun <T: Any> getLivePrefixedSettings(type: PreferenceType): MultiPrefixedLivePreference<T> {
        val prefix = PreferenceData.getKey(type)
        val defaultValue = PreferenceData.getDefaultValue(type) as T

        return MultiPrefixedLivePreference(updates, preferences, prefix, defaultValue)
    }

    companion object {
        private var sInstance: DataRepositoryPreferences? = null
        fun getInstance(preferences: SharedPreferences): DataRepositoryPreferences? {
            if (sInstance == null) {
                synchronized(DataRepositoryPreferences::class.java) {
                    if (sInstance == null) {
                        sInstance = DataRepositoryPreferences(preferences)
                    }
                }
            }
            return sInstance
        }
    }
}