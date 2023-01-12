package fr.amazer.pokechu.ui.details

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fr.amazer.pokechu.databinding.FragmentDetailsHeaderBinding
import fr.amazer.pokechu.database.entities.EntityPokemon
import fr.amazer.pokechu.enums.PokemonType
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.viewmodel.ViewModelPokemon

class FragmentDetailsHeader : Fragment() {
    private lateinit var binding: FragmentDetailsHeaderBinding

    private val viewModelPokemon: ViewModelPokemon by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailsHeaderBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request evolution root then evolution chain from root
        viewModelPokemon.getPokemon().observe(viewLifecycleOwner) { pokemon ->
            viewModelPokemon.getPokemonTypes().observe(viewLifecycleOwner) { types ->
                if ( pokemon != null && types != null)
                    setHeaderData(pokemon, types)
            }
        }
    }

    private fun setHeaderData(pokemon: EntityPokemon, types: List<Int>) {
        // Header text
        binding.height = pokemon.height.toString()
        binding.weight = pokemon.weight.toString()

        val typeBitmaps = mutableListOf<Bitmap>()
        types.forEach { type ->
            val imgPath = AssetUtils.getTypeThumbnailPath(PokemonType.values()[type])
            val bitmap = AssetUtils.getBitmapFromAsset(requireContext(), imgPath)
            if (bitmap != null)
                typeBitmaps.add(bitmap)
        }
        binding.typeBitmaps = typeBitmaps
    }
}