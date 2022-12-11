package fr.amazer.pokechu

import android.content.Context
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import fr.amazer.pokechu.managers.SettingsManager
import java.util.*

class PokechuApplication: LocalizationApplication() {
    /* ... */
    override fun getDefaultLanguage(context: Context) = Locale.ENGLISH

    override fun onCreate() {
        super.onCreate()

        SettingsManager.with(applicationContext)
    }

//    override fun attachBaseContext(base: Context) {
//        SettingsManager.with(base)
//        super.attachBaseContext(base)
//    }


}