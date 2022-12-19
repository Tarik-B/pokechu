package fr.amazer.pokechu.ui

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.activities.ActivityDetails
import fr.amazer.pokechu.data.BaseIdEvolvedIdCondition
import fr.amazer.pokechu.data.PokedexType
import fr.amazer.pokechu.data.Pokemon
import fr.amazer.pokechu.data.PokemonType
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.ui.ListAdapter.ListViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList


class ListAdapter internal constructor(
    private var context: Context?,
    private var pokemonsMap: Map<Int, ListAdapterData>,
    private var typesMap: Map<Int,List<PokemonType>>,
    private var uiItemId: Int
    ) : RecyclerView.Adapter<ListViewHolder>() {
    private val pokemonIdsFull: List<Int>
    private var pokemonIds: List<Int>

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardView: MaterialCardView
        var imageView: ImageView
        var textView1: TextView
        var capturedImageView: ImageView
        var typeImagesContainer: ViewGroup

        init {
            cardView = itemView.findViewById<View>(R.id.root_card_view) as MaterialCardView
            imageView = itemView.findViewById<View>(R.id.image_app) as ImageView
            textView1 = itemView.findViewById<View>(R.id.text_view) as TextView
            capturedImageView = itemView.findViewById<View>(R.id.image_pokeball_captured) as ImageView
            typeImagesContainer = itemView.findViewById<View>(R.id.little_types_container) as ViewGroup
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
        val imgPath = AssetUtils.getPokemonThumbnailPath(currentId)
        var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it,imgPath ) }
        holder.imageView.setImageBitmap(bitmap)

        // Setup text
        var nameText: String

        val isDiscovered = SettingsManager.isPokemonDiscovered(currentId)
        if (isDiscovered) {
            // Remove image filter
            holder.imageView.clearColorFilter()

            val localizedName = LocalizationManager.getLocalizedPokemonName(context!!, currentId)
            nameText = "#${currentLocalId} - ${localizedName}"
        }
        else {
            // Add black filter to image
            holder.imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)

            nameText = "???"
        }

        holder.textView1.text = nameText

        val isCaptured = SettingsManager.isPokemonCaptured(currentId)
        val pokeball = if (isCaptured) R.drawable.ic_pokeball else R.drawable.ic_pokeball_empty
        holder.capturedImageView.setImageResource(pokeball)

        // Add type  images
        holder.typeImagesContainer.removeAllViews()
        typesMap[currentId]?.forEach { type ->
            val inflater = LayoutInflater.from(context)
            val imageRoot = inflater.inflate(R.layout.main_type_item, null, false)
            val imageView = imageRoot.findViewById(R.id.image_type) as ImageView

            if (isDiscovered) {
                val assetManager: AssetManager? = context!!.assets
                val imgPath =
                    AssetUtils.getTypeThuymbnailPathRound(PokemonType.values()[type.ordinal])
                val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
                imageView.setImageBitmap(bitmap)
            }
            else {
                val unknownImage = R.drawable.ic_question_mark
                imageView.setImageResource(unknownImage)
            }

            holder.typeImagesContainer.addView(imageRoot)
        }

//        if (currentId == currentLocalId)
//            holder.textView1.text = "#${currentId} - ${localizedName}"
//        else
//            holder.textView1.text = "#${currentLocalId} (#${currentId}) - ${localizedName}"

        holder.itemView.setOnClickListener { v ->
        }

        // Needs to be a MaterialCardView
        holder.cardView.strokeWidth = if (isCaptured) 10 else 0
//        holder.itemView.setOnLongClickListener

//        typesMas: Map<Int,List<PokedexType>>,
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