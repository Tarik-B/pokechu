package fr.amazer.pokechu.ui

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.core.text.color
import androidx.databinding.BindingAdapter
import fr.amazer.pokechu.R
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.utils.UIUtils

@BindingAdapter("visibleAnimated")
fun View.showHide(show: Boolean) {
    if (show) {
        visibility = View.VISIBLE
        alpha = 1.0f
//        UIUtils.animateView(this, View.VISIBLE, 1.0f, 100)
    }
    else {
        UIUtils.animateView(this, View.GONE, 0.0f, 100)
    }
}

@BindingAdapter("assetPath")
fun ImageView.setAssetPath(imgPath: String?) {
    if (imgPath != null) {
        val assetManager: AssetManager? = context?.assets
        val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }

        setImageBitmap(bitmap)
    }
}

@BindingAdapter("imageBitmap")
fun ImageView.setImageBitmap(bitmap: Bitmap?) {
    setImageBitmap(bitmap)
}

@BindingAdapter("tint")
fun ImageView.setImageTint(@ColorInt color: Int) {
    setColorFilter(color)
}

@BindingAdapter(value = ["filteredText", "filter", "filterColor"], requireAll = false)
fun TextView.highlightFilteredText(text: String, filter: String, @ColorInt color: Int) {

    val spannableString = SpannableStringBuilder()
    val regex = Regex(filter, option = RegexOption.IGNORE_CASE)
    val matches = regex.findAll(text)
    var lastEnd = 0
    matches.forEach { match ->
        val start = match.range.start
        val end = match.range.endInclusive
        if (start > lastEnd)
            spannableString.append(text.substring(lastEnd, start))
        spannableString.color(color) { append(match.value) }
        lastEnd = end + 1
    }
    if (lastEnd < text.length) {
        spannableString.append(text.substring(lastEnd, text.length))
    }

    setText(spannableString)
}

@BindingAdapter(value = ["typeBitmaps", "typeResIds", "typeItemResId"], requireAll = false)
fun ViewGroup.setTypeImages(typeBitmaps: List<Bitmap>?, typeResIds: List<Int>?, @LayoutRes resId: Int) {
    removeAllViews()
    fun createView(): ImageView {
        val inflater = LayoutInflater.from(context)
        val imageRoot = inflater.inflate(resId /*R.layout.main_type_item*/, null, false)
        val imageView = imageRoot.findViewById(R.id.imageType) as ImageView
        addView(imageRoot)

        return imageView
    }
    typeBitmaps?.forEach { typeBitmap ->
        val imageView = createView()
        imageView.setImageBitmap(typeBitmap)
    }
    typeResIds?.forEach { typeResId ->
        val imageView = createView()
        imageView.setImageResource(typeResId)
    }
}
