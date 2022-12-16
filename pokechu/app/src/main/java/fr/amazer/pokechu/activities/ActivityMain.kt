package fr.amazer.pokechu.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.data.NationalIdLocalId
import fr.amazer.pokechu.data.PokedexType
import fr.amazer.pokechu.data.Regions
import fr.amazer.pokechu.databinding.ActivityMainBinding
import fr.amazer.pokechu.fragments.StartSearchDialogFragment
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.ListAdapter
import fr.amazer.pokechu.ui.ListAdapterData
import fr.amazer.pokechu.ui.RecyclerTouchListener
import fr.amazer.pokechu.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap


class ActivityMain : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var menu: Menu

    private var adapter: ListAdapter? = null

    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var detailsActivityLauncher: ActivityResultLauncher<Intent>

    private lateinit var regions: List<Regions>

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // Initialize ui
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Bottom search button
        binding.buttonSearch.setOnClickListener { view ->
            val newFragment = StartSearchDialogFragment()
            newFragment.show(supportFragmentManager, "search")
        }

        // Discovered count
        suspend fun getPokemonsCount(): Int = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findPokemonsCount()
        }
        lifecycleScope.launch { // coroutine on main
            val totalPokemonCount = getPokemonsCount() // coroutine on IO
            // back on main
            val discoveredCount = SettingsManager.getPokemonDiscoveredCount()
            binding.discoveredCount.text = "${discoveredCount}/${totalPokemonCount}"
        }

        // Get regions
        suspend fun getRegions(): List<Regions> = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findRegions()
        }
        lifecycleScope.launch { // coroutine on main
            // Find regions
            regions = getRegions() // coroutine on IO
            // back on main
        }

        setUpRecyclerView()

        // Create launcher for activities details/settings + notify data changed when they close
        settingsActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            adapter?.notifyDataSetChanged()
        }

        detailsActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        this.menu = menu

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        //MenuCompat.setGroupDividerEnabled(menu, true)

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
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    Log.i(this::class.simpleName, "onMenuItemActionExpand")
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    // Clear text and apply
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

        val searchAllFields = SettingsManager.isSearchAllFieldsEnabled(applicationContext)
        val searchAllMenuItem = menu.findItem(R.id.search_all_fields)
        searchAllMenuItem.isChecked = searchAllFields

        // Add regions filter submenu
        val selectedRegion = SettingsManager.getSelectedRegion()
        val filterSubMenuItem = menu.findItem(R.id.button_filter)
        menuInflater.inflate(R.menu.menu_main_filter, filterSubMenuItem.subMenu)
        regions.forEach{ region ->
            val resId: Int = applicationContext.resources.getIdentifier("region_${region.id}", "string", "fr.amazer.pokechu")
            filterSubMenuItem.subMenu.add(0, region.id, region.id, resId)
            val menuItem = filterSubMenuItem.subMenu[region.id]
            menuItem.isCheckable = true
            menuItem.isChecked = (region.id == selectedRegion)
        }

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
            R.id.search_all_fields -> {
                item.isChecked = !item.isChecked
                SettingsManager.setSearchAllFieldsEnabled(applicationContext,item.isChecked)
                return true
            }
            in PokedexType.NATIONAL.ordinal .. PokedexType.PALDEA.ordinal -> {
                val regionId = PokedexType.values()[item.itemId]
                SettingsManager.setSelectedRegion(regionId)
//                adapter?.notifyDataSetChanged()
                UIUtils.reloadActivity(this, true)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setUpRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)

        // Grid or list
        val gridEnabled = !SettingsManager.isListViewEnabled()
        var layoutManager: RecyclerView.LayoutManager? = null
        if ( gridEnabled )
            layoutManager = GridLayoutManager(applicationContext, 2)
        else
            layoutManager  = LinearLayoutManager(applicationContext)

        recyclerView.layoutManager = layoutManager

        // Get pokemons for selected region and build map of id -> adapter data
        val selectedRegion = SettingsManager.getSelectedRegion()

        suspend fun getPokemonData(region: Int): Map<Int, ListAdapterData> = withContext(Dispatchers.IO) {
            if (region == PokedexType.NATIONAL.ordinal) {
                val pokemonIds: List<Int> = DatabaseManager.findPokemonIds()
                val pokemonData = ArrayList<ListAdapterData>()
                pokemonIds.forEach{ id -> pokemonData.add(ListAdapterData(id))}

                val dataMap = pokemonIds.zip(pokemonData).toMap()
                return@withContext dataMap
            }
            else {
                val pokemonLocalIds: List<NationalIdLocalId> = DatabaseManager.findLocalIdsByRegion(selectedRegion)
                val dataMap = HashMap<Int, ListAdapterData>()
                pokemonLocalIds.forEach{ id -> dataMap[id.pokemon_id] = ListAdapterData(id.local_id)}
                return@withContext dataMap
            }
        }
        lifecycleScope.launch { // coroutine on main
            val pokemonData = getPokemonData(selectedRegion) // coroutine on IO
            // back on main
            val uiItemId = if (gridEnabled) R.layout.list_grid_item else R.layout.list_item

            adapter = ListAdapter(applicationContext, pokemonData, uiItemId)
            recyclerView.adapter = adapter

            // Add click/long click listeners on items
            recyclerView.addOnItemTouchListener(
                RecyclerTouchListener(
                    applicationContext,
                    recyclerView,
                    object : RecyclerTouchListener.ClickListener {

                        // Open details activity on click
                        override fun onClick(view: View?, position: Int) {
                            val pokemonId = adapter?.getCurrentIds()?.get(position)
                            val intent = Intent(applicationContext, ActivityDetails::class.java)
                            intent.putExtra("PokemonId", pokemonId)

                            detailsActivityLauncher.launch(intent)
                        }

                        // Toggle discovered status on long click
                        override fun onLongClick(view: View?, position: Int) {
                            val pokemonId = adapter?.getCurrentIds()?.get(position)
                            if (pokemonId != null) {
                                SettingsManager.togglePokemonDiscovered(pokemonId)
                                adapter?.notifyItemChanged(position)
                            }
                        }
                    })
            )
        }
    }

    private fun filterQuery(text: String?) {
        val pokemonIds = adapter?.getAllIds()

        val filteredIds = ArrayList<Int>()
        if (pokemonIds != null) {
            for (id in pokemonIds) {
                val localizedNameFr = LocalizationManager.getLocalizedPokemonName(this, id, "fr")
                val localizedNameEn = LocalizationManager.getLocalizedPokemonName(this, id, "en")

                val found0 = id.toString().lowercase(Locale.getDefault()).contains(text!!)
//                val found1 = pokemonData.ids.paldea.lowercase(Locale.getDefault()).contains(text!!)
//                val found2 = pokemonData.ids.unique.lowercase(Locale.getDefault()).contains(text!!)
                val found3 = localizedNameFr?.lowercase(Locale.getDefault())?.contains(text!!)
                val found4 = localizedNameEn?.lowercase(Locale.getDefault())?.contains(text!!)

                if ( found0 || found3 == true || found4 == true) {
                    filteredIds.add(id)
                }
            }
        }
        adapter!!.setFilter(filteredIds)
    }
}