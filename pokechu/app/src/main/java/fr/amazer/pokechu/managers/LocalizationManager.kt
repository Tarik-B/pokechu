package fr.amazer.pokechu.managers

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import fr.amazer.pokechu.R
import fr.amazer.pokechu.enums.EvolutionCondition
import fr.amazer.pokechu.enums.EvolutionItem
import fr.amazer.pokechu.enums.Region
import java.util.*


object LocalizationManager {
    private lateinit var languages: List<String>

    fun with(context: Context) {
        val langs = context.resources.getStringArray(R.array.language_values)
        languages = langs.asList()
    }

    fun getLanguages(): List<String> {
        return languages
    }

    fun getPokemonName(context: Context, id: Int, language: String = ""): String? {
        return getLocalizedName(context, "pokemon_name_${id}", language)
    }

    fun getRegionName(context: Context, id: Region, language: String = ""): String? {
        return getLocalizedName(context, "region_name_${id.ordinal}", language)
    }

    fun getItemName(context: Context, id: EvolutionItem, language: String = ""): String? {
        return getLocalizedName(context, "evolution_item_name_${id.ordinal}", language)
    }

    fun getConditionName(context: Context, id: EvolutionCondition, language: String = ""): String? {
        return getLocalizedName(context, "evolution_condition_name_${id.ordinal}", language)
    }

    fun getLocalizedName(context: Context, name: String, language: String = ""): String? {

        var lang = language
        if (lang == "")
            lang = SettingsManager.getSetting<String>(SettingType.DATA_LANGUAGE)

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