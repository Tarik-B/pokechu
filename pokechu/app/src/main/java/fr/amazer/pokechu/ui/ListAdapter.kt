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
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.ui.ListAdapter.ListViewHolder
import kotlin.collections.ArrayList


class ListAdapter internal constructor(
    private var context: Context?,
    private var pokemonsMap: Map<Int, ListAdapterData>,
    private var uiItemId: Int
    ) : RecyclerView.Adapter<ListViewHolder>() {
    private val pokemonIdsFull: List<Int>
    private var pokemonIds: List<Int>

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
        // Build sorted national ids by local ids
        val nationalIds = pokemonsMap.keys.toList()
        val localIds = ArrayList<Int>()
        nationalIds.forEach{ id -> pokemonsMap[id]?.let { localIds.add(it.localId) } }

        val sortedLocalIds = localIds.sorted()

        val sortedNationalIds = ArrayList<Int>()
        sortedLocalIds.forEach{ local_id -> sortedNationalIds.add(nationalIds[localIds.indexOf(local_id)]) }

        pokemonIds = sortedNationalIds
        pokemonIdsFull = ArrayList<Int>(pokemonIds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(uiItemId, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val currentId = pokemonIds[position]
        val currentLocalId = pokemonsMap[currentId]!!.localId

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

        holder.textView1.text = "#${currentLocalId} - ${localizedName}"
//        if (currentId == currentLocalId)
//            holder.textView1.text = "#${currentId} - ${localizedName}"
//        else
//            holder.textView1.text = "#${currentLocalId} (#${currentId}) - ${localizedName}"

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