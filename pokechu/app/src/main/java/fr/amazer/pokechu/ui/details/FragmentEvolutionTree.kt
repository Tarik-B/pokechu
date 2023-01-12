package fr.amazer.pokechu.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.zoom.ZoomLayout
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.FragmentEvolutionTreeBinding
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.RecyclerViewTouchListener
import fr.amazer.pokechu.ui.details.evolution_tree.EvolutionNodeViewHolder
import fr.amazer.pokechu.ui.details.evolution_tree.EvolutionTreeAdapter
import fr.amazer.pokechu.ui.details.evolution_tree.EvolutionTreeEdgeDecoration
import fr.amazer.pokechu.viewmodel.ViewModelEvolutionData
import fr.amazer.pokechu.viewmodel.ViewModelEvolutions
import fr.amazer.pokechu.viewmodel.ViewModelPokemon
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.forEach
import kotlin.collections.set

class FragmentEvolutionTree : Fragment() {
    private lateinit var binding: FragmentEvolutionTreeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AbstractGraphAdapter<EvolutionNodeViewHolder>

    private val viewModelPokemon: ViewModelPokemon by activityViewModels()
    private val viewModelEvolutions: ViewModelEvolutions by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEvolutionTreeBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI(true)
        loadData()
    }

    private fun loadData() {
        viewModelEvolutions.getEvolutionData().removeObservers(viewLifecycleOwner)
        viewModelEvolutions.getEvolutionData().observe(viewLifecycleOwner) { evolutionData ->
            // Create graph
            val graph = createGraph(evolutionData)
            adapter.submitGraph(graph)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupUI(vertical: Boolean) {
        // Setup graph view
        recyclerView = binding.recycler

        setLayoutManager(vertical)
        recyclerView.addItemDecoration(EvolutionTreeEdgeDecoration())
        setTouchListeners()

        val zoomLayout = requireView().findViewById(R.id.zoomLayout) as ZoomLayout
        zoomLayout.setMinZoom(0.8f)
        zoomLayout.setMaxZoom(10.0f)

        adapter = EvolutionTreeAdapter()
        recyclerView.adapter = adapter
    }

    private fun setLayoutManager(vertical: Boolean) {
        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(250)
            .setLevelSeparation(250)
            .setSubtreeSeparation(250)
            .setOrientation( if(vertical) BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM else BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT)
            .build()
        recyclerView.layoutManager = BuchheimWalkerLayoutManager(requireContext(), configuration)
    }

    private fun setTouchListeners() {
        // Add click/long click listeners on items
        recyclerView.addOnItemTouchListener(
            RecyclerViewTouchListener(
                context,
                recyclerView,
                object : RecyclerViewTouchListener.ClickListener {

                    // Open details activity on click
                    override fun onClick(view: View?, position: Int) {
                        val data = adapter.getNodeData(position) as ViewModelEvolutionData
                            viewModelPokemon.setPokemonId(data.pokemonId)
                            viewModelEvolutions.setPokemonId(data.pokemonId)
                    }

                    // Toggle discovered/captured status on long click
                    override fun onLongClick(view: View?, position: Int) {
                        val data = adapter.getNodeData(position) as ViewModelEvolutionData
                        SettingsManager.togglePokemonDiscovered(data.pokemonId)
                    }
                }
            )
        )
    }

    private fun createGraph(evolutionData: List<ViewModelEvolutionData>): Graph {
        val graph = Graph()

        // Create nodes
        val nodes = HashMap<Int, Node>()
        evolutionData.forEach{ data ->
            val node = Node(data)
            nodes[data.pokemonId] = node
            graph.addNode(node)
        }

        // Create edges
        evolutionData.forEach{ data ->
            if (data.baseId != null) {
                graph.addEdge(nodes[data.baseId]!!, nodes[data.pokemonId]!!)
            }
        }

        return graph
    }
}