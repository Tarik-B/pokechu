package fr.amazer.pokechu.ui.details

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ActivityDetailsBinding
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.BaseActivity
import fr.amazer.pokechu.ui.FlingHelper
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.viewmodel.ViewModelEvolutions
import fr.amazer.pokechu.viewmodel.ViewModelPokemon


class ActivityDetails : BaseActivity() {
    private lateinit var binding: ActivityDetailsBinding

    private var pokemonId: Int = 0

    private val viewModelPokemon: ViewModelPokemon by viewModels()
    private val viewModelEvolutions: ViewModelEvolutions by viewModels()
    private lateinit var flingHelper: FlingHelper

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

        // Animated collapsing image
        flingHelper = FlingHelper(applicationContext,
            binding.appBarLayout,
            binding.toolbar,
            binding.imageHeader,
            binding.workaround)

        updateActionBarTitle(pokemonId)

        viewModelPokemon.getPokemonId().observe(this) { id ->
            // Image
            var thumbnailImgPath = AssetUtils.getPokemonThumbnailPath(id)
            binding.imagePath = thumbnailImgPath

            updateActionBarTitle(id)

            // Update image filter on discovered status modifications
            viewModelPokemon.getPokemonsDiscovered().observe(this){
                updateDiscovered(id)
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_details, menu)

        return true
    }

    private fun updateActionBarTitle(id: Int) {
        val localizedName = LocalizationManager.getPokemonName(id)
//        supportActionBar?.title = "#${id} - ${localizedName}"
//        binding.toolbar.title = "#${id} - ${localizedName}"
        binding.collapsingLayout.title ="#${id} - ${localizedName}"
    }

    private fun updateDiscovered(id: Int) {
        // For black filter
        val isDiscovered = SettingsManager.isPokemonDiscovered(id)
        binding.isDiscovered = isDiscovered
    }
}