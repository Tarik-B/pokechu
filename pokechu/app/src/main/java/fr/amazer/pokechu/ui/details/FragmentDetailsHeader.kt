package fr.amazer.pokechu.ui.details

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.amazer.pokechu.databinding.FragmentDetailsHeaderBinding
import fr.amazer.pokechu.database.entities.EntityPokemon
import fr.amazer.pokechu.enums.PokemonType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.viewmodel.ViewModelPokemon

private const val ARG_POKEMON_ID = "pokemonId"

class FragmentDetailsHeader : Fragment() {
    private var pokemonId: Int = 0

    private lateinit var binding: FragmentDetailsHeaderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailsHeaderBinding.inflate(layoutInflater)

        pokemonId = requireArguments().getInt(ARG_POKEMON_ID)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: ViewModelPokemon = ViewModelProvider(this)[ViewModelPokemon::class.java]

        // Request evolution root then evolution chain from root
        viewModel.getPokemon(pokemonId).observe(viewLifecycleOwner) { pokemon ->
            viewModel.getPokemonTypes(pokemonId).observe(viewLifecycleOwner) { types ->
                setHeaderData(pokemon, types)
            }
        }
    }

    private fun setHeaderData(pokemon: EntityPokemon, types: List<Int>) {
        // Header text
        binding.height = pokemon.height.toString()
        binding.weight = pokemon.weight.toString()

        // Image
        var thumbnailImgPath = AssetUtils.getPokemonThumbnailPath(pokemonId)
        binding.imagePath = thumbnailImgPath

        // For black filter
        val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
        binding.isDiscovered = isDiscovered

        val assetManager: AssetManager? = context?.assets

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