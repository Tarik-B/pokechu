package fr.amazer.pokechu.ui.details

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ActivityDetailsBinding
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.ui.BaseActivity
import fr.amazer.pokechu.viewmodel.ViewModelEvolutions
import fr.amazer.pokechu.viewmodel.ViewModelPokemon


class ActivityDetails : BaseActivity() {
    private lateinit var binding: ActivityDetailsBinding

    private var pokemonId: Int = 0

    private val viewModelPokemon: ViewModelPokemon by viewModels()
    private val viewModelEvolutions: ViewModelEvolutions by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pokemonId = intent.getIntExtra("PokemonId", 0)
        viewModelPokemon.setPokemonId(pokemonId)
        viewModelEvolutions.setPokemonId(pokemonId)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        updateActionBarTitle(pokemonId)
        viewModelPokemon.getPokemonId().observe(this) { id ->
            updateActionBarTitle(id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_details, menu)

        return true
    }

    private fun updateActionBarTitle(pokemonId: Int) {
        val localizedName = LocalizationManager.getPokemonName(pokemonId)
        supportActionBar?.title = "#${pokemonId} - ${localizedName}"
    }
}