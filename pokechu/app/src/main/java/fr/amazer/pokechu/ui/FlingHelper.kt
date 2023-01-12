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
        val EXPAND_AVATAR_SIZE = context.resources.getDimension(R.dimen.default_expanded_image_size)
        val COLLAPSE_IMAGE_SIZE = context.resources.getDimension(R.dimen.default_collapsed_image_size)
        val horizontalToolbarAvatarMargin = context.resources.getDimension(R.dimen.activity_margin)

        (toolbar.height - COLLAPSE_IMAGE_SIZE) * 2
        /**/
        appBarLayout.addOnOffsetChangedListener { appBarLayout, i ->
            if (isCalculated.not()) {
                imageAnimateStartPointY =
                    Math.abs((appBarLayout.height - (EXPAND_AVATAR_SIZE + horizontalToolbarAvatarMargin)) / appBarLayout.totalScrollRange)
                imageCollapseAnimationChangeWeight = 1 / (1 - imageAnimateStartPointY)
                verticalToolbarImageMargin = (toolbar.height - COLLAPSE_IMAGE_SIZE) * 2
                isCalculated = true
            }
            /**/
            updateViews(Math.abs(i / appBarLayout.totalScrollRange.toFloat()))
        }
    }

    private fun updateViews(offset: Float) {
        val EXPAND_AVATAR_SIZE = context.resources.getDimension(R.dimen.default_expanded_image_size)
        val COLLAPSE_IMAGE_SIZE = context.resources.getDimension(R.dimen.default_collapsed_image_size)
        val horizontalToolbarAvatarMargin = context.resources.getDimension(R.dimen.activity_margin)

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
                            // set avatar on start position (center of parent frame layout)
                            imageHeader.translationX = 0F
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

            // Collapse avatar img
            imageHeader.apply {
                when {
                    offset > imageAnimateStartPointY -> {
                        val avatarCollapseAnimateOffset = (offset - imageAnimateStartPointY) * imageCollapseAnimationChangeWeight
                        val avatarSize = EXPAND_AVATAR_SIZE - (EXPAND_AVATAR_SIZE - COLLAPSE_IMAGE_SIZE) * avatarCollapseAnimateOffset
                        this.layoutParams.also {
                            it.height = Math.round(avatarSize)
                            it.width = Math.round(avatarSize)
                        }
                        val offsetDip = offset / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
                        workaround.setLayoutHeight(offsetDip.roundToInt())

                        this.translationX = ((appBarLayout.width - horizontalToolbarAvatarMargin - avatarSize) / 2) * avatarCollapseAnimateOffset
                        this.translationY = ((toolbar.height  - verticalToolbarImageMargin - avatarSize ) / 2) * avatarCollapseAnimateOffset
                    }
                    else -> this.layoutParams.also {
                        if (it.height != EXPAND_AVATAR_SIZE.toInt()) {
                            it.height = EXPAND_AVATAR_SIZE.toInt()
                            it.width = EXPAND_AVATAR_SIZE.toInt()
                            this.layoutParams = it
                        }
                        translationX = 0f
                    }
                }
            }
        }
    }
}