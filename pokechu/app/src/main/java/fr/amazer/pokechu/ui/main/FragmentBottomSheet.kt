package fr.amazer.pokechu.ui.main

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import fr.amazer.pokechu.database.entities.EntityRegion
import fr.amazer.pokechu.databinding.BottomSheetRegionItemBinding
import fr.amazer.pokechu.databinding.FragmentBottomSheetBinding
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.viewmodel.ViewModelRegions


class FragmentBottomSheet : Fragment() {
    private lateinit var binding: FragmentBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetBinding.inflate(layoutInflater)

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        // Update the region list when the data changes
        val viewModel: ViewModelRegions = ViewModelProvider(this)[ViewModelRegions::class.java]
        viewModel.getRegions().observe(viewLifecycleOwner) { regions ->
            setRegions(regions)
        }

        view.setOnTouchListener { v, event ->
            if (isExpanded()) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val rect = Rect(0, 0, 0, 0)
                    binding.bottomSheet.getHitRect(rect)

                    val intersects: Boolean = rect.contains(event.x.toInt(), event.y.toInt())
                    if (!intersects) {
                        // notify that we consumed this event
                        toggleExpanded()
                        true
                    }
                }
            }
            false
        }
    }

    private fun setRegions(regions: List<EntityRegion>) {
        binding.regionGridContainer.removeAllViews()
        regions.forEach { region ->
            val itemBinding = BottomSheetRegionItemBinding.inflate(layoutInflater)
            val regionItem = itemBinding.rootCardView
            val imgPath = AssetUtils.getRegionThumbnailPath(Region.values()[region.id])
            itemBinding.regionImgPath = imgPath

            itemBinding.regionName = LocalizationManager.getRegionName(Region.values()[region.id])

            val selectedRegion = SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION)
            regionItem.strokeWidth = if (region.id == selectedRegion) 10 else 0

            regionItem.setOnClickListener { _ ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                val regionId = Region.values()[region.id]
                if (regionId.ordinal != SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION)) {
                    // ActivityMain has a listener on this
                    SettingsManager.setSetting<Int>(PreferenceType.SELECTED_REGION, regionId.ordinal)
                    updateSelectedRegionStroke()
                }
            }

            binding.regionGridContainer.addView(itemBinding.root)
        }

        updateSelectedRegionStroke()
    }

    private fun updateSelectedRegionStroke() {
        binding.regionGridContainer.children.forEachIndexed { index, view ->
            val regionItem = view as MaterialCardView
            val selectedRegion = SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION)
            regionItem.strokeWidth = if (index == selectedRegion) 10 else 0
        }
    }

    fun isExpanded(): Boolean {
        return bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
    }

    fun toggleExpanded() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}