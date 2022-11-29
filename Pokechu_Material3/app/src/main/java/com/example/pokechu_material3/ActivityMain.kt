package com.example.pokechu_material3

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*
import android.view.*


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

            val searchView = (menu.findItem(R.id.search).actionView as SearchView)
            searchView.requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
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
        val pokemons = PokemonManager.getPokemons()
        adapter = pokemons?.let { ListAdapter(applicationContext, it) }
        recyclerView.adapter = adapter

        // Move to Adapter's onBindViewHolder()
        recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                recyclerView,
                object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        //val pokemons = (activity as MainActivity)?.getPokemons()
                        //val pokemonData = pokemons?.get(position)
                        val pokemonData = adapter?.getExampleList()?.get(position)
                        //val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(
                        //    pokemonData?.names?.fr
                        //)
                        //findNavController().navigate(action)

                        if (pokemonData != null) {
                            val intent = Intent(applicationContext, ActivityDetails::class.java)
                            intent.putExtra("PokemonId", pokemonData.ids.unique)
                            startActivity(intent)
                        }
                    }

                    override fun onLongClick(view: View?, position: Int) {

                    }
                })
        )
    }

    /* access modifiers changed from: private */
    fun filterQuery(text: String?) {
        val pokemons = PokemonManager.getPokemons()

        val filterdNames = ArrayList<PokemonData>()
        if (pokemons != null) {
            for (s in pokemons) {
                val found1 = s!!.ids.paldea.lowercase(Locale.getDefault()).contains(text!!)
                val found2 = s!!.ids.unique.lowercase(Locale.getDefault()).contains(text!!)
                val found3 = s!!.names.fr.lowercase(Locale.getDefault()).contains(text!!)
                val found4 = s!!.names.en.lowercase(Locale.getDefault()).contains(text!!)

                if ( found1 || found2 || found3 || found4 ) {
                    filterdNames.add(s)
                }
            }
        }
        adapter!!.setFilter(filterdNames)
    }
}