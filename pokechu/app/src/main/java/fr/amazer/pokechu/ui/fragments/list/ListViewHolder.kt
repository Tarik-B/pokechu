package fr.amazer.pokechu.ui.fragments.list

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import fr.amazer.pokechu.R
import fr.amazer.pokechu.enums.PokemonType
import fr.amazer.pokechu.databinding.ListGridItemBinding
import fr.amazer.pokechu.databinding.ListItemBinding
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.ListAdapterData
import fr.amazer.pokechu.utils.AssetUtils

class ListViewHolder(
    private val binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(context: Context, id: Int, localId: Int, data: ListAdapterData, types: List<PokemonType>, filter: String) {
        // Setup image
        val assetManager: AssetManager? = context.assets
        val imgPath = AssetUtils.getPokemonThumbnailPath(id)
        val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it,imgPath ) }
//            imageView.setImageBitmap(bitmap)
        if (bitmap != null) {
            setImage(bitmap)
        }

        // Show image if discovered or "show undiscovered info" checked
        val isDiscovered = SettingsManager.isPokemonDiscovered(id)
        val showUndiscoveredInfo = SettingsManager.isShowUndiscoveredInfoEnabled()
        setIsDiscovered(isDiscovered || showUndiscoveredInfo)

        val isCaptured = SettingsManager.isPokemonCaptured(id)
        setIsCaptured(isCaptured)

        // Setup text, show name if discovered or "show undiscovered info" checked OR seach query isnt empty
        var nameText: String
        if (!filter.isEmpty() || isDiscovered || showUndiscoveredInfo) {
            val localizedName = data.names.get(SettingsManager.getDataLanguage())
            nameText = "#${localId} - ${localizedName}"
        }
        else {
            nameText = "#${localId} - ???"
        }
        setText(nameText, filter)

        // Add type  images
        if (isDiscovered || showUndiscoveredInfo) {
            val typeBitmaps = mutableListOf<Bitmap>()
            types.forEach { type ->
                val typeImgPath = AssetUtils.getTypeThumbnailPathRound(PokemonType.values()[type.ordinal])
                val typeBitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, typeImgPath) }
                if (typeBitmap != null)
                    typeBitmaps.add(typeBitmap)
            }
            setTypeBitmaps(typeBitmaps)
            setTypeResIds(null)
        }
        else {
            val typeResIds = mutableListOf<Int>()
            types.forEach { type ->
                val unknownImage = R.drawable.ic_question_mark
                typeResIds.add(unknownImage)
            }
            setTypeBitmaps(null)
            setTypeResIds(typeResIds)
        }
    }

    // TODO find a way for ListItemBinding and ListGridItemBinding to have a common interface (other than ViewBinding)
    fun setImage(image: Bitmap) {
        if (binding is ListItemBinding)
            binding.imageBitmap = image
        else if (binding is ListGridItemBinding)
            binding.imageBitmap = image
    }
    fun setText(text: String, filter: String) {
        if (binding is ListItemBinding) {
            binding.text = text
            binding.filter = filter
        }
        else if (binding is ListGridItemBinding) {
            binding.text = text
            binding.filter = filter
        }
    }
    fun setIsDiscovered(discovered: Boolean) {
        if (binding is ListItemBinding)
            binding.isDiscovered = discovered
        else if (binding is ListGridItemBinding)
            binding.isDiscovered = discovered
    }
    fun setIsCaptured(captured: Boolean) {
        if (binding is ListItemBinding)
            binding.isCaptured = captured
        else if (binding is ListGridItemBinding)
            binding.isCaptured = captured
    }
    fun setTypeBitmaps(types: List<Bitmap>?) {
        if (binding is ListItemBinding)
            binding.typeBitmaps = types
        else if (binding is ListGridItemBinding)
            binding.typeBitmaps = types
    }
    fun setTypeResIds(types: List<Int>?) {
        if (binding is ListItemBinding)
            binding.typeResIds = types
        else if (binding is ListGridItemBinding)
            binding.typeResIds = types
    }
}