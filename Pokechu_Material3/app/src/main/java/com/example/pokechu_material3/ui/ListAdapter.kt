package com.example.pokechu_material3.ui

import android.content.Context
import android.content.res.AssetManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.managers.PokemonManager
import com.example.pokechu_material3.R
import com.example.pokechu_material3.managers.SettingsManager
import com.example.pokechu_material3.utils.AssetUtils
import com.example.pokechu_material3.ui.ListAdapter.ListViewHolder
import java.util.*


class ListAdapter internal constructor(private var context: Context?, private var pokemonIds: List<String>) :
    RecyclerView.Adapter<ListViewHolder>() {
    private val pokemonIdsFull: List<String>

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var textView1: TextView
        var textView2: TextView

        init {
            imageView = itemView.findViewById<View>(R.id.image_app) as ImageView
            textView1 = itemView.findViewById<View>(R.id.textview) as TextView
            textView2 = itemView.findViewById<View>(R.id.textview2) as TextView
        }
    }

    init {
        pokemonIdsFull = ArrayList<String>(pokemonIds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val currentId = pokemonIds[position]
        val currentData = PokemonManager.findPokemonData(currentId)
        if (currentData == null)
            return

        val isDiscovered = context?.let { SettingsManager.isPokemonDiscovered(it, currentId) }
        val assetManager: AssetManager? = context!!.assets

        if (isDiscovered == true) {
            var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, "images/" + currentData.images.thumbnail) }
            holder.imageView.setImageBitmap(bitmap)
            //holder.imageView.clearColorFilter()
        }
        else {
            val unknownImage = R.drawable.question_mark
            holder.imageView.setImageResource(unknownImage)
            //holder.imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        }

        holder.textView1.text = "#${currentData.ids.paldea} - ${currentData.names.fr}"
        holder.textView2.text = "English name: ${currentData.names.en}"
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