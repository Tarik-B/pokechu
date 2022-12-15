package fr.amazer.pokechu.managers

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*


object LocalizationManager {

    fun with(context: Context) {
    }

    fun getLocalizedPokemonName(context: Context, id: Int, language: String = ""): String? {

        var lang = language
        if (lang == "")
            lang = SettingsManager.getDataLanguage()

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale(lang))

        val newContext = context.createConfigurationContext(configuration)

        val resId: Int = newContext.resources.getIdentifier("pokemon_name_${id}", "string", "fr.amazer.pokechu")

        try {
            return newContext.resources.getString(resId)
        }
        catch (e: Resources.NotFoundException) {
            return null
        }
    }
}