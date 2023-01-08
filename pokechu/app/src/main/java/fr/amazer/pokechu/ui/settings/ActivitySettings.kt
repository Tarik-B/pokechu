package fr.amazer.pokechu.ui.settings

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import fr.amazer.pokechu.R
import fr.amazer.pokechu.enums.NightMode
import fr.amazer.pokechu.enums.PreferenceData
import fr.amazer.pokechu.enums.PreferenceType
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
            setPreferencesFromResource(R.xml.preferences, rootKey)

            // Night mode
            val nightMode: ListPreference? = findPreference("setting_night_mode")
            if (nightMode != null) {
                // Pass night mode current value
                val preference = SettingsManager.getSetting<String>(PreferenceType.NIGHT_MODE)
                val preferenceValue = preference.toInt()
                if (preferenceValue >= 0)
                    nightMode.setValueIndex(preferenceValue)

                nightMode.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, newValue ->
                        val preferenceValue = (newValue as String).toInt()
                        val nightModeValue = PreferenceData.nightModeToAppCompat(NightMode.values()[preferenceValue])
                        AppCompatDelegate.setDefaultNightMode(nightModeValue)
                        true
                    }
            }

            // App language change
            val appLanguage: ListPreference? = findPreference("setting_app_language")
            if (appLanguage != null) {
                // Pass app language current value
                val langs = LocalizationManager.getLanguages()
                val currentLang = (activity as ActivitySettings).getCurrentLanguage().language
                val index = langs.indexOf(currentLang)
                if (index >= 0)
                    appLanguage.setValueIndex(index)

                appLanguage.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, newValue ->
                        val language = newValue.toString()
                        (activity as PreferenceChangeListener).onLanguagePreferenceChanged(language)
                        true
                    }
            }

            // Pass data language current value
            val dataLanguage: ListPreference? = findPreference("setting_data_language")
            if (dataLanguage != null) {
                val langs = LocalizationManager.getLanguages()
                val dataLang = SettingsManager.getSetting<String>(PreferenceType.DATA_LANGUAGE)
                val index = langs.indexOf(dataLang)
                if (index >= 0)
                    dataLanguage.setValueIndex(index)
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

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)

        print("night mode = ")
        when(mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> {println("MODE_NIGHT_NO")}
            AppCompatDelegate.MODE_NIGHT_YES -> {println("MODE_NIGHT_YES")}
            AppCompatDelegate.MODE_NIGHT_AUTO_TIME -> {println("MODE_NIGHT_AUTO_TIME")}
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {println("MODE_NIGHT_FOLLOW_SYSTEM")}
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> {println("MODE_NIGHT_UNSPECIFIED")}
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> {println("MODE_NIGHT_AUTO_BATTERY")}
        }

        print("uiMode & night mask = ")
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {println("Configuration.UI_MODE_NIGHT_NO")}
            // Night mode is not active, we're in day time
            Configuration.UI_MODE_NIGHT_YES -> {println("Configuration.UI_MODE_NIGHT_YES")}
            // Night mode is active, we're at night!
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {println("Configuration.UI_MODE_NIGHT_UNDEFINED")}
            // We don't know what mode we're in, assume notnight
        }
    }
}