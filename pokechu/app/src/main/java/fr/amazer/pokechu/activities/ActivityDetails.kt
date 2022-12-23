package fr.amazer.pokechu.activities

import android.os.Bundle
import android.view.Menu
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ActivityDetailsBinding
import fr.amazer.pokechu.fragments.FragmentDetailsHeader
import fr.amazer.pokechu.fragments.FragmentEvolutionTree
import fr.amazer.pokechu.managers.LocalizationManager


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

        val localizedName = LocalizationManager.getPokemonName(this, pokemonId)

        // Action bar title
        supportActionBar?.setTitle("#${pokemonId} - ${localizedName}");
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_details, menu)

        return true
    }
}