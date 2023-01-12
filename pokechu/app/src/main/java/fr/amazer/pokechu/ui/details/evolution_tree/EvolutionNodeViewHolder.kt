package fr.amazer.pokechu.ui.details.evolution_tree

import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.databinding.EvolutionTreeNodeBinding
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.viewmodel.ViewModelEvolutionData

class EvolutionNodeViewHolder(
    private val binding: EvolutionTreeNodeBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: ViewModelEvolutionData) {
        val showUndiscoveredInfo = SettingsManager.getSetting<Boolean>(PreferenceType.SHOW_UNDISCOVERED_INFO)
        binding.isDiscovered = (data.isDiscovered || showUndiscoveredInfo)

        // Set pokemon name
        if ( data.isDiscovered || showUndiscoveredInfo )
            binding.name = data.localizedName
        else
            binding.name = "?"

        // Set thumbnail image
        binding.imagePath = data.thumbnailPath

        binding.executePendingBindings()
    }
}