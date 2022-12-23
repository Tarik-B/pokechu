package fr.amazer.pokechu.ui

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.data.PokemonType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.ListAdapter.ListViewHolder
import fr.amazer.pokechu.utils.AssetUtils


data class ListAdapterData(
//    val pokemonId: Int,
    val localId: Int,
    val names: MutableMap<String, String> = mutableMapOf<String, String>()
)

class ListAdapter internal constructor(
    private var context: Context?,
    private var pokemonsMap: Map<Int, ListAdapterData>,
    private var typesMap: Map<Int,List<PokemonType>>,
    private var uiItemId: Int
    ) : RecyclerView.Adapter<ListViewHolder>(), Filterable {
    private val pokemonIdsFull: List<Int>
    private var pokemonIdsFiltered: List<Int>
    private var currentFilter = ""

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardView: MaterialCardView
        var imageView: ImageView
        var textView1: TextView
        var capturedImageView: ImageView
        var typeImagesContainer: ViewGroup

        init {
            cardView = itemView.findViewById<View>(R.id.root_card_view) as MaterialCardView
            imageView = itemView.findViewById<View>(R.id.image_thumbnail) as ImageView
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

        pokemonIdsFiltered = sortedNationalIds
        pokemonIdsFull = ArrayList<Int>(pokemonIdsFiltered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(uiItemId, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentId = pokemonIdsFiltered[position]
        val currentData = pokemonsMap[currentId]
        val currentLocalId = pokemonsMap[currentId]!!.localId

        // Setup image
        val assetManager: AssetManager? = context!!.assets
        val imgPath = AssetUtils.getPokemonThumbnailPath(currentId)
        var bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it,imgPath ) }
        holder.imageView.setImageBitmap(bitmap)

        // Show image if discovered or "show undiscovered info" checked
        val isDiscovered = SettingsManager.isPokemonDiscovered(currentId)
        val showUndiscoveredInfo = SettingsManager.isShowUndiscoveredInfoEnabled()
        if (isDiscovered || showUndiscoveredInfo) {
            // Remove image filter
            holder.imageView.clearColorFilter()
        }
        else {
            // Add black filter to image
            holder.imageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        }

        // Setup text
        val localizedName = currentData?.names?.get(SettingsManager.getDataLanguage())
        // Show name if discovered or "show undiscovered info" checked OR seach query isnt empty
        if (!currentFilter.isEmpty()) {
            if (localizedName != null) {
                val color = ContextCompat.getColor(context!!, fr.amazer.pokechu.R.color.colorPrimary)
                val spannableString = SpannableStringBuilder().append("${currentLocalId} - ")
                val regex = Regex(currentFilter, option = RegexOption.IGNORE_CASE)
                val matches = regex.findAll(localizedName)
                if (matches != null) {
                    var lastEnd = 0
                    matches.forEach { match ->
                        val start = match.range.start
                        val end = match.range.endInclusive
                        if (start > lastEnd)
                            spannableString.append(localizedName.substring(lastEnd, start))
                        spannableString.color(color) { append(match.value) }
                        lastEnd = end + 1
                    }
                    if (lastEnd < localizedName.length) {
                        spannableString.append(localizedName.substring(lastEnd, localizedName.length))
                    }
                }
                else {
                    spannableString.append(localizedName)
                }
    //            val names = matches.map { it.groupValues[1] }.joinToString()
    //            println(names) // Alice, Bob, Eve

    //                .color(color) { append("${currentLocalId} - ") }
    //                .append("${localizedName}")

                holder.textView1.text = spannableString
            }
        }
        else if (isDiscovered || showUndiscoveredInfo) {
            var nameText = "#${currentLocalId} - ${localizedName}"
            holder.textView1.text = nameText
        }
        else {
            var nameText = "#${currentLocalId} - ???"
            holder.textView1.text = nameText
        }

        val isCaptured = SettingsManager.isPokemonCaptured(currentId)
        val pokeball = if (isCaptured) R.drawable.ic_pokeball else R.drawable.ic_pokeball_empty
        holder.capturedImageView.setImageResource(pokeball)

        // Add type  images
        holder.typeImagesContainer.removeAllViews()
        typesMap[currentId]?.forEach { type ->
            val inflater = LayoutInflater.from(context)
            val imageRoot = inflater.inflate(R.layout.main_type_item, null, false)
            val imageView = imageRoot.findViewById(R.id.image_type) as ImageView

            if (isDiscovered || showUndiscoveredInfo) {
                val assetManager: AssetManager? = context!!.assets
                val imgPath =
                    AssetUtils.getTypeThumbnailPathRound(PokemonType.values()[type.ordinal])
                val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
                imageView.setImageBitmap(bitmap)
            }
            else {
                val unknownImage = R.drawable.ic_question_mark
                imageView.setImageResource(unknownImage)
            }

            holder.typeImagesContainer.addView(imageRoot)
        }

        holder.itemView.setOnClickListener { v ->
        }

        // Needs to be a MaterialCardView
        holder.cardView.strokeWidth = if (isCaptured) 10 else 0
//        holder.itemView.setOnLongClickListener
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                currentFilter = constraint?.toString() ?: ""
                if (currentFilter.isEmpty()) {
                    pokemonIdsFiltered = pokemonIdsFull
                }
                else {
                    val filteredList = ArrayList<Int>()
                    val lang = SettingsManager.getDataLanguage()
                    pokemonIdsFull
                        .filter {
                            filterPokemon(it, currentFilter)
                        }
                        .forEach { filteredList.add(it) }
                    pokemonIdsFiltered = filteredList

                }
                return FilterResults().apply { values = pokemonIdsFiltered }
            }

            fun filterPokemon(id: Int, pattern: String): Boolean {
                var pokemonData = pokemonsMap[id]
                pokemonData!!.names.forEach{ (_, name) ->
                    if (name.contains(pattern, true))
                        return true
                }
                return false
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                pokemonIdsFiltered = if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<Int>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return pokemonIdsFiltered.size
    }

    public fun getCurrentIds():List<Int> {
        return pokemonIdsFiltered
    }
}