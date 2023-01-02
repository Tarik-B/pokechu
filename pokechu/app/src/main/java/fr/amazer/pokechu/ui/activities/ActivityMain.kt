package fr.amazer.pokechu.ui.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.google.android.material.snackbar.Snackbar
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ActivityMainBinding
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.managers.SettingType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.fragments.FragmentBottomSheet
import fr.amazer.pokechu.ui.fragments.FragmentStartSearchDialog
import fr.amazer.pokechu.ui.fragments.list.FragmentList
import fr.amazer.pokechu.utils.UIUtils
import fr.amazer.pokechu.viewmodel.ViewModelPokemons


class ActivityMain : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentList: FragmentList
    private lateinit var fragmentBottomSheet: FragmentBottomSheet
    private lateinit var menu: Menu
    private lateinit var loadingOverlay: View
    private lateinit var detailsActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>

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

                fun openDetails(searchedId: Int) {
                    // Id found, open details
                    if (searchedId > 0) {
                        val intent = Intent(applicationContext, ActivityDetails::class.java)
                        intent.putExtra("PokemonId", searchedId)
                        detailsActivityLauncher.launch(intent)
                    }
                    else {
                        Snackbar.make(view, "ID ${pokemonId} not found ", Snackbar.LENGTH_LONG).show()
                    }
                }

                var searchedId = pokemonId

                // Check if id must be converted to national (unique id not checked)
                val selectedRegion = SettingsManager.getSetting<Int>(SettingType.SELECTED_REGION)
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
        val gridEnabled = !SettingsManager.getSetting<Boolean>(SettingType.LIST_VIEW)
        val listButton = menu.findItem(R.id.button_list_view)
        val gridButton = menu.findItem(R.id.button_grid_view)
        val currentActivity = this
        gridButton.isVisible = !gridEnabled
        listButton.isVisible = gridEnabled
        if ( gridEnabled ) {
            listButton.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    SettingsManager.setSetting(SettingType.LIST_VIEW, true)
                    UIUtils.reloadActivity(currentActivity, true)

                    return true
                }
            })
        }
        else {
            gridButton.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    SettingsManager.setSetting(SettingType.LIST_VIEW, false)
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
        showUndiscoveredInfoItem.isChecked = SettingsManager.getSetting<Boolean>(SettingType.SHOW_UNDISCOVERED_INFO)

        val showDiscoveredOnlyItem = menu.findItem(R.id.show_discovered_only)
        showDiscoveredOnlyItem.isChecked = SettingsManager.getSetting<Boolean>(SettingType.SHOW_DISCOVERED_ONLY)

        val showCapturedOnlyItem = menu.findItem(R.id.show_captured_only)
        showCapturedOnlyItem.isChecked = SettingsManager.getSetting<Boolean>(SettingType.SHOW_CAPTURED_ONLY)

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
                SettingsManager.setSetting(SettingType.SHOW_UNDISCOVERED_INFO, item.isChecked)
                fragmentList.notifyDataSetChanged()

                return true
            }
            R.id.show_discovered_only -> {
                item.isChecked = !item.isChecked
                SettingsManager.setSetting(SettingType.SHOW_DISCOVERED_ONLY, item.isChecked)
//                fragmentList.rebuildDataSet()

                return true
            }
            R.id.show_captured_only -> {
                item.isChecked = !item.isChecked
                SettingsManager.setSetting(SettingType.SHOW_CAPTURED_ONLY, item.isChecked)
//                fragmentList.rebuildDataSet()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}