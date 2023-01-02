package fr.amazer.pokechu.ui.fragments.tree

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
import fr.amazer.pokechu.databinding.EvolutionTreeNodeBinding
import fr.amazer.pokechu.databinding.FragmentEvolutionTreeBinding
import fr.amazer.pokechu.enums.BaseIdEvolvedIdCondition
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.activities.ActivityDetails
import fr.amazer.pokechu.ui.RecyclerViewTouchListener
import fr.amazer.pokechu.utils.ConditionUtils
import fr.amazer.pokechu.utils.UIUtils
import fr.amazer.pokechu.viewmodel.ViewModelEvolutions
import java.util.*

private const val ARG_POKEMON_ID = "pokemonId"

class FragmentEvolutionTree : Fragment() {
    private var pokemonId: Int = 0

    private lateinit var binding: FragmentEvolutionTreeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>

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

        // Request evolution root then evolution chain from root
        val viewModel: ViewModelEvolutions = ViewModelProvider(this)[ViewModelEvolutions::class.java]
        viewModel.getEvolutionRoot(pokemonId).observe(viewLifecycleOwner) { rootId ->
            val id = if (rootId != 0) rootId else pokemonId
            viewModel.getEvolutions(id).observe(viewLifecycleOwner) { evolutions ->
                // Create graph
                val graph = createGraph(evolutions)
                setupGraphView(graph)
            }
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
                        val data = adapter.getNodeData(position) as EvolutionNodeData

                        val intent = Intent(context, ActivityDetails::class.java)
                        intent.putExtra("PokemonId", data.pokemonId)

                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                        requireActivity().startActivity(intent)
                        requireActivity().finish()
                    }

                    // Toggle discovered/captured status on long click
                    override fun onLongClick(view: View?, position: Int) {
                        val data = adapter.getNodeData(position) as EvolutionNodeData

                        SettingsManager.togglePokemonDiscovered(data.pokemonId)
                        if ( data.pokemonId == pokemonId) {
                            UIUtils.reloadActivity(requireActivity(), true)
                        }
                        else {
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            )
        )
    }

    private fun createGraph(evolutions: List<BaseIdEvolvedIdCondition>): Graph {
        val graph = Graph()

        if (!evolutions.isEmpty()){

            // Iterate on evolution links but create each node only once
            val nodes = HashMap<Int, Node>()
            nodes[pokemonId] = Node(EvolutionNodeData(pokemonId, null))
            evolutions.forEach { evolution ->

                // Find or create both nodes
                if (evolution.base_id !in nodes)
                    nodes[evolution.base_id] = Node(EvolutionNodeData(evolution.base_id, null ) )

                if (evolution.evolved_id !in nodes)
                    nodes[evolution.evolved_id] = Node(EvolutionNodeData(evolution.evolved_id, null ) )

                // Add decoded conditions to data
                (nodes[evolution.evolved_id]?.data as EvolutionNodeData).conditions = ConditionUtils.parseEncodedCondition(evolution.condition_encoded)

                // Create edge
                graph.addEdge(nodes[evolution.base_id]!!, nodes[evolution.evolved_id]!!)
            }

            // Add root
            graph.addNode(nodes[pokemonId]!!)
        }
        else {
            graph.addNode(Node(EvolutionNodeData(pokemonId, null)))
        }

        return graph
    }

    private fun setupGraphView(graph: Graph) {

        adapter = object : AbstractGraphAdapter<NodeViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
                val binding = EvolutionTreeNodeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )


                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.evolution_tree_node, parent, false)
                return NodeViewHolder(binding)
            }

            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
                val nodeData = getNodeData(position) as EvolutionNodeData
                holder.bind(context!!, nodeData, pokemonId)
            }
        }.apply {
            this.submitGraph(graph)
            recyclerView.adapter = this
        }

    }
}