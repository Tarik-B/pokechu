package com.example.pokechu_material3

import android.content.Context
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import com.example.pokechu_material3.managers.SettingsManager
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