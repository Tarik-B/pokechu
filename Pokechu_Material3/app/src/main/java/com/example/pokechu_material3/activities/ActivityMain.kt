package com.example.pokechu_material3.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.R
import com.example.pokechu_material3.data.PokemonData
import com.example.pokechu_material3.databinding.ActivityMainBinding
import com.example.pokechu_material3.fragments.StartSearchDialogFragment
import com.example.pokechu_material3.managers.PokemonManager
import com.example.pokechu_material3.managers.SettingsManager
import com.example.pokechu_material3.ui.ListAdapter
import com.example.pokechu_material3.ui.RecyclerTouchListener
import com.example.pokechu_material3.utils.UIUtils
import java.util.*


class ActivityMain : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var menu: Menu

    private var adapter: ListAdapter? = null

    private val OPEN_DETAILS = 123
    private val OPEN_SETTINGS = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        PokemonManager.loadJsonData(applicationContext)
//        SettingsManager.with(applicationContext)

        // Initialize ui
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        getSupportActionBar()?.setTitle("Pokechu");

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
        val totalPokemonCount = PokemonManager.getPokemonMap().count()
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

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                Log.i(this::class.toString(), "onQueryTextChange = $newText")

                filterQuery(newText)

                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i(this::class.toString(), "onQueryTextSubmit = $query")
                return false
            }
        })

        menu.findItem(R.id.search).setOnActionExpandListener( object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    Log.i(this::class.toString(), "onMenuItemActionExpand")
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
                Log.i(this::class.toString(), "onMenuItemActionExpand")
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
                val intent = Intent(applicationContext, ActivitySettings::class.java)
                startActivityForResult(intent, OPEN_SETTINGS)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DETAILS) {
            adapter?.notifyDataSetChanged()
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
        //val pokemonIds = PokemonManager.buildPokemonIdsList()
        // Build sorted list of unique ids based on paldea ids
        val pokemonData = PokemonManager.getPokemonMap().values
        var sortedData = pokemonData
            .sortedWith<PokemonData> (object : Comparator <PokemonData> {
                override fun compare (p0: PokemonData, p1: PokemonData) : Int {
                    if (p0.ids.paldea.toInt() > p1.ids.paldea.toInt()) {
                        return 1
                    }
                    return -1
                }
            })
        val pokemonIds = ArrayList<String>()
        sortedData.forEach { data ->
            pokemonIds.add(data.ids.unique)
        }

        val uiItemId = if (gridEnabled) R.layout.list_grid_item else R.layout.list_item

        adapter = ListAdapter(applicationContext, pokemonIds, uiItemId)
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                recyclerView,
                object : RecyclerTouchListener.ClickListener {

                    // Open details activity on click
                    override fun onClick(view: View?, position: Int) {
                        val pokemonId = adapter?.getCurrentIds()?.get(position)
                        val pokemonData = pokemonId?.let { PokemonManager.findPokemonData(it) }
                        if (pokemonData != null) {
                            val intent = Intent(applicationContext, ActivityDetails::class.java)
                            intent.putExtra("PokemonId", pokemonData.ids.unique)
                            startActivityForResult(intent, OPEN_DETAILS)
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
                val pokemonData = PokemonManager.findPokemonData(id)
                if (pokemonData == null)
                    continue

                val found1 = pokemonData.ids.paldea.lowercase(Locale.getDefault()).contains(text!!)
                val found2 = pokemonData.ids.unique.lowercase(Locale.getDefault()).contains(text!!)
                val found3 = pokemonData.names.fr.lowercase(Locale.getDefault()).contains(text!!)
                val found4 = pokemonData.names.en.lowercase(Locale.getDefault()).contains(text!!)

                if ( found1 || found2 || found3 || found4 ) {
                    filteredIds.add(id)
                }
            }
        }
        adapter!!.setFilter(filteredIds)
    }
}