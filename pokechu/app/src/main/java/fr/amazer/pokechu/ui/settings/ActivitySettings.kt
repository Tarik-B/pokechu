package fr.amazer.pokechu.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import fr.amazer.pokechu.R
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.BaseActivity

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
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // App language change
            val appLanguage: ListPreference? = findPreference("setting_app_language")
            if (appLanguage != null) {
                // App language preference is not persistent so it has to be manually loaded
                val langs = LocalizationManager.getLanguages()
                val index = langs.indexOf((activity as ActivitySettings).getCurrentLanguage().language)
                if (index >= 0)
                    appLanguage.setValueIndex(index)

                appLanguage.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, newValue ->
                        val language = newValue.toString()
                        (activity as PreferenceChangeListener).onLanguagePreferenceChanged(language)
                        true
                    }
            }

            // Data clearing
            val clearData: Preference? = findPreference("setting_clear_data")
            if (clearData != null) {
                clearData.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener { _ ->
                        // Open confirmation dialog before clearing
                        val builder = AlertDialog.Builder(activity)
                        builder.setMessage(R.string.dialog_are_you_sure)
                            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                                SettingsManager.clearPokemonDiscoveredAndCaptured()
                            }
                            .setNegativeButton(R.string.dialog_no) { _, _ -> }
                        val alert = builder.create()
                        alert.show()
                        true
                    }
            }
        }
    }

    override fun onLanguagePreferenceChanged(language: String) {
        setLanguage(language)
    }
}