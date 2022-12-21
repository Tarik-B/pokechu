package fr.amazer.pokechu.activities

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import fr.amazer.pokechu.R
import fr.amazer.pokechu.data.BaseIdEvolvedIdCondition
import fr.amazer.pokechu.data.PokedexType
import fr.amazer.pokechu.data.Pokemon
import fr.amazer.pokechu.data.PokemonType
import fr.amazer.pokechu.databinding.ActivityDetailsBinding
import fr.amazer.pokechu.fragments.FragmentDetailsHeader
import fr.amazer.pokechu.fragments.FragmentEvolutionTree
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.EvolutionTreeEdgeDecoration
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ActivityDetails : BaseActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private var pokemonId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pokemonId = intent.getIntExtra("PokemonId", 0)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pass pokemon id to evolution tree fragment
        val args = Bundle()
        args.putInt("pokemonId", pokemonId)
        val fragmentEvolutionTree = binding.fragmentEvolutionTree.getFragment<FragmentEvolutionTree>()
        fragmentEvolutionTree.arguments = args

        // Pass pokemon id to header fragment
        val fragmentDetailsHeader = binding.fragmentHeader.getFragment<FragmentDetailsHeader>()
        fragmentDetailsHeader.arguments = args

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        val localizedName = LocalizationManager.getLocalizedPokemonName(this, pokemonId)

        // Action bar title
        supportActionBar?.setTitle("#${pokemonId} - ${localizedName}");
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_details, menu)

        return true
    }
}