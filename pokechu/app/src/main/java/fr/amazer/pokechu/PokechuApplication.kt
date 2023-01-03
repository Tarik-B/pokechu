package fr.amazer.pokechu

import android.content.Context
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import fr.amazer.pokechu.data.DataRepositoryEvolutions
import fr.amazer.pokechu.data.DataRepositoryPokemon
import fr.amazer.pokechu.data.DataRepositoryPokemons
import fr.amazer.pokechu.data.DataRepositoryRegions
import fr.amazer.pokechu.database.PokechuDatabase
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import java.util.*

class PokechuApplication: LocalizationApplication() {
    override fun getDefaultLanguage(context: Context): Locale = Locale.ENGLISH
    private val appExecutors = ApplicationExecutors()

    override fun onCreate() {
        super.onCreate()

        SettingsManager.with(applicationContext)
        LocalizationManager.with(applicationContext)
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
}