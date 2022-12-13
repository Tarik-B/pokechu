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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.data.DataPokemon
import fr.amazer.pokechu.databinding.ActivityMainBinding
import fr.amazer.pokechu.fragments.StartSearchDialogFragment
import fr.amazer.pokechu.managers.DataManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.ListAdapter
import fr.amazer.pokechu.ui.RecyclerTouchListener
import fr.amazer.pokechu.utils.UIUtils
import java.util.*


class ActivityMain : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var menu: Menu

    private var adapter: ListAdapter? = null

    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var detailsActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        DataManager.loadJsonData(applicationContext)
//        SettingsManager.with(applicationContext)

        // Initialize ui
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.buttonSearch.setOnClickListener { view ->
            val newFragment = StartSearchDialogFragment()
            newFragment.show(supportFragmentManager, "test")

            /*
            // Put focus on search view
            val searchMenuItem = menu.findItem(R.id.search)
            searchMenuItem.expandActionView()
            menu.performIdentifierAction(R.id.search, 0)
            val searchView = (menu.findItem(R.id.search).actionView as SearchView)
            //searchView.requestFocus()
            //searchView.isFocusable = true
            searchView.requestFocus()
            //searchView.focusSearch(View.FOCUS_RIGHT)
             */
        }

        setUpRecyclerView()

        val discoveredCount = SettingsManager.getPokemonDiscoveredCount()
        val totalPokemonCount = DataManager.getPokemonMap().count()
        binding.discoveredCount.text = "${discoveredCount}/${totalPokemonCount}"

        /*
        Searchtext = view.findViewById<View>(R.id.search_input) as EditText
        Searchtext!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                filterQuery(editable.toString())
            }
        })*/

        // Associate searchable configuration with the SearchView

        settingsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//            }
            adapter?.notifyDataSetChanged()
        }

        detailsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//            }
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

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                Log.i(this::class.simpleName, "onQueryTextChange = $newText")

                filterQuery(newText)

                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i(this::class.simpleName, "onQueryTextSubmit = $query")
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

        val customizeButton = menu.findItem(R.id.button_customize)
        customizeButton.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                Log.i(this::class.simpleName, "onMenuItemActionExpand")
                return true
            }
        })

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

        // Added customize search submenu
        val subMenuItem = menu.findItem(R.id.button_customize)
        menuInflater.inflate(R.menu.menu_main_filter, subMenuItem.subMenu)

        val searchAllFields = SettingsManager.isSearchAllFieldsEnabled(applicationContext)
        val searchAllMenuItem = menu.findItem(R.id.search_all_fields)
        searchAllMenuItem.isChecked = searchAllFields


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setUpRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)

        val gridEnabled = !SettingsManager.isListViewEnabled()
        var layoutManager: RecyclerView.LayoutManager? = null
        if ( gridEnabled )
            layoutManager = GridLayoutManager(applicationContext, 2)
        else
        layoutManager  = LinearLayoutManager(applicationContext)

        recyclerView.layoutManager = layoutManager

        //adapter = exampleList?.let { ExampleAdapter(it) }
        //val pokemonIds = DataManager.buildPokemonIdsList()
        // Build sorted list of unique ids based on paldea ids
        val pokemonMap = DataManager.getPokemonMap()
//        var sortedData = pokemonData
//            .sortedWith<DataPokemon> (object : Comparator <DataPokemon> {
//                override fun compare (p0: DataPokemon, p1: DataPokemon) : Int {
//                    if (p0.ids.paldea.toInt() > p1.ids.paldea.toInt()) {
//                        return 1
//                    }
//                    return -1
//                }
//            })
        val pokemonIds = ArrayList<String>(pokemonMap.keys)
//        sortedData.forEach { data ->
//            pokemonIds.add(data.ids.unique)
//        }

        val uiItemId = if (gridEnabled) R.layout.list_grid_item else R.layout.list_item

        adapter = ListAdapter(this, pokemonIds, uiItemId)
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                recyclerView,
                object : RecyclerTouchListener.ClickListener {

                    // Open details activity on click
                    override fun onClick(view: View?, position: Int) {
                        val pokemonId = adapter?.getCurrentIds()?.get(position)
                        val pokemonData = pokemonId?.let { DataManager.findPokemonData(it) }
                        if (pokemonData != null) {
                            val intent = Intent(applicationContext, ActivityDetails::class.java)
                            intent.putExtra("PokemonId", pokemonId)

                            detailsActivityLauncher.launch(intent)
                        }
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

    /* access modifiers changed from: private */
    fun filterQuery(text: String?) {
        val pokemonIds = adapter?.getAllIds()

        val filteredIds = ArrayList<String>()
        if (pokemonIds != null) {
            for (id in pokemonIds) {
                val pokemonData = DataManager.findPokemonData(id)
                if (pokemonData == null)
                    continue

                val localizedNameFr = DataManager.getLocalizedPokemonName(this, id, "fr")
                val localizedNameEn = DataManager.getLocalizedPokemonName(this, id, "en")

                val found0 = id.lowercase(Locale.getDefault()).contains(text!!)
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