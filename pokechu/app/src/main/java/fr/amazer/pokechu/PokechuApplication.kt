package fr.amazer.pokechu

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import fr.amazer.pokechu.data.*
import fr.amazer.pokechu.database.PokechuDatabase
import fr.amazer.pokechu.enums.NightMode
import fr.amazer.pokechu.enums.PreferenceData
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import java.util.*

class PokechuApplication: LocalizationApplication() {
    private val appExecutors = ApplicationExecutors()

    override fun onCreate() {
        super.onCreate()

        SettingsManager.with(applicationContext)
        LocalizationManager.with(this)

        // Apply saved night mode
        val preference = SettingsManager.getSetting<String>(PreferenceType.NIGHT_MODE)
        val preferenceValue = preference.toInt()
        val nightModeValue = PreferenceData.nightModeToAppCompat(NightMode.values()[preferenceValue])
        if (nightModeValue != null) {
            AppCompatDelegate.setDefaultNightMode(nightModeValue)
        }


//        var elapsed = measureTimeMillis {
//            println("test")
//        }
//        println("Time = $elapsed")
    }

//    override fun attachBaseContext(context: Context) {
//
//        val configuration = Configuration(base.resources.configuration)
//
//        // Required for the day/night theme to work
//        configuration.uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED
//        val baseContext = base.createConfigurationContext(configuration)
//
//        super.attachBaseContext(baseContext)
//    }

    override fun getDefaultLanguage(context: Context): Locale {
        val langs = context.resources.getStringArray(R.array.language_values)
        return Locale(langs[0])
    }

    fun getDatabase(): PokechuDatabase {
        return PokechuDatabase.getInstance(this, appExecutors)!!
    }
    fun getRepositoryRegions(): DataRepositoryRegions? {
        return DataRepositoryRegions.getInstance(getDatabase())
    }
    fun getRepositoryEvolutions(): DataRepositoryEvolutions? {
        return DataRepositoryEvolutions.getInstance(getDatabase())
    }
    fun getRepositoryPokemon(): DataRepositoryPokemon? {
        return DataRepositoryPokemon.getInstance(getDatabase())
    }
    fun getRepositoryPokemons(): DataRepositoryPokemons? {
        return DataRepositoryPokemons.getInstance(getDatabase())
    }
    fun getRepositoryPreference(): DataRepositoryPreferences? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        return DataRepositoryPreferences.getInstance(preferences)
    }
}