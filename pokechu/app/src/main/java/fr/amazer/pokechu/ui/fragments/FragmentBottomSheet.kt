package fr.amazer.pokechu.ui.fragments

import android.content.res.AssetManager
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.enums.PokedexType
import fr.amazer.pokechu.enums.EntityRegion
import fr.amazer.pokechu.databinding.FragmentBottomSheetBinding
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class FragmentBottomSheet : Fragment() {
    private lateinit var binding: FragmentBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var regions = ArrayList<EntityRegion>()

    // Only used to trigger observers
    private var selectedRegion: PokedexType by Delegates.observable(PokedexType.NATIONAL) { _, _, newValue ->
        loadedObservers.forEach { it(newValue) }
    }
    private val loadedObservers = mutableListOf<(PokedexType) -> Unit>()
    fun addLoadedObserver(observer: (PokedexType) -> Unit) {
        loadedObservers.add(observer)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        suspend fun getRegions(): List<EntityRegion> = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findRegions()
        }
        lifecycleScope.launch { // coroutine on main
            // Get regions
            regions = getRegions() as ArrayList<EntityRegion> // coroutine on IO
            // back on main

            setupBottomSheet()
        }
    }

    private fun setupBottomSheet() {
        binding.regionGridContainer.removeAllViews()
        regions.forEach { region ->
            val inflater = LayoutInflater.from(context)
//            val itemBinding = BottomSheetRegionItemBinding.inflate(inflater)
            val regionItem =
                inflater.inflate(R.layout.bottom_sheet_region_item, null, false) as MaterialCardView
            val imageView = regionItem.findViewById(R.id.regionImageView) as ImageView

            val assetManager: AssetManager? = context?.assets
            val imgPath = AssetUtils.getRegionThumbnailPath(PokedexType.values()[region.id])
            val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
            imageView.setImageBitmap(bitmap)

            val textView = regionItem.findViewById(R.id.regionTextView) as TextView
            textView.text =
                context?.let { LocalizationManager.getRegionName(it, PokedexType.values()[region.id]) }
            val selectedRegion = SettingsManager.getSelectedRegion()
            regionItem.strokeWidth = if (region.id == selectedRegion) 10 else 0

            regionItem.setOnClickListener { _ ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                val regionId = PokedexType.values()[region.id]
                if (regionId.ordinal != SettingsManager.getSelectedRegion()) {
                    // ActivityMain has a listener on this
                    SettingsManager.setSelectedRegion(regionId)
                    updateSelectedRegionStroke()
                }
            }

            binding.regionGridContainer.addView(regionItem)
        }

        updateSelectedRegionStroke()
    }

    private fun updateSelectedRegionStroke() {
        binding.regionGridContainer.children.forEachIndexed { index, view ->
            val regionItem = view as MaterialCardView
            val selectedRegion = SettingsManager.getSelectedRegion()
            regionItem.strokeWidth = if (index == selectedRegion) 10 else 0
        }
    }

    fun getGlobalVisibleRect(outRect: Rect) {
        binding.bottomSheet.getGlobalVisibleRect(outRect)
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

    companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment FragmentBottomSheet.
     */
    @JvmStatic
    fun newInstance() =
        FragmentBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }
}