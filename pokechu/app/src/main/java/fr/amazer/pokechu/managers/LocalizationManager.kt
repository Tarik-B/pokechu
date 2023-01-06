package fr.amazer.pokechu.managers

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import fr.amazer.pokechu.BuildConfig
import fr.amazer.pokechu.PokechuApplication
import fr.amazer.pokechu.R
import fr.amazer.pokechu.enums.EvolutionCondition
import fr.amazer.pokechu.enums.EvolutionItem
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.enums.Region
import kotlinx.coroutines.*
import java.util.*


object LocalizationManager {
    private lateinit var languages: List<String>
    private lateinit var pokemonNames: List<List<String>>
    private lateinit var regionNames: List<List<String>>
    private lateinit var itemNames: List<List<String>>
    private lateinit var conditionNames: List<List<String>>

    fun with(application: PokechuApplication) {
        val langs = application.resources.getStringArray(R.array.language_values)
        languages = langs.asList()

        loadNames(application)
    }

    fun getLanguages(): List<String> {
        return languages
    }

    private fun getDataLanguage(): String {
        return SettingsManager.getSetting(PreferenceType.DATA_LANGUAGE)
    }

    private fun getPokemonNameKey(id: Int): String { return "pokemon_name_${id}" }
    fun getPokemonName(id: Int, language: String = getDataLanguage()): String? {
        val langIndex = languages.indexOf(language)
        return if (langIndex >= 0) pokemonNames[langIndex][id] else null
    }
    private fun getRegionNameKey(id: Int): String { return "region_name_${id}" }
    fun getRegionName(id: Region, language: String = getDataLanguage()): String? {
        val langIndex = languages.indexOf(language)
        return if (langIndex >= 0) regionNames[langIndex][id.ordinal] else null
    }
    private fun getItemNameKey(id: Int): String { return "evolution_item_name_${id}" }
    fun getItemName(id: EvolutionItem, language: String = getDataLanguage()): String? {
        val langIndex = languages.indexOf(language)
        return if (langIndex >= 0) itemNames[langIndex][id.ordinal] else null
    }
    private fun getConditionNameKey(id: Int): String { return "evolution_condition_name_${id}" }
    fun getConditionName(id: EvolutionCondition, language: String = getDataLanguage()): String? {
        val langIndex = languages.indexOf(language)
        return if (langIndex >= 0) conditionNames[langIndex][id.ordinal] else null
    }

    private fun loadNames(application: PokechuApplication) {
        runBlocking {
            launch(Dispatchers.IO) {
                val langContexts = List<Context>(languages.size){ it ->
                    val lang = languages[it]
                    val configuration = Configuration(application.resources.configuration)
                    configuration.setLocale(Locale(lang))

                    application.createConfigurationContext(configuration)
                }

                val pokemonIds = application.getDatabase().getPokemonsDao().findAllIds()
                pokemonNames = loadNames(langContexts, pokemonIds, ::getPokemonNameKey)

                val regionIds = application.getDatabase().getRegionsDao().findAllIds()
                regionNames = loadNames(langContexts, regionIds, ::getRegionNameKey)

                val itemIds = List(EvolutionItem.values().size) { EvolutionItem.values()[it].ordinal }
                itemNames = loadNames(langContexts, itemIds, ::getItemNameKey)

                val conditionIds = List(EvolutionCondition.values().size) { EvolutionCondition.values()[it].ordinal }
                conditionNames = loadNames(langContexts, conditionIds, ::getConditionNameKey)
            }
        }
    }

    private fun loadNames(contexts: List<Context>, ids: List<Int>, getKeyFunction: (input: Int) -> String
    ): MutableList<MutableList<String>> {
        val namesByLangs = MutableList<MutableList<String>>(languages.size) { mutableListOf() }

        languages.forEachIndexed{ langIndex, lang ->

            val max = ids.max()

            namesByLangs[langIndex] = MutableList(max + 1){ "" }
            val names = namesByLangs[langIndex]

            val langResources = contexts[langIndex].resources

            ids.forEach { id ->
                val key = getKeyFunction(id)
                val resId = langResources.getIdentifier(key, "string", BuildConfig.APPLICATION_ID)
                try {
                    names[id] = langResources.getString(resId)
                }
                catch (e: Resources.NotFoundException) {

                }
            }
        }

        return namesByLangs
    }
}