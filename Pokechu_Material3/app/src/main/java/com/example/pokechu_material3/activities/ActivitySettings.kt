package com.example.pokechu_material3.activities

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.pokechu_material3.R

interface PreferenceChangeListener {
    fun onLanguagePreferenceChanged(language: String)
}

class ActivitySettings : BaseActivity(), PreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val appLanguage: ListPreference? = findPreference("setting_app_language")
            val dataLanguage: ListPreference? = findPreference("setting_data_language")

            if (appLanguage != null) {
                val currentActivity = activity
                appLanguage.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, newValue ->
                        val language = newValue.toString()
                        (activity as PreferenceChangeListener).onLanguagePreferenceChanged(language)
                        true
                    }
            };
        }
    }

    override fun onLanguagePreferenceChanged(language: String) {
        setLanguage(language)
    }
}