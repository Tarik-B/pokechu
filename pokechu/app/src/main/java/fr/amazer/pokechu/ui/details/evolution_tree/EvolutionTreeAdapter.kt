package fr.amazer.pokechu.ui.details.evolution_tree

import android.view.LayoutInflater
import android.view.ViewGroup
import dev.bandb.graphview.AbstractGraphAdapter
import fr.amazer.pokechu.databinding.EvolutionTreeNodeBinding
import fr.amazer.pokechu.viewmodel.ViewModelEvolutionData

class EvolutionTreeAdapter (
    private val rootId: Int
) : AbstractGraphAdapter<EvolutionNodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvolutionNodeViewHolder {
        val binding = EvolutionTreeNodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return EvolutionNodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EvolutionNodeViewHolder, position: Int) {
        val nodeData = getNodeData(position) as ViewModelEvolutionData
        holder.bind(nodeData, rootId)
    }
}



