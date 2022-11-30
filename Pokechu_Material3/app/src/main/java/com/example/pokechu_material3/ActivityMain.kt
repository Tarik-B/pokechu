package com.example.pokechu_material3

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*


class ActivityMain : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var menu: Menu

    private var adapter: ListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        PokemonManager.loadJsonData(applicationContext)

        // Initialize ui
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        //val navController = findNavController(R.id.nav_host_fragment_content_main)
        //appBarConfiguration = AppBarConfiguration(navController.graph)
        //setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show()

            val searchMenuItem = menu.findItem(R.id.search)
            searchMenuItem.expandActionView()
            menu.performIdentifierAction(R.id.search, 0)
            val searchView = (menu.findItem(R.id.search).actionView as SearchView)
            //searchView.requestFocus()
            //searchView.isFocusable = true
            searchView.requestFocus()

            //searchView.focusSearch(View.FOCUS_RIGHT)
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
        }

        setUpRecyclerView()

        /*view.findViewById<View>(R.id.filter).setOnClickListener {
            val intent = Intent(context, Country_A::class.java)
            startActivity(intent)
        }
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
                Log.i("TAG", "onQueryTextChange = $newText")

                filterQuery(newText)

                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i("TAG", "onQueryTextSubmit = $query")
                return false
            }
        })

        // Required to make the searchview manually focusable
        searchView.isIconifiedByDefault = false

        val prefs = applicationContext.getSharedPreferences("Settings", MODE_PRIVATE)
        val searchAllFields = prefs.getBoolean("search_all_fields", true)

        val searchAllMenuItem = menu.findItem(R.id.search_all_fields)
        searchAllMenuItem.isChecked = searchAllFields

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            R.id.search_all_fields -> {
                item.isChecked = !item.isChecked
                val prefs = applicationContext.getSharedPreferences("Settings", MODE_PRIVATE)
                prefs.edit().putBoolean("search_all_fields", item.isChecked).apply()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }*/

    private fun setUpRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager

        //adapter = exampleList?.let { ExampleAdapter(it) }
        //val pokemonIds = PokemonManager.getPokemonIds()
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

        adapter = pokemonIds?.let { ListAdapter(applicationContext, pokemonIds) }
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                recyclerView,
                object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        val pokemonId = adapter?.getCurrentIds()?.get(position)
                        val pokemonData = pokemonId?.let { PokemonManager.findData(it) }
                        if (pokemonData != null) {
                            val intent = Intent(applicationContext, ActivityDetails::class.java)
                            intent.putExtra("PokemonId", pokemonData.ids.unique)
                            startActivity(intent)
                        }
                    }

                    override fun onLongClick(view: View?, position: Int) {
                        val pokemonId = adapter?.getCurrentIds()?.get(position)
                        if (pokemonId != null) {
                            val discovered = PokemonManager.isDiscovered(applicationContext, pokemonId)
                            PokemonManager.setIsDiscovered(applicationContext,pokemonId, !discovered )
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
                val pokemonData = PokemonManager.findData(id)
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