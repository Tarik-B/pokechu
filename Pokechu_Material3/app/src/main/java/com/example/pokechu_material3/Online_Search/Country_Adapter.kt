package com.example.pokechu_material3.Online_Search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.pokechu_material3.Online_Search.Country_Adapter.MyViewHolder
import com.example.pokechu_material3.R

class Country_Adapter(var conVideoArrayList: ArrayList<Country_Model>, context: AppCompatActivity) :
    RecyclerView.Adapter<MyViewHolder>() {
    var context: Context
    var str_userid: String? = null
    var str_name: String? = null
    var str_fname: String? = null
    var str_lname: String? = null

    interface OnItemClickListener {
        fun onItemClick(positon: Int, item: Country_Model?, view: View?)
    }

    /* access modifiers changed from: 0000 */
    fun setFilter(filterdNames: ArrayList<Country_Model>) {
        conVideoArrayList = filterdNames
        notifyDataSetChanged()
    }

    init {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country, parent, false) as View
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val classModel = conVideoArrayList[position]
        holder.name.text = classModel.name
        holder.ll_.setOnClickListener {
            Toast.makeText(
                context,
                "" + classModel.name,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return conVideoArrayList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var city: TextView? = null
        var mobile: TextView? = null
        var age: TextView? = null
        var blood_grp: TextView? = null
        var id: TextView? = null
        var image_app: ImageView
        var ll_: LinearLayout

        init {
            name = itemView.findViewById(R.id.name)
            image_app = itemView.findViewById(R.id.image)
            ll_ = itemView.findViewById(R.id.lyt_parent)
        }
    }
}