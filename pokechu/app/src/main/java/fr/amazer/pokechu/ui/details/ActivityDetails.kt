package fr.amazer.pokechu.ui.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ActivityDetailsBinding
import fr.amazer.pokechu.enums.DetailsView
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.BaseActivity
import fr.amazer.pokechu.ui.FlingHelper
import fr.amazer.pokechu.ui.SwipeTouchListener
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.viewmodel.ViewModelEvolutionData
import fr.amazer.pokechu.viewmodel.ViewModelEvolutions
import fr.amazer.pokechu.viewmodel.ViewModelPokemon

class ActivityDetails : BaseActivity() {
    private lateinit var binding: ActivityDetailsBinding

    private var pokemonId: Int = 0

    private val viewModelPokemon: ViewModelPokemon by viewModels()
    private val viewModelEvolutions: ViewModelEvolutions by viewModels()
    private lateinit var flingHelper: FlingHelper

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pokemonId = intent.getIntExtra("PokemonId", 0)
        viewModelPokemon.setPokemonId(pokemonId)
        viewModelEvolutions.setPokemonId(pokemonId)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bottom nav bar
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_container
        ) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.details_nav_graph)
        // Restore last displayed fragment
        val savedDestination = SettingsManager.getSetting<Int>(PreferenceType.DETAILS_START_VIEW)
        val startDestination = when(savedDestination) {
            DetailsView.GENERAL_INFO.ordinal -> R.id.generalInfo
            DetailsView.EVOLUTION_TREE.ordinal -> R.id.evolutionTree
            else -> -1
        }
        if (startDestination != -1)
            navGraph.setStartDestination(startDestination)
        navController.graph = navGraph

        // Setup the bottom navigation view with navController
        val bottomNavigationView = binding.bottomNav
        bottomNavigationView.setupWithNavController(navController)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Animated collapsing image
        flingHelper = FlingHelper(applicationContext,
            binding.appBarLayout,
            binding.toolbar,
            binding.imageHeader,
            binding.workaround,
            1)

        // Update action bar title/image/filter
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

        // Left/right swipe for fragment navigation
        binding.root.setOnTouchListener(object: SwipeTouchListener(applicationContext) {
            override fun onSwipeLeft() {
                navController.navigate(R.id.action_general_info_to_evolution_tree)
            }
            override fun onSwipeRight() {
                navController.navigate(R.id.action_evolution_tree_to_general_info)
            }
        })

        // Update fragment title on change
        navController.addOnDestinationChangedListener(object: NavController.OnDestinationChangedListener{
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                binding.fragmentTitle = destination.label.toString()
            }
        })

        binding.imageHeader.setOnLongClickListener { _ ->
            SettingsManager.togglePokemonDiscovered(pokemonId)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_details, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun updateActionBarTitle(id: Int) {
        val localizedName = LocalizationManager.getPokemonName(id)
        binding.name ="#${id} - ${localizedName}"
    }

    private fun updateDiscovered(id: Int) {
        // For black filter
        val isDiscovered = SettingsManager.isPokemonDiscovered(id)
        binding.isDiscovered = isDiscovered
    }

    override fun onBackPressed() {
        // Prevent navController from catching back (to pop last fragment in its stack)
//        if(navController.graph.startDestinationId == navController.currentDestination?.id) {
//            finish()
        supportFinishAfterTransition()
//        } else {
//            super.onBackPressed()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Save last displayed fragment
        val currentDestination = navController.currentDestination
        if (currentDestination != null) {
            val savedDestination = when (currentDestination.id) {
                R.id.generalInfo -> DetailsView.GENERAL_INFO.ordinal
                R.id.evolutionTree -> DetailsView.EVOLUTION_TREE.ordinal
                else -> -1
            }

            if (savedDestination != -1)
                SettingsManager.setSetting<Int>(PreferenceType.DETAILS_START_VIEW, savedDestination)
        }
    }
}