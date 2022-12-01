package com.example.pokechu_material3.managers

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object SettingsManager {
    public fun isPokemonDiscovered(context: Context, pokemonId: String): Boolean {
        val prefs = context.getSharedPreferences("Data", Context.MODE_PRIVATE)
        val discovered = prefs.getBoolean("pokemon_${pokemonId}_discovered", false)

        return discovered
    }

    public fun setPokemonDiscovered(context: Context, pokemonId: String, discovered: Boolean) {
        val prefs = context.getSharedPreferences("Data", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pokemon_${pokemonId}_discovered", discovered).apply()
    }

    public fun togglePokemonDiscovered(context: Context, pokemonId: String) {
        val discovered = isPokemonDiscovered(context, pokemonId)
        setPokemonDiscovered(context,pokemonId, !discovered )
    }

    public fun isSearchAllFieldsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("search_all_fields", true)

        return enabled
    }

    public fun setSearchAllFieldsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("search_all_fields", enabled).apply()
    }
}