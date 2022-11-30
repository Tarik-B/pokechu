package com.example.pokechu_material3.managers

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.pokechu_material3.PokemonData
import com.example.pokechu_material3.utils.AssetUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object PokemonManager {
    private var pokemonMap = HashMap<String, PokemonData>()

    public fun loadJsonData(context: Context) {
        val jsonFileString = AssetUtils.getJsonDataFromAsset(context, "pokemon_list.json")
        val pokemonDictType = object : TypeToken<List<PokemonData>>() {}.type

        val pokemonList: List<PokemonData> = Gson().fromJson(jsonFileString, pokemonDictType)
        pokemonList.forEach { data -> pokemonMap[data.ids.unique] = data }
    }

    public fun getPokemonMap() : Map<String, PokemonData> {
        return pokemonMap
    }

    public fun getPokemonIds() : List<String> {
        val idList = ArrayList<String>()
        pokemonMap.forEach { (key, data) -> idList.add(key) }

        return idList
    }

    public fun findData(id: String): PokemonData? {
        return pokemonMap[id]
    }

    public fun isDiscovered(context: Context, pokemonId: String): Boolean {
        val prefs = context.getSharedPreferences("Data", MODE_PRIVATE)
        val discovered = prefs.getBoolean("pokemon_${pokemonId}_discovered", false)

        return discovered
    }

    public fun setIsDiscovered(context: Context, pokemonId: String, discovered: Boolean) {
        val prefs = context.getSharedPreferences("Data", MODE_PRIVATE)
        prefs.edit().putBoolean("pokemon_${pokemonId}_discovered", discovered).apply()
    }
}