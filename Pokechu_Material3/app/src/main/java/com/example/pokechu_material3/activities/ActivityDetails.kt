package com.example.pokechu_material3.activities

import android.content.res.AssetManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.R
import com.example.pokechu_material3.data.EvolutionTreeData
import com.example.pokechu_material3.databinding.ActivityDetailsBinding
import com.example.pokechu_material3.managers.PokemonManager
import com.example.pokechu_material3.managers.SettingsManager
import com.example.pokechu_material3.utils.AssetUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import dev.bandb.graphview.layouts.tree.TreeEdgeDecoration
import java.util.*


class ActivityDetails : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>

    private lateinit var pokemonId: String
    private lateinit var evolutionTree: EvolutionTreeData
    private var currentNode: Node? = null
    private var nodeCount = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title

        pokemonId = intent.getStringExtra("PokemonId").toString()
        evolutionTree = PokemonManager.findEvolutionTree(pokemonId)!!

//        binding.textView.text = tree_to_string(evolutionTree)

        val graph = createGraph()
        recyclerView = findViewById(R.id.recycler)
        setLayoutManager()
        setEdgeDecoration()
        setupGraphView(graph)
    }

    private fun createGraph(): Graph {
        val graph = Graph()
        if (evolutionTree == null)
            return graph

        val root = Node(evolutionTree)
        createNodeHierarchy(graph, root)

        return graph
    }

    private fun createNodeHierarchy(graph: Graph, node: Node) {
        val nodeData = node.data as EvolutionTreeData
        nodeData.evolutions.forEach{ childData ->
            val child = Node(childData)
            graph.addEdge(node, child)
            createNodeHierarchy(graph, child)
        }
    }

    private fun setLayoutManager() {
        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(100)
            .setSubtreeSeparation(100)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()
        recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, configuration)
    }

    private fun setEdgeDecoration() {
        recyclerView.addItemDecoration(TreeEdgeDecoration())
    }

    private fun setupGraphView(graph: Graph) {
        adapter = object : AbstractGraphAdapter<NodeViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tree_node, parent, false)
                return NodeViewHolder(view)
            }

            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
                // Set pokemon name
                val nodeData = Objects.requireNonNull(getNodeData(position)) as EvolutionTreeData
                val pokemonData = PokemonManager.findPokemonData(nodeData.id)
                if (pokemonData == null)
                    return

                // Set thumbnail
                val isDiscovered = SettingsManager.isPokemonDiscovered(applicationContext, nodeData.id)
                val assetManager: AssetManager? = applicationContext.assets

                if ( isDiscovered || pokemonId == nodeData.id )
                    holder.textViewName.text = pokemonData.names.fr
                else
                    holder.textViewName.text = ""

                if (isDiscovered == true) {

                    var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, "images/" + pokemonData.images.thumbnail) }
                    holder.imageThumbnail.setImageBitmap(bitmap)
                    //holder.imageView.clearColorFilter()
                }
                else {
                    val unknownImage = R.drawable.question_mark
                    holder.imageThumbnail.setImageResource(unknownImage)
                    //holder.imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
                }
            }
        }.apply {
            this.submitGraph(graph)
            recyclerView.adapter = this
        }
    }

    protected inner class NodeViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewName: TextView = itemView.findViewById(R.id.text_name)
        var imageThumbnail: ImageView = itemView.findViewById(R.id.image_thumbnail)

        init {
            itemView.setOnClickListener {
                currentNode = adapter.getNode(bindingAdapterPosition)
//                Snackbar.make(itemView, "Clicked on " + adapter.getNodeData(bindingAdapterPosition)?.toString(),
//                    Snackbar.LENGTH_SHORT).show()

                var currentNodeData = adapter.getNodeData(bindingAdapterPosition) as EvolutionTreeData
                SettingsManager.togglePokemonDiscovered(applicationContext, currentNodeData.id)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private val nodeText: String
        get() = "Node " + nodeCount++

    //    def print_tree(self, node, level=0):
    //
    //        how = ( " (" + node["condition"] + ")" ) if node["condition"] else ""
    //        print("    " * (level - 1) + "+---" * (level > 0) + node["id"] + how )
    //
    //        for child in node["evolutions"]:
    //            self.print_tree(child, level + 1)
    fun tree_to_string(tree: EvolutionTreeData, level: Int = 0): String {
        var text = String()
        if (level >0)
            text += "    ".repeat(level - 1)
        if (level > 0)
            text += "+---"
        text += tree.id + "\n"

        tree.evolutions.forEach { child ->
            text += tree_to_string(child, level + 1)
        }

        return text
    }
}