package fr.amazer.pokechu.ui

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.data.Pokemons
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.ui.ListAdapter.ListViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ListAdapter internal constructor(
    private var context: Context?,
    private var lifecycleScope: LifecycleCoroutineScope,
    private var pokemonIds: List<Int>,
    private var uiItemId: Int
    ) : RecyclerView.Adapter<ListViewHolder>() {
    private val pokemonIdsFull: List<Int>
    private lateinit var pokemonsMap: Map<Int,Pokemons>

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var textView1: TextView
        var textView2: TextView?

        init {
            imageView = itemView.findViewById<View>(R.id.image_app) as ImageView
            textView1 = itemView.findViewById<View>(R.id.text_view) as TextView
            textView2 = itemView.findViewById<View>(R.id.text_view2) as TextView?
        }
    }

    init {
        pokemonIdsFull = ArrayList<Int>(pokemonIds)

        suspend fun getPokemons(ids: List<Int>): List<Pokemons> = withContext(Dispatchers.IO) {
//            return@withContext DatabaseManager.getPokemonsByIds(ids.toIntArray()) // TOO MANY VARIABLES IN SQL
            return@withContext DatabaseManager.findPokemons()
        }
        lifecycleScope.launch { // coroutine on main
            val pokemons = getPokemons(pokemonIds) // coroutine on IO

            // back on main
            pokemonsMap = pokemonIds.zip(pokemons).toMap()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(uiItemId, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val currentId = pokemonIds[position]

        // Setup image
        val assetManager: AssetManager? = context!!.assets
        val imgPath = AssetUtils.getThumbnailPath(currentId)
        var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it,imgPath ) }
        holder.imageView.setImageBitmap(bitmap)

        // Add black filter
        val isDiscovered = SettingsManager.isPokemonDiscovered(currentId)
        if (isDiscovered == true) {
            holder.imageView.clearColorFilter()
        }
        else {
//            val unknownImage = R.drawable.ic_question_mark
//            holder.imageView.setImageResource(unknownImage)
            holder.imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        }

        // Setup text
        val localizedName = LocalizationManager.getLocalizedPokemonName(context!!, currentId)

//        holder.textViewId.text = "#${currentData.ids.paldea}"
        holder.textView1.text = "#${currentId} - ${localizedName}"
//        if (holder.textView2 != null)
//            holder.textView2!!.text = "English name: ${currentData.names.en}"
        holder.itemView.setOnClickListener { v ->
        }
    }

    override fun getItemCount(): Int {
        return pokemonIds.size
    }

    public fun getCurrentIds():List<Int> {
        return pokemonIds
    }

    public fun getAllIds() : List<Int> {
        return pokemonIdsFull
    }

    fun setFilter(filterdNames: List<Int>) {
        pokemonIds = filterdNames
        notifyDataSetChanged()
    }
}