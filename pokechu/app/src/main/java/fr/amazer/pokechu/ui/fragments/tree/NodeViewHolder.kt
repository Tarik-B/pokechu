package fr.amazer.pokechu.ui.fragments.tree

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.databinding.EvolutionTreeNodeBinding
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils

class NodeViewHolder(
    private val binding: EvolutionTreeNodeBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(context: Context, data: EvolutionNodeData, currentId: Int) {
        val pokemonId = data.pokemonId

        val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
        val showUndiscoveredInfo = SettingsManager.getSetting<Boolean>(SettingType.SHOW_UNDISCOVERED_INFO)
        binding.isDiscovered = (isDiscovered || showUndiscoveredInfo)

        // Set pokemon name
        val text: String
        if ( pokemonId == currentId || isDiscovered || showUndiscoveredInfo )
            text = LocalizationManager.getPokemonName(context, pokemonId).toString()
        else
            text = "???"
        binding.text = text

        // Set thumbnail image
        val imgPath = AssetUtils.getPokemonThumbnailPath(pokemonId)
        binding.imagePath = imgPath
    }
}