package fr.amazer.pokechu.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import fr.amazer.pokechu.database.entities.EntityGame
import fr.amazer.pokechu.database.entities.EntityRegion
import fr.amazer.pokechu.databinding.BottomSheetRegionItemBinding
import fr.amazer.pokechu.databinding.FragmentBottomSheetPageBinding
import fr.amazer.pokechu.enums.Game
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.viewmodel.ViewModelRegions

enum class PageType {
    REGIONS,
    GAMES,
}

const val ARG_PAGE_TYPE = "object"

class FragmentBottomSheetPage : Fragment() {
    private lateinit var binding: FragmentBottomSheetPageBinding
    private lateinit var pageType: PageType
    private val viewModel: ViewModelRegions by activityViewModels()

    private val selectionObservers = mutableListOf<() -> Unit>()
    fun addSelectionObserver(observer: () -> Unit) {
        selectionObservers.add(observer)
    }
    private fun triggerSelectionObservers() {
        selectionObservers.forEach { it() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetPageBinding.inflate(layoutInflater)

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.takeIf { it.containsKey(ARG_PAGE_TYPE) }?.apply {
            val pageIndex = getInt(ARG_PAGE_TYPE)
            pageType = PageType.values()[pageIndex]
        }

        when(pageType) {
            PageType.REGIONS -> {
                // Update the region list when the data changes
                viewModel.getRegions().observe(viewLifecycleOwner) { regions ->
                    setData(regions)

                    // Update selected stroke when selected region changes
                    viewModel.getSelectedRegion().observe(viewLifecycleOwner) { selectedRegion ->
                        updateSelectedRegionStroke()
                    }
                }
            }
            PageType.GAMES -> {
                val viewModel: ViewModelRegions = ViewModelProvider(this)[ViewModelRegions::class.java]
                viewModel.getGames().observe(viewLifecycleOwner) { games ->
                    setData(games)
                    viewModel.getSelectedRegion().observe(viewLifecycleOwner) { selectedRegion ->
                        updateSelectedGameStroke(games)
                    }
                }
            }
        }
    }

    private fun <T> setData(dataList: List<T>) {
        binding.gridContainer.removeAllViews()
        dataList.forEach { data ->
            val itemBinding = BottomSheetRegionItemBinding.inflate(layoutInflater)
            val gridItem = itemBinding.rootCardView
            val imgPath = when(pageType) {
                PageType.REGIONS -> AssetUtils.getRegionThumbnailPath(Region.values()[(data as EntityRegion).id])
                PageType.GAMES -> AssetUtils.getGameThumbnailPath(Game.values()[(data as EntityGame).id])
            }
            itemBinding.imagePath = imgPath

            itemBinding.name = when(pageType) {
                PageType.REGIONS -> LocalizationManager.getRegionName(Region.values()[(data as EntityRegion).id])
                PageType.GAMES -> LocalizationManager.getGameName(Game.values()[(data as EntityGame).id])
            }

            gridItem.setOnClickListener { _ ->
                when(pageType) {
                    PageType.REGIONS -> {
                        val regionId = Region.values()[(data as EntityRegion).id]
                        val newSelectedRegion = regionId.ordinal

                        setSelectedRegion(newSelectedRegion)
                    }
                    PageType.GAMES -> {
                        val regionId = (data as EntityGame).region_id

                        setSelectedRegion(regionId)
                    }
                }

                triggerSelectionObservers()
            }

            binding.gridContainer.addView(itemBinding.root)
        }
    }

    private fun setSelectedRegion(newSelectedRegion: Int) {
        val selectedRegion = SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION)
        if (newSelectedRegion != selectedRegion) {
            // ActivityMain has a listener on this
            SettingsManager.setSetting<Int>(PreferenceType.SELECTED_REGION, newSelectedRegion)
        }
    }

    private fun updateSelectedRegionStroke() {
        val selectedRegion = SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION)
        binding.gridContainer.children.forEachIndexed { index, view ->
            val regionItem = view as MaterialCardView
            regionItem.strokeWidth = if (selectedRegion == index) 10 else 0
        }
    }

    private fun updateSelectedGameStroke(games: List<EntityGame>) {
        val selectedRegion = SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION)
        binding.gridContainer.children.forEachIndexed { index, view ->
            val gameItem = view as MaterialCardView
            gameItem.strokeWidth = if (selectedRegion == games[index].region_id) 10 else 0
        }
    }
}