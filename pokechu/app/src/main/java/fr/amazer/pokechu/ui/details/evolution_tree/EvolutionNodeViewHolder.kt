package fr.amazer.pokechu.ui.details.evolution_tree

import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.databinding.EvolutionTreeNodeBinding
import fr.amazer.pokechu.managers.SettingType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.viewmodel.ViewModelEvolutionData

class EvolutionNodeViewHolder(
    private val binding: EvolutionTreeNodeBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: ViewModelEvolutionData, rootId: Int) {
        val pokemonId = data.pokemonId

        val showUndiscoveredInfo = SettingsManager.getSetting<Boolean>(SettingType.SHOW_UNDISCOVERED_INFO)
        binding.isDiscovered = (data.isDiscovered || showUndiscoveredInfo)

        // Set pokemon name
        if ( pokemonId == rootId || data.isDiscovered || showUndiscoveredInfo )
            binding.text = data.localizedName
        else
            binding.text = "?"

        // Set thumbnail image
        binding.imagePath = data.thumbnailPath

        binding.executePendingBindings()
    }
}