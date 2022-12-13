package fr.amazer.pokechu.managers

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.amazer.pokechu.data.DataEvolutionTree
import fr.amazer.pokechu.data.DataPokemon
import fr.amazer.pokechu.utils.AssetUtils
import java.util.*


object DataManager {
    private lateinit var pokemonMap: Map<String, DataPokemon>
    private var evolutionTrees = ArrayList<DataEvolutionTree>()

//    private var pokemonIdsByRegions = HashMap

    public fun loadJsonData(context: Context) {
        // Pokemon list
        var jsonFileString = AssetUtils.getJsonDataFromAsset(context, "pokemon_list.json")
        val pokemonDictType = object : TypeToken<Map<String, DataPokemon>>() {}.type
        pokemonMap = Gson().fromJson(jsonFileString, pokemonDictType)
//        pokemonList.forEach { (key, data) -> pokemonMap[data.ids.unique] = data }

        // Evolution trees
        jsonFileString = AssetUtils.getJsonDataFromAsset(context, "pokemon_evolution_trees.json")
        val evolutionTreeListType = object : TypeToken<List<DataEvolutionTree>>() {}.type
        evolutionTrees = Gson().fromJson(jsonFileString, evolutionTreeListType)
    }

    public fun processData() {
        pokemonMap.forEach { (key, data) ->

        }
    }

    public fun getPokemonMap() : Map<String, DataPokemon> {
        return pokemonMap
    }

    public fun buildPokemonIdsList() : List<String> {
        val idList = ArrayList<String>()
        pokemonMap.forEach { (key, data) -> idList.add(key) }

        return idList
    }

    public fun getLocalizedPokemonName(context: Activity, id: String, language: String = ""): String? {

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

    public fun findPokemonData(id: String): DataPokemon? {
        return pokemonMap[id]
    }

//    public fun findPokemonDataPaldea(paldea_id: String): DataPokemon? {
//        pokemonMap.forEach { (key, data) ->
//            if (data.ids.paldea == paldea_id)
//                return data
//        }
//
//        return null
//    }

    public fun findEvolutionTree(id: String): DataEvolutionTree? {
        evolutionTrees.forEach{ tree ->
            val node = findEvolutionTreeNode(tree, id)
            if (node != null)
                return tree
        }

        return null
    }

    public fun findEvolutionTreeNode(node: DataEvolutionTree, id: String): DataEvolutionTree? {
        if (node.id == id)
            return node

        if (node.evolutions == null)
            return null

        node.evolutions.forEach{ child ->
            val found = findEvolutionTreeNode(child, id)
            if (found != null)
                return found
        }

        return null
    }
}