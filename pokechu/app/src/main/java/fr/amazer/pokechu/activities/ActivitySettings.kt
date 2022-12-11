package fr.amazer.pokechu.activities

import android.app.AlertDialog
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import fr.amazer.pokechu.R
import fr.amazer.pokechu.managers.SettingsManager

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
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val appLanguage: ListPreference? = findPreference("setting_app_language")
            val dataLanguage: ListPreference? = findPreference("setting_data_language")

            if (appLanguage != null) {
                appLanguage.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, newValue ->
                        val language = newValue.toString()
                        (activity as PreferenceChangeListener).onLanguagePreferenceChanged(language)
                        true
                    }
            };

            val clearData: Preference? = findPreference("setting_clear_data")
            if (clearData != null) {
                clearData.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener { preference ->
                        val builder = AlertDialog.Builder(activity)
                        builder.setMessage(R.string.dialog_are_you_sure)
                            .setPositiveButton(R.string.dialog_yes) { dialog, id ->
                                SettingsManager.clearPokemonDiscovered()
                            }
                            .setNegativeButton(R.string.dialog_no) { dialog, id ->
                            }
                        val alert = builder.create()
                        alert.show()
                        true
                    }
            };
        }
    }

    override fun onLanguagePreferenceChanged(language: String) {
        setLanguage(language)
    }
}