package com.example.pokechu_material3.activities

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.R
import com.example.pokechu_material3.data.EvolutionTreeData
import com.example.pokechu_material3.databinding.ActivityDetailsBinding
import com.example.pokechu_material3.managers.PokemonManager
import com.example.pokechu_material3.managers.SettingsManager
import com.example.pokechu_material3.ui.EvolutionTreeEdgeDecoration
import com.example.pokechu_material3.utils.AssetUtils
import com.example.pokechu_material3.utils.UIUtils
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import java.util.*


class ActivityDetails : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>

    private lateinit var pokemonId: String
    private var evolutionTree: EvolutionTreeData? = null
    private var currentNode: Node? = null

    val OPEN_DETAILS = 123456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);

//        binding.toolbarLayout.title = title

        pokemonId = intent.getStringExtra("PokemonId").toString()
        evolutionTree = PokemonManager.findEvolutionTree(pokemonId)

        // Setup header
        val pokemonData = PokemonManager.findPokemonData(pokemonId)!!

        getSupportActionBar()?.setTitle("#${pokemonData.ids.paldea} - ${pokemonData.names.fr}");

        val textView = binding.textView
        val imageView = binding.imageHeader

        val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
        val assetManager: AssetManager? = applicationContext.assets

        var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, "images/" + pokemonData.images.thumbnail) }
        imageView.setImageBitmap(bitmap)

        textView.text = "English name: ${pokemonData.names.en}\n" +
                        "National ID: ${pokemonData.ids.unique}\n" +
                        "Paldea ID: ${pokemonData.ids.paldea}"


        if (isDiscovered == true) {
            imageView.clearColorFilter()
        }
        else {
            imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        }

        if ( evolutionTree != null ) {
            // Setup graph
            val graph = createGraph()
            recyclerView = findViewById(R.id.recycler)
            setLayoutManager()
            setEdgeDecoration()
            setupGraphView(graph)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_details, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.getItemId() === android.R.id.home) {
            finish() // close this activity and return to previous activity (if there is any)
        }
        return super.onOptionsItemSelected(item)
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == OPEN_DETAILS) {
//            adapter?.notifyDataSetChanged()
//        }
//    }

    private fun createGraph(): Graph {
        val graph = Graph()
        if (evolutionTree == null)
            return graph

        val root = Node(evolutionTree!!)
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
//        recyclerView.addItemDecoration(TreeEdgeDecoration())

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
                    .inflate(R.layout.tree_node, parent, false)
                return NodeViewHolder(view)
            }

            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
                // Set pokemon name
                val nodeData = Objects.requireNonNull(getNodeData(position)) as EvolutionTreeData
                val pokemonData = PokemonManager.findPokemonData(nodeData.id)
                if (pokemonData == null)
                    return

                val isDiscovered = SettingsManager.isPokemonDiscovered(nodeData.id)
                val assetManager: AssetManager? = applicationContext.assets

                // Setup texts
                if ( isDiscovered || pokemonId == nodeData.id )
                    holder.textViewName.text = pokemonData.names.fr
                else
                    holder.textViewName.text = ""

//                val evolutionTreeNode = PokemonManager.findEvolutionTreeNode(evolutionTree!!, nodeData.id)
//                if ( evolutionTreeNode != null && isDiscovered )
//                    holder.textViewSub.text = evolutionTreeNode.condition

                // Set thumbnail image
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
//        var textViewSub: TextView = itemView.findViewById(R.id.text_sub)
        var imageThumbnail: ImageView = itemView.findViewById(R.id.image_thumbnail)

        init {
            itemView.setOnClickListener {
                currentNode = adapter.getNode(bindingAdapterPosition)
                var currentNodeData = adapter.getNodeData(bindingAdapterPosition) as EvolutionTreeData

                val intent = Intent(applicationContext, ActivityDetails::class.java)
                intent.putExtra("PokemonId", currentNodeData.id)

//                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

//                startActivityForResult(intent, OPEN_DETAILS)
//                startActivity(intent)

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                startActivity(intent)
                finish()

                true
            }
            itemView.setOnLongClickListener{
                currentNode = adapter.getNode(bindingAdapterPosition)
//                Snackbar.make(itemView, "Clicked on " + adapter.getNodeData(bindingAdapterPosition)?.toString(),
//                    Snackbar.LENGTH_SHORT).show()

                var currentNodeData = adapter.getNodeData(bindingAdapterPosition) as EvolutionTreeData
                SettingsManager.togglePokemonDiscovered(currentNodeData.id)
                if ( currentNodeData.id == pokemonId) {
                    var host: Activity = itemView.getContext() as Activity
                    UIUtils.reloadActivity(host, true)
                }
                else {
                    adapter.notifyDataSetChanged()
                }

                true
            }
        }
    }
}