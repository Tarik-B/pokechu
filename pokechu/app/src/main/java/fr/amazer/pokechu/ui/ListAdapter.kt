package fr.amazer.pokechu.ui

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.managers.PokemonManager
import fr.amazer.pokechu.R
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.ui.ListAdapter.ListViewHolder
import java.util.*


class ListAdapter internal constructor(
    private var context: Context?,
    private var pokemonIds: List<String>,
    private var uiItemId: Int
    ) :
    RecyclerView.Adapter<ListViewHolder>() {
    private val pokemonIdsFull: List<String>

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
//        var textViewId: TextView
        var textView1: TextView
        var textView2: TextView?

        init {
            imageView = itemView.findViewById<View>(R.id.image_app) as ImageView
//            textViewId = itemView.findViewById<View>(R.id.text_view_id) as TextView
            textView1 = itemView.findViewById<View>(R.id.text_view) as TextView
            textView2 = itemView.findViewById<View>(R.id.text_view2) as TextView?
        }
    }

    init {
        pokemonIdsFull = ArrayList<String>(pokemonIds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(uiItemId, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val currentId = pokemonIds[position]
        val currentData = PokemonManager.findPokemonData(currentId)
        if (currentData == null)
            return

        val isDiscovered = SettingsManager.isPokemonDiscovered(currentId)
        val assetManager: AssetManager? = context!!.assets

        if (isDiscovered == true) {
            var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, "images/" + currentData.images.thumbnail) }
            holder.imageView.setImageBitmap(bitmap)
            //holder.imageView.clearColorFilter()
        }
        else {
            val unknownImage = R.drawable.ic_question_mark
            holder.imageView.setImageResource(unknownImage)
            //holder.imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        }

        val localizedName = PokemonManager.getLocalizedPokemonName(context as Activity, currentId)

//        holder.textViewId.text = "#${currentData.ids.paldea}"
        holder.textView1.text = "#${currentData.ids.paldea} - ${localizedName}"
//        if (holder.textView2 != null)
//            holder.textView2!!.text = "English name: ${currentData.names.en}"
        holder.itemView.setOnClickListener { v ->
        }
    }

    override fun getItemCount(): Int {
        return pokemonIds.size
    }

    public fun getCurrentIds():List<String> {
        return pokemonIds
    }

    public fun getAllIds() : List<String> {
        return pokemonIdsFull
    }

    /* access modifiers changed from: 0000 */
    fun setFilter(filterdNames: List<String>) {
        pokemonIds = filterdNames
        notifyDataSetChanged()
    }
}