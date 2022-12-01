package com.example.pokechu_material3.managers

import android.content.Context
import com.example.pokechu_material3.data.EvolutionTreeData
import com.example.pokechu_material3.data.PokemonData
import com.example.pokechu_material3.utils.AssetUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object PokemonManager {
    private var pokemonMap = HashMap<String, PokemonData>()
    private var evolutionTrees = ArrayList<EvolutionTreeData>()

    public fun loadJsonData(context: Context) {
        // Pokemon list
        var jsonFileString = AssetUtils.getJsonDataFromAsset(context, "pokemon_list.json")
        val pokemonDictType = object : TypeToken<List<PokemonData>>() {}.type
        val pokemonList: List<PokemonData> = Gson().fromJson(jsonFileString, pokemonDictType)
        pokemonList.forEach { data -> pokemonMap[data.ids.unique] = data }

        // Evolution trees
        jsonFileString = AssetUtils.getJsonDataFromAsset(context, "pokemon_evolution_trees.json")
        val evolutionTreeListType = object : TypeToken<List<EvolutionTreeData>>() {}.type
        evolutionTrees = Gson().fromJson(jsonFileString, evolutionTreeListType)
    }

    public fun getPokemonMap() : Map<String, PokemonData> {
        return pokemonMap
    }

    public fun getPokemonIds() : List<String> {
        val idList = ArrayList<String>()
        pokemonMap.forEach { (key, data) -> idList.add(key) }

        return idList
    }

    public fun findPokemonData(id: String): PokemonData? {
        return pokemonMap[id]
    }

    public fun findPokemonDataPaldea(paldea_id: String): PokemonData? {
        pokemonMap.forEach { (key, data) ->
            if (data.ids.paldea == paldea_id)
                return data
        }

        return null
    }

    public fun findEvolutionTree(id: String): EvolutionTreeData? {
        evolutionTrees.forEach{ tree ->
            val node = findEvolutionTreeNode(tree, id)
            if (node != null)
                return tree
        }

        return null
    }

    public fun findEvolutionTreeNode(node: EvolutionTreeData, id: String): EvolutionTreeData? {
        if (node.id == id)
            return node

        node.evolutions.forEach{ child ->
            val found = findEvolutionTreeNode(child, id)
            if (found != null)
                return found
        }

        return null
    }
}