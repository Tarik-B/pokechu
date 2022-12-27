package fr.amazer.pokechu

import android.content.Context
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import java.util.*

class PokechuApplication: LocalizationApplication() {
    override fun getDefaultLanguage(context: Context): Locale = Locale.ENGLISH

    override fun onCreate() {
        super.onCreate()

        DatabaseManager.with(applicationContext)
        SettingsManager.with(applicationContext)
        LocalizationManager.with(applicationContext)
    }
}