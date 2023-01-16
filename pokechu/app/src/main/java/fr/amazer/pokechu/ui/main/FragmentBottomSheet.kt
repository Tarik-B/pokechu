package fr.amazer.pokechu.ui.main

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.FragmentBottomSheetBinding

class BottomSheetCollectionAdapter(
    private val fragmentBottomSheet: FragmentBottomSheet
) : FragmentStateAdapter(fragmentBottomSheet) {
    override fun getItemCount(): Int = PageType.values().size

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = FragmentBottomSheetPage()
        fragment.addSelectionObserver { fragmentBottomSheet.onSelectionChanged() }

        fragment.arguments = Bundle().apply {
            putInt(ARG_PAGE_TYPE, position)
        }

        return fragment
    }
}

class FragmentBottomSheet : Fragment() {
    private lateinit var binding: FragmentBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    // When requested, this adapter returns a FragmentBottomSheetPage,
    // representing an object in the collection.
    private lateinit var bottomSheetCollectionAdapter: BottomSheetCollectionAdapter
    private lateinit var viewPager: ViewPager2

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

        bottomSheetCollectionAdapter = BottomSheetCollectionAdapter(this)
        viewPager = binding.pager
        viewPager.adapter = bottomSheetCollectionAdapter

        // Set tab layout titles
        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val titles = resources.getStringArray(R.array.bottom_sheet_page_titles)
            tab.text = titles[position]
        }.attach()

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

    fun isExpanded(): Boolean {
        return bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
    }

    fun toggleExpanded() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            collapse()
        }
        else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun collapse() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun onSelectionChanged() {
        collapse()
    }
}