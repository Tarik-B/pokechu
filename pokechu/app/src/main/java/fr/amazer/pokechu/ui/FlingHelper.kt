package fr.amazer.pokechu.ui

import android.content.Context
import android.util.DisplayMetrics
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import fr.amazer.pokechu.R
import kotlin.math.roundToInt

class FlingHelper(
    private val context: Context,
    private val appBarLayout: AppBarLayout,
    private val toolbar: MaterialToolbar,
    private val imageHeader: ImageView,
    private val workaround: FrameLayout,
    private val translateDirectionX: Int,
) {

    private var cashCollapseState: Pair<Int, Int>? = null
    private var imageAnimateStartPointY: Float = 0F
    private var imageCollapseAnimationChangeWeight: Float = 0F
    private var isCalculated = false
    private var verticalToolbarImageMargin =0F

    init {
        setupAppBar()
    }

    private fun setupAppBar() {
        val EXPAND_IMAGE_SIZE = context.resources.getDimension(R.dimen.default_expanded_image_size)
        val COLLAPSE_IMAGE_SIZE = context.resources.getDimension(R.dimen.default_collapsed_image_size)
        val horizontalToolbarImageMargin = context.resources.getDimension(R.dimen.activity_margin)

        (toolbar.height - COLLAPSE_IMAGE_SIZE) * 2
        /**/
        appBarLayout.addOnOffsetChangedListener { appBarLayout, i ->
            if (isCalculated.not()) {
                imageAnimateStartPointY =
                    Math.abs((appBarLayout.height - (EXPAND_IMAGE_SIZE + horizontalToolbarImageMargin)) / appBarLayout.totalScrollRange)
                imageCollapseAnimationChangeWeight = 1 / (1 - imageAnimateStartPointY)
                verticalToolbarImageMargin = (toolbar.height - COLLAPSE_IMAGE_SIZE) * 2
                isCalculated = true
            }
            /**/
            updateViews(Math.abs(i / appBarLayout.totalScrollRange.toFloat()))
        }
    }

    private fun updateViews(offset: Float) {
        val EXPAND_IMAGE_SIZE = context.resources.getDimension(R.dimen.default_expanded_image_size)
        val COLLAPSE_IMAGE_SIZE = context.resources.getDimension(R.dimen.default_collapsed_image_size)
        val horizontalToolbarImageMargin = context.resources.getDimension(R.dimen.activity_margin)

        val SWITCH_BOUND = 0.8f
        val TO_EXPANDED = 0
        val TO_COLLAPSED = 1
        val WAIT_FOR_SWITCH = 0
        val SWITCHED = 1

        // collapse - expand switch
        when {
            offset < SWITCH_BOUND -> Pair(TO_EXPANDED, cashCollapseState?.second ?: WAIT_FOR_SWITCH)
            else -> Pair(TO_COLLAPSED, cashCollapseState?.second ?: WAIT_FOR_SWITCH)
        }.apply {
            when {
                cashCollapseState != null && cashCollapseState != this -> {
                    when (first) {
                        TO_EXPANDED -> {
                            if (translateDirectionX != 0) {
                                // set image on start position (center of parent frame layout)
                                imageHeader.translationX = 0F
                            }
                        }
                        TO_COLLAPSED -> {
                        }
                    }
                    cashCollapseState = Pair(first, SWITCHED)
                }
                else -> {
                    cashCollapseState = Pair(first, WAIT_FOR_SWITCH)
                }
            }

            // Collapse image
            imageHeader.apply {
                when {
                    offset > imageAnimateStartPointY -> {
                        val imageCollapseAnimateOffset = (offset - imageAnimateStartPointY) * imageCollapseAnimationChangeWeight
                        val imageSize = EXPAND_IMAGE_SIZE - (EXPAND_IMAGE_SIZE - COLLAPSE_IMAGE_SIZE) * imageCollapseAnimateOffset
                        this.layoutParams.also {
                            it.height = Math.round(imageSize)
                            it.width = Math.round(imageSize)
                        }
                        val offsetDip = offset / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
                        workaround.setLayoutHeight(offsetDip.roundToInt())

                        if (translateDirectionX != 0) {
                            this.translationX = (translateDirectionX*(appBarLayout.width - horizontalToolbarImageMargin - imageSize) / 2) * imageCollapseAnimateOffset
                        }
                        this.translationY = ((toolbar.height - verticalToolbarImageMargin - imageSize) / 2) * imageCollapseAnimateOffset
                    }
                    else -> this.layoutParams.also {
                        if (it.height != EXPAND_IMAGE_SIZE.toInt()) {
                            it.height = EXPAND_IMAGE_SIZE.toInt()
                            it.width = EXPAND_IMAGE_SIZE.toInt()
                            this.layoutParams = it
                        }
                        if (translateDirectionX != 0) {
                            translationX = 0f
                        }
                    }
                }
            }
        }
    }
}