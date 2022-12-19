package fr.amazer.pokechu.activities

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import fr.amazer.pokechu.R
import fr.amazer.pokechu.data.BaseIdEvolvedIdCondition
import fr.amazer.pokechu.data.PokedexType
import fr.amazer.pokechu.data.Pokemon
import fr.amazer.pokechu.data.PokemonType
import fr.amazer.pokechu.databinding.ActivityDetailsBinding
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.EvolutionTreeEdgeDecoration
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ActivityDetails : BaseActivity() {

    private lateinit var binding: ActivityDetailsBinding
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractGraphAdapter<NodeViewHolder>

    private var pokemonId: Int = 0
    private lateinit var pokemon: Pokemon
    private lateinit var evolutionChain: List<BaseIdEvolvedIdCondition>
    private lateinit var types: List<Int>
    private var currentNode: Node? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        pokemonId = intent.getIntExtra("PokemonId", 0)

//        binding.toolbarLayout.title = title

        suspend fun getPokemonById(): Pokemon? = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findPokemonById(pokemonId)
        }
        suspend fun getEvolutionChain(): List<BaseIdEvolvedIdCondition> = withContext(Dispatchers.IO) {
            var evolutionRoot: Int = DatabaseManager.findPokemonEvolutionRoot(pokemonId)
            if (evolutionRoot == 0)
                evolutionRoot = pokemonId
            return@withContext DatabaseManager.findPokemonEvolutions(evolutionRoot)
        }
        suspend fun getPokemonTypes(): List<Int> = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findPokemonTypes(pokemonId)
        }
        lifecycleScope.launch { // coroutine on main
            var pokemonData = getPokemonById() // coroutine on IO
            evolutionChain = getEvolutionChain() // coroutine on IO
            types = getPokemonTypes() // coroutine on IO
            // back on main

            if (pokemonData == null)
                finish()
            pokemon = pokemonData!!

            // Header
            setupHeader()

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

    private fun setupHeader() {
        val localizedName = LocalizationManager.getLocalizedPokemonName(this, pokemonId)

        // Action bar title
        supportActionBar?.setTitle("#${pokemonId} - ${localizedName}");

        // Header text
        binding.textHeightValue.text = pokemon.height.toString()
        binding.textWeightValue.text = pokemon.weight.toString()

        types.forEach{ type ->
            val inflater = LayoutInflater.from(applicationContext)
            val imageRoot = inflater.inflate(R.layout.details_type_item, null, false)
            val imageView = imageRoot.findViewById(R.id.image_type) as ImageView

            val assetManager: AssetManager? = applicationContext.assets
            val imgPath = AssetUtils.getTypeThuymbnailPath(PokemonType.values()[type])
            val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
            imageView.setImageBitmap(bitmap)

            binding.typesImageContainer.addView(imageRoot)
        }
//        val textView = binding.textView
//        textView.text = "" //"English name: ${pokemonData.names.en}\n" +
//                        "National ID: ${pokemonData.ids.unique}\n" +
//                        "Paldea ID: ${pokemonData.ids.paldea}"

        // Image
        val imageView = binding.imageHeader
        val assetManager: AssetManager? = applicationContext.assets
        val imgPath = AssetUtils.getPokemonThumbnailPath(pokemonId)
        val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
        imageView.setImageBitmap(bitmap)

        // Add black filter
        val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
        if (isDiscovered == true) {
            imageView.clearColorFilter()
        }
        else {
            imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        }
    }

    private fun createGraph(): Graph {
        val graph = Graph()

        if (!evolutionChain.isEmpty()){

            // Iterate on evolution links but create each node only once
            val nodes = HashMap<Int, Node>()
            nodes[pokemonId] = Node(pokemonId)
            evolutionChain.forEach { evolution ->
                var baseNode =
                    if (evolution.base_id in nodes) nodes[evolution.base_id] else Node(evolution.base_id)
                var evolvedNode =
                    if (evolution.evolved_id in nodes) nodes[evolution.evolved_id] else Node(
                        evolution.evolved_id
                    )

                if (baseNode != null && evolvedNode != null) {
                    graph.addEdge(baseNode, evolvedNode)
                }
            }

            // Add root
            graph.addNode(nodes[pokemonId]!!)
        }
        else {
            graph.addNode(Node(pokemonId))
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
        recyclerView.layoutManager = BuchheimWalkerLayoutManager(this, configuration)
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
                    .inflate(R.layout.tree_node, parent, false)
                return NodeViewHolder(view)
            }

            override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
                // Set pokemon name
                val currentId = Objects.requireNonNull(getNodeData(position)) as Int // as DataEvolutionTree

                val isDiscovered = SettingsManager.isPokemonDiscovered(currentId.toInt())
                val assetManager: AssetManager? = applicationContext.assets

                val localizedName = LocalizationManager.getLocalizedPokemonName(this@ActivityDetails, currentId.toInt())

                // Setup texts
                if ( isDiscovered || pokemonId == currentId )
                    holder.textViewName.text = localizedName
                else
                    holder.textViewName.text = ""

//                val evolutionTreeNode = DataManager.findEvolutionTreeNode(evolutionTree!!, nodeData.id)
//                if ( evolutionTreeNode != null && isDiscovered )
//                    holder.textViewSub.text = evolutionTreeNode.condition

                // Set thumbnail image
                val imgPath = AssetUtils.getPokemonThumbnailPath(currentId)
                var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
                holder.imageThumbnail.setImageBitmap(bitmap)
                if (isDiscovered == true) {
                    holder.imageThumbnail.clearColorFilter()
                }
                else {
                    val unknownImage = R.drawable.ic_question_mark
//                    holder.imageThumbnail.setImageResource(unknownImage)
                    holder.imageThumbnail.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
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
                // On click open new details activity that replaces the current one
                currentNode = adapter.getNode(bindingAdapterPosition)
                var currentId = adapter.getNodeData(bindingAdapterPosition) as Int//as DataEvolutionTree

                val intent = Intent(applicationContext, ActivityDetails::class.java)
                intent.putExtra("PokemonId", currentId)

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                startActivity(intent)
                finish()

                true
            }
            itemView.setOnLongClickListener{
                // On long click toggle discovered status
                currentNode = adapter.getNode(bindingAdapterPosition)

                var currentNodeData = adapter.getNodeData(bindingAdapterPosition) as Int //as DataEvolutionTree
                SettingsManager.togglePokemonDiscovered(currentNodeData.toInt())
                if ( currentNodeData == pokemonId) {
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