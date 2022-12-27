package fr.amazer.pokechu.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import fr.amazer.pokechu.R
import fr.amazer.pokechu.activities.compose.ActivityAboutCompose
import fr.amazer.pokechu.data.PokedexType
import fr.amazer.pokechu.data.Pokemon
import fr.amazer.pokechu.databinding.ActivityMainBinding
import fr.amazer.pokechu.fragments.FragmentBottomSheet
import fr.amazer.pokechu.fragments.FragmentList
import fr.amazer.pokechu.fragments.FragmentStartSearchDialog
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ActivityMain : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentList: FragmentList
    private lateinit var fragmentBottomSheet: FragmentBottomSheet
    private lateinit var menu: Menu
    private lateinit var loadingOverlay: View
    private lateinit var detailsActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // Initialize ui
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Show loading overlay (with animation):
        loadingOverlay = binding.loadOverlay.loadingFrame
        showLoadingOverlay()
        fragmentList = binding.fragmentList.getFragment<FragmentList>()
        fragmentList.addLoadedObserver { loaded ->
            // Hide it (with animation) when list is loaded
            if (loaded)
                hideLoadingOverlay()
            else
                showLoadingOverlay()
        }
        // Refresh captured/discovered counts
        refreshCounts()
        fragmentList.addDataChangedObserver{ ->
            refreshCounts()
        }

        fragmentBottomSheet = binding.fragmentBottomSheet.getFragment<FragmentBottomSheet>()

        // Create launcher for activities details/settings
        detailsActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { _ ->
        }
        settingsActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { _ ->
            fragmentList.notifyDataSetChanged()
        }

        // Bottom search button
        binding.buttonSearch.setOnClickListener { view ->
            val newFragment = FragmentStartSearchDialog()
            newFragment.addsearchQueryListeners { pokemonId, isNational ->
                // Search id and open details
                suspend fun getPokemonById(id: Int): Pokemon? = withContext(Dispatchers.IO) {
                    return@withContext DatabaseManager.findPokemonById(id)
                }
                suspend fun localToNational(regionId: Int, localId: Int): Int = withContext(Dispatchers.IO) {
                    return@withContext DatabaseManager.localToNationalId(regionId, localId)
                }
                lifecycleScope.launch { // coroutine on Main

                    var searchedId = pokemonId

                    // Check if id must be converted to national (unique id not checked)
                    val selectedRegion = SettingsManager.getSelectedRegion()
                    if (!isNational && selectedRegion != PokedexType.NATIONAL.ordinal) {
                        searchedId = localToNational(selectedRegion, searchedId)
                    }

                    val pokemon = getPokemonById(searchedId) // coroutine on IO
                    // back on main

                    // Id found, open details
                    if (pokemon != null) {
                        val intent = Intent(applicationContext, ActivityDetails::class.java)
                        intent.putExtra("PokemonId", searchedId)
                        detailsActivityLauncher.launch(intent)
                    }
                    else {
                        Snackbar.make(view, "ID ${pokemonId} not found ", Snackbar.LENGTH_LONG).show()
                    }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        this.menu = menu

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            menu.setGroupDividerEnabled(true)
        }

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = (menu.findItem(R.id.search).actionView as SearchView)
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        // Setup search bar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                filterQuery(newText)
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })

        menu.findItem(R.id.search).setOnActionExpandListener( object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                Log.i(this::class.simpleName, "onMenuItemActionExpand")
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                searchView.setQuery("", true)
                return true
            }
        })

        // Setup customize menu
        val customizeButton = menu.findItem(R.id.button_customize)
        customizeButton.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                Log.i(this::class.simpleName, "onMenuItemActionExpand")
                return true
            }
        })

        // Setup list/grid switch view button
        val gridEnabled = !SettingsManager.isListViewEnabled()
        val listButton = menu.findItem(R.id.button_list_view)
        val gridButton = menu.findItem(R.id.button_grid_view)
        val currentActivity = this
        gridButton.isVisible = !gridEnabled
        listButton.isVisible = gridEnabled
        if ( gridEnabled ) {
            listButton.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    SettingsManager.setListViewEnabled(true)
                    UIUtils.reloadActivity(currentActivity, true)

                    return true
                }
            })
        }
        else {
            gridButton.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    SettingsManager.setListViewEnabled(false)
                    UIUtils.reloadActivity(currentActivity, true)

                    return true
                }
            })
        }


        // Required to make the searchview manually focusable
        searchView.isIconifiedByDefault = false

        // Add customize search submenu
        val customizeSubMenuItem = menu.findItem(R.id.button_customize)
        menuInflater.inflate(R.menu.menu_main_customize, customizeSubMenuItem.subMenu)

        val showUndiscoveredInfoItem = menu.findItem(R.id.show_undiscovered_info)
        showUndiscoveredInfoItem.isChecked = SettingsManager.isShowUndiscoveredInfoEnabled()

        val showDiscoveredOnlyItem = menu.findItem(R.id.show_discovered_only)
        showDiscoveredOnlyItem.isChecked = SettingsManager.isShowDiscoveredOnlyEnabled()

        val showCapturedOnlyItem = menu.findItem(R.id.show_captured_only)
        showCapturedOnlyItem.isChecked = SettingsManager.isShowCapturedOnlyEnabled()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle action bar item clicks
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, ActivitySettings::class.java)
                settingsActivityLauncher.launch(intent)
                return true
            }
            R.id.action_about -> {
                val intent = Intent(this, ActivityAbout::class.java)
                settingsActivityLauncher.launch(intent)
                return true
            }
            // TODO refactor this into a generic case
            R.id.show_undiscovered_info -> {
                item.isChecked = !item.isChecked
                SettingsManager.setShowUndiscoveredInfoEnabled(item.isChecked)
                fragmentList.notifyDataSetChanged()

                return true
            }
            R.id.show_discovered_only -> {
                item.isChecked = !item.isChecked
                SettingsManager.setShowDiscoveredOnlyEnabled(item.isChecked)
                fragmentList.rebuildDataSet()

                return true
            }
            R.id.show_captured_only -> {
                item.isChecked = !item.isChecked
                SettingsManager.setShowCapturedOnlyEnabled(item.isChecked)
                fragmentList.rebuildDataSet()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showLoadingOverlay() {
        loadingOverlay.visibility = View.VISIBLE
        loadingOverlay.alpha = 1.0f
//        UIUtils.animateView(loadingOverlay, View.VISIBLE, 1.0f, 100);
    }

    private fun hideLoadingOverlay() {
        UIUtils.animateView(loadingOverlay, View.GONE, 0.0f, 100)
    }

    private fun filterQuery(text: String?) {
        fragmentList.filterQuery(text)
    }

    private fun refreshCounts() {
        // Get data
        suspend fun getPokemonsCount(): Int = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findPokemonsCount()
        }
        lifecycleScope.launch { // coroutine on main
            val totalPokemonCount = getPokemonsCount() // coroutine on IO
            // back on main

            // Discovered
            val discoveredCount = SettingsManager.getPokemonDiscoveredCount()
            binding.discoveredCount.text = "${discoveredCount}/${totalPokemonCount}"

            // Captured
            val capturedCount = SettingsManager.getPokemonCapturedCount()
            binding.capturedCount.text = "${capturedCount}/${totalPokemonCount}"
        }
    }
}