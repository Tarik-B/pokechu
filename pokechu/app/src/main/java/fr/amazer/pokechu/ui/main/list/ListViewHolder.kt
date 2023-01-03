package fr.amazer.pokechu.ui.main.list

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.enums.PokemonType
import fr.amazer.pokechu.databinding.ListGridItemBinding
import fr.amazer.pokechu.databinding.ListItemBinding
import fr.amazer.pokechu.managers.SettingType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.viewmodel.ViewModelPokemonData

class ListViewHolder(
    private val binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(context: Context, id: Int, localId: Int, data: ViewModelPokemonData, types: List<PokemonType>, filter: String) {
        // Setup image
        val imgPath = AssetUtils.getPokemonThumbnailPath(id)
        setImagePath(imgPath)

        // Show image if discovered or "show undiscovered info" checked
        val isDiscovered = SettingsManager.isPokemonDiscovered(id)
        val showUndiscoveredInfo = SettingsManager.getSetting<Boolean>(SettingType.SHOW_UNDISCOVERED_INFO)
        setIsDiscovered(isDiscovered || showUndiscoveredInfo)

        val isCaptured = SettingsManager.isPokemonCaptured(id)
        setIsCaptured(isCaptured)

        // Setup text, show name if discovered or "show undiscovered info" checked OR seach query isnt empty
        val nameText: String
        if (!filter.isEmpty() || isDiscovered || showUndiscoveredInfo) {
            val dataLanguage = SettingsManager.getSetting<String>(SettingType.DATA_LANGUAGE)
            val localizedName = data.names[dataLanguage]
            nameText = "#${localId} - ${localizedName}"
        }
        else {
            nameText = "#${localId} - ?"
        }
        setText(nameText, filter)

        // Add type images
        val assetManager: AssetManager? = context.assets
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
            types.forEach { _ ->
                val unknownImage = R.drawable.ic_question_mark
                typeResIds.add(unknownImage)
            }
            setTypeBitmaps(null)
            setTypeResIds(typeResIds)
        }

        binding.executePendingBindings()
    }

    // TODO find a way for ListItemBinding and ListGridItemBinding to have a common interface (other than ViewBinding)
    fun setImagePath(imgPath: String) {
        if (binding is ListItemBinding)
            binding.imagePath = imgPath
        else if (binding is ListGridItemBinding)
            binding.imagePath = imgPath
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