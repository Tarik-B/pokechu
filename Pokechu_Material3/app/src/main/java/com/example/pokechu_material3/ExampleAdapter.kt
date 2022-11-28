package com.example.pokechu_material3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.ExampleAdapter.ExampleViewHolder

class ExampleAdapter internal constructor(private var exampleList: List<ExampleItem?>) :
    RecyclerView.Adapter<ExampleViewHolder>() {
    private val exampleListFull: List<ExampleItem?>

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
        exampleListFull = ArrayList<ExampleItem?>(exampleList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        return ExampleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_my_apps, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val currentItem = exampleList[position]
        holder.imageView.setImageResource(currentItem!!.imageResource)
        holder.textView1.text = currentItem.text1
        holder.textView2.text = currentItem.text2
        holder.itemView.setOnClickListener { v ->
            Toast.makeText(
                v.context,
                "" + currentItem.text1,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return exampleList.size
    }

    /* access modifiers changed from: 0000 */
    fun setFilter(filterdNames: List<ExampleItem?>) {
        exampleList = filterdNames
        notifyDataSetChanged()
    }
}