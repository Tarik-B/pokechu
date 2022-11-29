package com.example.pokechu_material3

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PokemonManager {
    private var pokemons: List<PokemonData> = ArrayList<PokemonData>()

    public fun getPokemons():List<PokemonData> {
        return pokemons
    }

    public fun loadJsonData(context: Context) {
        val jsonFileString = Utils.getJsonDataFromAsset(context, "pokemon_list.json")
        val pokemonDictType = object : TypeToken<List<PokemonData>>() {}.type

        pokemons = Gson().fromJson(jsonFileString, pokemonDictType)
        //pokemons.forEachIndexed { idx, pokemon -> Log.i("data", "> Item $idx:\n$person") }
        //pokemons.forEach { (key, value) -> println("$key = $value") }
        /*pokemons.forEach { entry ->
            //print("${entry.key} : ${entry.value.names.fr}")
            Log.d("TAG", "Pokemon ${entry.ids.unique} = ${entry.names.fr}" )
        }*/
    }
}