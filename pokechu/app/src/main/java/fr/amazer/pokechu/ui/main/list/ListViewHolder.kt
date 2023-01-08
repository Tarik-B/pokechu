package fr.amazer.pokechu.ui.main.list

import android.content.Context
import android.graphics.Bitmap
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ListItemBinding
import fr.amazer.pokechu.databinding.ListItemGridBinding
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils

class ListViewHolder(
    private val binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(context: Context, holderData: ListViewHolderData) {

        val data = holderData.viewModelData

        // Setup image
        setImagePath(data.thumbnailPath)

        bindIsDiscovered(data.isDiscovered)
        bindIsCaptured(data.isCaptured)

        // Setup text, show name if discovered or "show undiscovered info" checked OR seach query isnt empty
        val showUndiscoveredInfo = SettingsManager.getSetting<Boolean>(PreferenceType.SHOW_UNDISCOVERED_INFO)

//        var localizedName: String
//        if (holderData.filter.isEmpty()) {
//            val dataLanguage = SettingsManager.getSetting<String>(PreferenceType.DATA_LANGUAGE)
//            localizedName = data.names[dataLanguage]?: ""
//        }
//        else {
//            localizedName = data.names.values.find { value -> value.contains(holderData.filter, true) }?: ""
//        }

        val nameText: String
        if (!holderData.filter.isEmpty() || data.isDiscovered || showUndiscoveredInfo) {
            val dataLanguage = SettingsManager.getSetting<String>(PreferenceType.DATA_LANGUAGE)
            nameText = data.names[dataLanguage]!!
//            nameText = "#${data.localId} - ${localizedName}"
        }
        else {
            nameText = "?"
        }
        setText(nameText, data.localId.toString(), holderData.filter)

        // Add type images
        if (data.isDiscovered || showUndiscoveredInfo) {
            val typeBitmaps = mutableListOf<Bitmap>()
            data.typeImagePaths?.forEach { typeImgPath ->
                val typeBitmap = AssetUtils.getBitmapFromAsset(context, typeImgPath)
                if (typeBitmap != null)
                    typeBitmaps.add(typeBitmap)
            }
            setTypeBitmaps(typeBitmaps)
            setTypeResIds(null)
        }
        else {
            val typeResIds = mutableListOf<Int>()
            data.typeImagePaths?.forEach { _ ->
                val unknownImage = R.drawable.ic_question_mark
                typeResIds.add(unknownImage)
            }
            setTypeBitmaps(null)
            setTypeResIds(typeResIds)
        }

        binding.executePendingBindings()
    }

    fun bindIsDiscovered(isDiscovered: Boolean) {
        // Show image if discovered or "show undiscovered info" checked
        val showUndiscoveredInfo = SettingsManager.getSetting<Boolean>(PreferenceType.SHOW_UNDISCOVERED_INFO)
        setIsDiscovered(isDiscovered || showUndiscoveredInfo)
    }

    fun bindIsCaptured(isCaptured: Boolean) {
        setIsCaptured(isCaptured)
    }

    // TODO find a way for ListItemBinding and ListItemGridBinding to have a common interface (other than ViewBinding)
    fun setImagePath(imgPath: String) {
        if (binding is ListItemBinding)
            binding.imagePath = imgPath
        else if (binding is ListItemGridBinding)
            binding.imagePath = imgPath
    }
    fun setText(name: String, id: String, filter: String) {
        if (binding is ListItemBinding) {
            binding.name = name
            binding.id = id
            binding.filter = filter
        }
        else if (binding is ListItemGridBinding) {
            binding.name = name
            binding.id = id
            binding.filter = filter
        }
    }
    private fun setIsDiscovered(discovered: Boolean) {
        if (binding is ListItemBinding)
            binding.isDiscovered = discovered
        else if (binding is ListItemGridBinding)
            binding.isDiscovered = discovered
    }
    private fun setIsCaptured(captured: Boolean) {
        if (binding is ListItemBinding)
            binding.isCaptured = captured
        else if (binding is ListItemGridBinding)
            binding.isCaptured = captured
    }
    private fun setTypeBitmaps(types: List<Bitmap>?) {
        if (binding is ListItemBinding)
            binding.typeBitmaps = types
        else if (binding is ListItemGridBinding)
            binding.typeBitmaps = types
    }
    private fun setTypeResIds(types: List<Int>?) {
        if (binding is ListItemBinding)
            binding.typeResIds = types
        else if (binding is ListItemGridBinding)
            binding.typeResIds = types
    }
}