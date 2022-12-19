package fr.amazer.pokechu.managers

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import fr.amazer.pokechu.data.PokedexType
import java.util.*


object LocalizationManager {

    fun with(context: Context) {
    }

    fun getLocalizedPokemonName(context: Context, id: Int, language: String = ""): String? {
        return getLocalizedName(context, "pokemon_name_${id}", language)
    }
    fun getLocalizedRegionName(context: Context, id: PokedexType, language: String = ""): String? {
        return getLocalizedName(context, "region_name_${id.ordinal}", language)
    }

    fun getLocalizedName(context: Context, name: String, language: String = ""): String? {

        var lang = language
        if (lang == "")
            lang = SettingsManager.getDataLanguage()

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale(lang))

        val newContext = context.createConfigurationContext(configuration)

        val resId: Int = newContext.resources.getIdentifier(name, "string", "fr.amazer.pokechu")

        try {
            return newContext.resources.getString(resId)
        }
        catch (e: Resources.NotFoundException) {
            return null
        }
    }
}