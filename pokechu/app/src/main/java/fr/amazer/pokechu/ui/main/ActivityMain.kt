package fr.amazer.pokechu.ui.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.google.android.material.snackbar.Snackbar
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ActivityMainBinding
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.BaseActivity
import fr.amazer.pokechu.ui.about.ActivityAbout
import fr.amazer.pokechu.ui.details.ActivityDetails
import fr.amazer.pokechu.ui.settings.ActivitySettings
import fr.amazer.pokechu.viewmodel.ViewModelPokemons


class ActivityMain : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentList: FragmentList
    private lateinit var fragmentBottomSheet: FragmentBottomSheet
    private lateinit var menu: Menu
    private lateinit var loadingOverlay: View

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    private val viewModel: ViewModelPokemons by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // Initialize ui
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Show loading overlay (with animation):
        loadingOverlay = binding.loadOverlay.loadingFrame
//        showLoadingOverlay()
        fragmentList = binding.fragmentList.getFragment<FragmentList>()
        fragmentList.addLoadedObserver { loaded ->
            // Hide it (with animation) when list is loaded
            binding.isLoading = !loaded
        }

        // Observe captured/discovered/total counts and pass them to binding
        viewModel.getPokemonCount().observe(this) { count ->
            binding.totalCount = count
        }
        viewModel.getPokemonDiscoveredCount().observe(this) { count ->
            binding.discoveredCount = count
        }
        viewModel.getPokemonCapturedCount().observe(this) { count ->
            binding.capturedCount = count
        }

        fragmentBottomSheet = binding.fragmentBottomSheet.getFragment<FragmentBottomSheet>()

        // Bottom search button
        binding.buttonSearch.setOnClickListener { view ->
            val newFragment = FragmentStartSearchDialog()
            newFragment.addsearchQueryListeners { pokemonId, isNational ->

                fun openDetails(searchedId: Int) {
                    // Id found, open details
                    if (searchedId > 0) {
                        val intent = Intent(applicationContext, ActivityDetails::class.java)
                        intent.putExtra("PokemonId", searchedId)

                        startActivity(intent)
                    }
                    else {
                        Snackbar.make(view, "ID ${pokemonId} not found ", Snackbar.LENGTH_LONG).show()
                    }
                }

                var searchedId = pokemonId

                // Check if id must be converted to national (unique id not checked)
                val selectedRegion = SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION)
                if (!isNational && selectedRegion != Region.NATIONAL.ordinal) {
                    viewModel.localToNationalId(selectedRegion, searchedId).observe(this) { nationalId ->
                        openDetails(nationalId)
                    }
                }
                else {
                    openDetails(searchedId)
                }
            }

            newFragment.show(supportFragmentManager, "search")
        }

        // Override back press on bottom sheet
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (fragmentBottomSheet.isExpanded()) {
                    fragmentBottomSheet.toggleExpanded()
                }
                else {
                    finish()
                }
            }
        })
    }

    // Map of menu item id -> preference to toggle
    private val MENU_ITEM_ID_TO_PREFERENCE_TYPE = mapOf(
        R.id.show_undiscovered_info to PreferenceType.SHOW_UNDISCOVERED_INFO,
        R.id.show_discovered_only to PreferenceType.SHOW_DISCOVERED_ONLY,
        R.id.show_captured_only to PreferenceType.SHOW_CAPTURED_ONLY
    )

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        this.menu = menu

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = (menu.findItem(R.id.search).actionView as SearchView)
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        // Setup search bar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                fragmentList.filterQuery(newText)
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })

        menu.findItem(R.id.search).setOnActionExpandListener( object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                searchView.setQuery("", true)
                return true
            }
        })

        // Setup list/grid switch view button
        val listButton = menu.findItem(R.id.button_list_view)
        val gridButton = menu.findItem(R.id.button_grid_view)
        viewModel.getListViewEnabled().observe(this) { enabled ->
            gridButton.isVisible = enabled
            listButton.isVisible = !enabled
        }
        listButton.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                SettingsManager.setSetting(PreferenceType.LIST_VIEW, true)
                return true
            }
        })
        gridButton.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                SettingsManager.setSetting(PreferenceType.LIST_VIEW, false)
                return true
            }
        })

        // Required to make the searchview manually focusable
        searchView.isIconifiedByDefault = false

        // Add customize search submenu
        val customizeSubMenuItem = menu.findItem(R.id.button_customize)
        menuInflater.inflate(R.menu.menu_main_customize, customizeSubMenuItem.subMenu)

        // Initialize menu item checked status according to preference value
        MENU_ITEM_ID_TO_PREFERENCE_TYPE.forEach { (itemId, preferenceType) ->
            viewModel.getLiveSetting<Boolean>(preferenceType).observe(this) { value ->
                val menuItem = menu.findItem(itemId)
                menuItem.isChecked = SettingsManager.getSetting(preferenceType)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Map of menu item id -> activity to launch
        val itemIdToLaunchActivity = mapOf(
            R.id.action_settings to ActivitySettings::class.java,
            R.id.action_about to ActivityAbout::class.java
        )

        // Handle action bar item clicks
        when (item.itemId) {
            in itemIdToLaunchActivity -> {
                val intent = Intent(this, itemIdToLaunchActivity[item.itemId])
                startActivity(intent)
                return true
            }
            in MENU_ITEM_ID_TO_PREFERENCE_TYPE -> {
                item.isChecked = !item.isChecked
                SettingsManager.setSetting(MENU_ITEM_ID_TO_PREFERENCE_TYPE[item.itemId]!!, item.isChecked)

                // If show discovered/captured only is checked, uncheck the other
                if (item.isChecked) {
                    if (MENU_ITEM_ID_TO_PREFERENCE_TYPE[item.itemId]!! == PreferenceType.SHOW_DISCOVERED_ONLY)
                        SettingsManager.setSetting(PreferenceType.SHOW_CAPTURED_ONLY, false)
                    else if (MENU_ITEM_ID_TO_PREFERENCE_TYPE[item.itemId]!! == PreferenceType.SHOW_CAPTURED_ONLY)
                        SettingsManager.setSetting(PreferenceType.SHOW_DISCOVERED_ONLY, false)
                }

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}