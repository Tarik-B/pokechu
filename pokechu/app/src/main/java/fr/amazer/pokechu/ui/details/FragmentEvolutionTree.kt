package fr.amazer.pokechu.ui.details

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.zoom.ZoomLayout
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.FragmentEvolutionTreeBinding
import fr.amazer.pokechu.managers.SettingType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.RecyclerViewTouchListener
import fr.amazer.pokechu.ui.details.evolution_tree.EvolutionNodeViewHolder
import fr.amazer.pokechu.ui.details.evolution_tree.EvolutionTreeAdapter
import fr.amazer.pokechu.ui.details.evolution_tree.EvolutionTreeEdgeDecoration
import fr.amazer.pokechu.viewmodel.ViewModelEvolutionData
import fr.amazer.pokechu.viewmodel.ViewModelEvolutions

private const val ARG_POKEMON_ID = "pokemonId"

class FragmentEvolutionTree : Fragment() {
    private var pokemonId: Int = 0

    private lateinit var binding: FragmentEvolutionTreeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AbstractGraphAdapter<EvolutionNodeViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEvolutionTreeBinding.inflate(layoutInflater)

        pokemonId = requireArguments().getInt(ARG_POKEMON_ID)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        val factory = ViewModelEvolutions.Factory(requireActivity().application, pokemonId)
        val viewModel: ViewModelEvolutions = ViewModelProvider(this, factory)[ViewModelEvolutions::class.java]

        viewModel.getEvolutionData().observe(viewLifecycleOwner) { evolutionData ->
            // Create graph
            val graph = createGraph(evolutionData)
            adapter.submitGraph(graph)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupUI() {
        // Setup graph view
        recyclerView = requireView().findViewById(R.id.recycler)
        setLayoutManager()
        setEdgeDecoration()
        setTouchListeners()

        val zoomLayout = requireView().findViewById(R.id.zoomLayout) as ZoomLayout
        zoomLayout.setMinZoom(0.8f)
        zoomLayout.setMaxZoom(10.0f)

        adapter = EvolutionTreeAdapter(pokemonId)
        recyclerView.adapter = adapter
    }

    private fun setLayoutManager() {
        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(100)
            .setSubtreeSeparation(100)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()
        recyclerView.layoutManager = context?.let { BuchheimWalkerLayoutManager(it, configuration) }
    }

    private fun setEdgeDecoration() {
        val edgeStyle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 5f
            color = Color.BLACK
//            style = Paint.Style.STROKE
//            strokeJoin = Paint.Join.ROUND
//            pathEffect = CornerPathEffect(10f)
            textSize = 50F
        }

        recyclerView.addItemDecoration(EvolutionTreeEdgeDecoration(edgeStyle))
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

                        val intent = Intent(context, ActivityDetails::class.java)
                        intent.putExtra("PokemonId", data.pokemonId)

                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                        requireActivity().startActivity(intent)
                        requireActivity().finish()
                    }

                    // Toggle discovered/captured status on long click
                    override fun onLongClick(view: View?, position: Int) {
                        val data = adapter.getNodeData(position) as ViewModelEvolutionData

                        SettingsManager.togglePokemonDiscovered(data.pokemonId)
                        adapter.notifyDataSetChanged()
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
            if (data.baseId != 0) {
                graph.addEdge(nodes[data.baseId]!!, nodes[data.pokemonId]!!)
            }
        }

        return graph
    }
}