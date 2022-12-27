package fr.amazer.pokechu.fragments

import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fr.amazer.pokechu.R
import fr.amazer.pokechu.data.Pokemon
import fr.amazer.pokechu.data.PokemonType
import fr.amazer.pokechu.databinding.FragmentDetailsHeaderBinding
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "pokemonId"

class FragmentDetailsHeader : Fragment() {
    private var pokemonId: Int = 0

    private lateinit var binding: FragmentDetailsHeaderBinding
    private lateinit var pokemon: Pokemon
    private lateinit var types: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailsHeaderBinding.inflate(layoutInflater)

        arguments?.let {
            pokemonId = it.getInt(ARG_PARAM1)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suspend fun getPokemonById(): Pokemon? = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findPokemonById(pokemonId)
        }
        suspend fun getPokemonTypes(): List<Int> = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findPokemonTypes(pokemonId)
        }
        lifecycleScope.launch { // coroutine on main
            var pokemonData = getPokemonById() // coroutine on IO
            types = getPokemonTypes() // coroutine on IO
            // back on main

            if (pokemonData != null) {
//                finish()
                pokemon = pokemonData

                // Header
                setupHeader()
            }
        }
    }

    private fun setupHeader() {
        // Header text
        binding.textHeightValue.text = pokemon.height.toString()
        binding.textWeightValue.text = pokemon.weight.toString()

        types.forEach{ type ->
            val inflater = LayoutInflater.from(context)
            val imageRoot = inflater.inflate(R.layout.details_type_item, null, false)
            val imageView = imageRoot.findViewById(R.id.imageType) as ImageView

            val assetManager: AssetManager? = context?.assets
            val imgPath = AssetUtils.getTypeThumbnailPath(PokemonType.values()[type])
            val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
            imageView.setImageBitmap(bitmap)

            binding.typesImageContainer.addView(imageRoot)
        }

        // Image
        val imageView = binding.imageHeader
        val assetManager: AssetManager? = context?.assets
        val imgPath = AssetUtils.getPokemonThumbnailPath(pokemonId)
        val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
        imageView.setImageBitmap(bitmap)

        // Add black filter
        val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
        if (isDiscovered == true) {
            imageView.clearColorFilter()
        }
        else {
            imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        }
    }
}