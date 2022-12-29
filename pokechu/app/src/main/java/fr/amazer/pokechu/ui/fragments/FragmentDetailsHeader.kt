package fr.amazer.pokechu.ui.fragments

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fr.amazer.pokechu.enums.EntityPokemon
import fr.amazer.pokechu.enums.PokemonType
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
    private lateinit var pokemon: EntityPokemon
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

        suspend fun getPokemonById(): EntityPokemon? = withContext(Dispatchers.IO) {
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
        binding.height = pokemon.height.toString()
        binding.weight = pokemon.weight.toString()

        val assetManager: AssetManager? = context?.assets

        // Image
        val imgPath = AssetUtils.getPokemonThumbnailPath(pokemonId)
        val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
        binding.imageBitmap = bitmap

        // For black filter
        val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
        binding.isDiscovered = isDiscovered

        val typeBitmaps = mutableListOf<Bitmap>()
        types.forEach { type ->
            val imgPath = AssetUtils.getTypeThumbnailPath(PokemonType.values()[type])
            val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
            if (bitmap != null)
                typeBitmaps.add(bitmap)
        }
        binding.typeBitmaps = typeBitmaps
    }
}