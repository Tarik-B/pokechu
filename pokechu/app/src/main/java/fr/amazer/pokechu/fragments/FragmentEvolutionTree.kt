package fr.amazer.pokechu.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.zoom.ZoomLayout
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import fr.amazer.pokechu.R
import fr.amazer.pokechu.activities.ActivityDetails
import fr.amazer.pokechu.data.BaseIdEvolvedIdCondition
import fr.amazer.pokechu.databinding.FragmentEvolutionTreeBinding
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.EvolutionNodeData
import fr.amazer.pokechu.ui.EvolutionTreeEdgeDecoration
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.utils.ConditionUtils
import fr.amazer.pokechu.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val ARG_PARAM1 = "pokemonId"

class FragmentEvolutionTree : Fragment() {
    private var pokemonId: Int = 0

    private lateinit var binding: FragmentEvolutionTreeBinding
    private var evolutionChain: List<BaseIdEvolvedIdCondition> = mutableListOf()
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>
    private var currentNode: Node? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_evolution_tree, container, false)
        binding = FragmentEvolutionTreeBinding.inflate(layoutInflater)

        arguments?.let {
            pokemonId = it.getInt(ARG_PARAM1)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suspend fun getEvolutionChain(): List<BaseIdEvolvedIdCondition> = withContext(Dispatchers.IO) {
            var evolutionRoot: Int = DatabaseManager.findPokemonEvolutionRoot(pokemonId)
            if (evolutionRoot == 0)
                evolutionRoot = pokemonId
            return@withContext DatabaseManager.findPokemonEvolutions(evolutionRoot)
        }
        lifecycleScope.launch { // coroutine on main
            evolutionChain = getEvolutionChain() // coroutine on IO

            // Setup graph
            val graph = createGraph()
            recyclerView = view.findViewById(R.id.recycler)
            setLayoutManager()
            setEdgeDecoration()
            setupGraphView(graph)

            val zoomLayout = view.findViewById(R.id.zoom_layout) as ZoomLayout
//            zoomLayout.setMinZoom(0.8f)
//            zoomLayout.setMaxZoom(2.5f)
            zoomLayout.setMinZoom(0.8f)
            zoomLayout.setMaxZoom(10.0f)
        }
    }

    private fun createGraph(): Graph {
        val graph = Graph()

        if (!evolutionChain.isEmpty()){

            // Iterate on evolution links but create each node only once
            val nodes = HashMap<Int, Node>()
            nodes[pokemonId] = Node(EvolutionNodeData(pokemonId, null))
            evolutionChain.forEach { evolution ->

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

    private fun setupGraphView(graph: Graph) {

        adapter = object : AbstractGraphAdapter<NodeViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.evolution_tree_node, parent, false)
                return NodeViewHolder(view)
            }

            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
                val nodeData = Objects.requireNonNull(getNodeData(position)) as EvolutionNodeData
                holder.onBinded(nodeData.pokemonId)
            }
        }.apply {
            this.submitGraph(graph)
            recyclerView.adapter = this
        }
    }

    protected inner class NodeViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewName: TextView = itemView.findViewById(R.id.text_name)
        //        var textViewSub: TextView = itemView.findViewById(R.id.text_sub)
        var imageThumbnail: ImageView = itemView.findViewById(R.id.image_thumbnail)

        init {
            itemView.setOnClickListener {
                // On click open new details activity that replaces the current one
                currentNode = adapter.getNode(bindingAdapterPosition)
                var currentNodeData = /*adapter.getNodeData(bindingAdapterPosition)*/
                    currentNode?.data as EvolutionNodeData

                val intent = Intent(context, ActivityDetails::class.java)
                intent.putExtra("PokemonId", currentNodeData.pokemonId)

//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
//                startActivity(intent)
//                finish()

                true
            }
            itemView.setOnLongClickListener{
                // On long click toggle discovered status
                currentNode = adapter.getNode(bindingAdapterPosition)

                var currentNodeData = adapter.getNodeData(bindingAdapterPosition) as EvolutionNodeData
                SettingsManager.togglePokemonDiscovered(currentNodeData.pokemonId)
                if ( currentNodeData.pokemonId == pokemonId) {
                    var host: Activity = itemView.getContext() as Activity
                    UIUtils.reloadActivity(host, true)
                }
                else {
                    adapter.notifyDataSetChanged()
                }

                true
            }
        }

        fun onBinded(pokemonId: Int) {
            // Set pokemon name
            val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
            val assetManager: AssetManager? = context?.assets

            val localizedName =
                context?.let { LocalizationManager.getPokemonName(it, pokemonId) }

            // Setup texts
            if ( isDiscovered || pokemonId == pokemonId )
                textViewName.text = localizedName
            else
                textViewName.text = ""

//                val evolutionTreeNode = DataManager.findEvolutionTreeNode(evolutionTree!!, nodeData.id)
//                if ( evolutionTreeNode != null && isDiscovered )
//                    holder.textViewSub.text = evolutionTreeNode.condition

            // Set thumbnail image
            val imgPath = AssetUtils.getPokemonThumbnailPath(pokemonId)
            var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
            imageThumbnail.setImageBitmap(bitmap)
            if (isDiscovered == true) {
                imageThumbnail.clearColorFilter()
            }
            else {
                val unknownImage = R.drawable.ic_question_mark
//                    holder.imageThumbnail.setImageResource(unknownImage)
                imageThumbnail.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment FragmentEvolutionTree.
         */
        @JvmStatic
        fun newInstance(param1: Int) =
            FragmentEvolutionTree().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}