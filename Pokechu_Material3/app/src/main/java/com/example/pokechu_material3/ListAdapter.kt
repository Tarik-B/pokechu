package com.example.pokechu_material3

import android.content.Context
import android.content.res.AssetManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.ListAdapter.ExampleViewHolder
import java.util.*


class ListAdapter internal constructor(private var context: Context?, private var exampleList: List<PokemonData>) :
    RecyclerView.Adapter<ExampleViewHolder>() {
    private val exampleListFull: List<PokemonData>

    inner class ExampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        exampleListFull = ArrayList<PokemonData>(exampleList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        return ExampleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_my_apps, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {

        val currentItem = exampleList[position]
        //holder.imageView.setImageResource(currentItem!!.imageResource)

        val assetManager: AssetManager? = context!!.assets
        val bitmap = assetManager?.let { Utils.getBitmapFromAsset(it, "images/" + currentItem.images.thumbnail) }

        holder.imageView.setImageBitmap(bitmap)
        holder.textView1.text = currentItem.ids.paldea
        holder.textView2.text = currentItem.names.fr
        holder.itemView.setOnClickListener { v ->
            Toast.makeText(
                v.context,
                "" + currentItem.names.fr,
                Toast.LENGTH_SHORT
            ).show()

            //val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(currentItem.names.fr)
            //findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return exampleList.size
    }

    public fun getExampleList():List<PokemonData> {
        return exampleList
    }

    /* access modifiers changed from: 0000 */
    fun setFilter(filterdNames: List<PokemonData>) {
        exampleList = filterdNames
        notifyDataSetChanged()
    }
}