package fr.amazer.pokechu.fragments

import android.content.res.AssetManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.data.PokedexType
import fr.amazer.pokechu.data.Region
import fr.amazer.pokechu.databinding.FragmentBottomSheetBinding
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import kotlin.properties.Delegates

class FragmentBottomSheet : Fragment() {
    private lateinit var binding: FragmentBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var regions = ArrayList<Region>()

    // Only used to trigger observers
    private var selectedRegion: PokedexType by Delegates.observable(PokedexType.NATIONAL) { _, _, newValue ->
        loadedObservers.forEach { it(newValue) }
    }
    private val loadedObservers = mutableListOf<(PokedexType) -> Unit>()
    fun addLoadedObserver(observer: (PokedexType) -> Unit) {
        loadedObservers.add(observer)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
        binding = FragmentBottomSheetBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
//        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


        suspend fun getRegions(): List<Region> = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findRegions()
        }
        lifecycleScope.launch { // coroutine on main
            // Get regions
            regions = getRegions() as ArrayList<Region> // coroutine on IO
            // back on main

            setupBottomSheet()
        }
    }

    private fun setupBottomSheet() {
        binding.regionGridContainer.removeAllViews()
        regions.forEach { region ->
            val inflater = LayoutInflater.from(context)
            val regionItem =
                inflater.inflate(R.layout.bottom_sheet_region_item, null, false) as MaterialCardView
            val imageView = regionItem.findViewById(R.id.region_image_view) as ImageView

            val assetManager: AssetManager? = context?.assets
            val imgPath = AssetUtils.getRegionThuymbnailPat(PokedexType.values()[region.id])
            val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }
            imageView.setImageBitmap(bitmap)

            val textView = regionItem.findViewById(R.id.region_text_view) as TextView
            textView.text =
                context?.let { LocalizationManager.getLocalizedRegionName(it, PokedexType.values()[region.id]) }
            Log.i(this::class.simpleName, "textView.text = ${textView.text}")
            val selectedRegion = SettingsManager.getSelectedRegion()
            regionItem.strokeWidth = if (region.id == selectedRegion) 10 else 0

            regionItem.setOnClickListener { l ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                val regionId = PokedexType.values()[region.id]
                if (regionId.ordinal != SettingsManager.getSelectedRegion()) {
                    SettingsManager.setSelectedRegion(regionId)
                    updateSelectedRegionStroke()
//                    adapter?.notifyDataSetChanged()
//                    UIUtils.reloadActivity(this@ActivityMain, true)

                    // TODO
//                    showLoadingOverlay()
//                    fragmentList.notifySelectedRegionChanged()
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

    public fun getGlobalVisibleRect(outRect: Rect) {
        binding.bottomSheet.getGlobalVisibleRect(outRect)
    }

    public fun isExpanded(): Boolean {
        return bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
    }

    public fun toggleExpanded() {
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
    fun newInstance(param1: String) =
        FragmentBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }
}