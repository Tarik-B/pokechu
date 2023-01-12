package fr.amazer.pokechu.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.FragmentListBinding
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.RecyclerViewTouchListener
import fr.amazer.pokechu.ui.details.ActivityDetails
import fr.amazer.pokechu.ui.main.list.ListGridAdapter
import fr.amazer.pokechu.utils.UIUtils
import fr.amazer.pokechu.viewmodel.ViewModelPokemons


class FragmentList : Fragment() {
    private lateinit var binding: FragmentListBinding

    private val viewModel: ViewModelPokemons by activityViewModels()

    private var adapter: ListGridAdapter? = null

    // Only used to trigger observers
    private val loadedObservers = mutableListOf<(Boolean) -> Unit>()

    private var regionChanged = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(layoutInflater)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getListViewEnabled().observe(viewLifecycleOwner) { enabled ->
            val gridEnabled = !enabled
            setGridEnabled(gridEnabled)
        }

        // When region is changed, store info for next data update
        viewModel.getSelectedRegion().observe(viewLifecycleOwner) {
            triggerLoadedObservers(false)
            regionChanged = true
        }

        // When show undiscovered info/data language settings is changed,
        // manually trigger refresh as no data is actually changed
        viewModel.getShowUndiscoveredInfo().observe(viewLifecycleOwner) {
            adapter?.notifyDataSetChanged()
        }
        viewModel.getDataLanguage().observe(viewLifecycleOwner) {
            adapter?.notifyDataSetChanged()
        }

        setupUI()

        loadData()


        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                binding.fabToTop.isVisible = recyclerView.canScrollVertically(-1)// || dy >= 0
            }
        })

        binding.fabToTop.setOnClickListener{ _ ->
            scrollToTop()
        }

//        binding.swipeRefresh.setOnRefreshListener {
//            binding.swipeRefresh.isRefreshing = false
//            triggerLoadedObservers(false)
//            loadData()
//        }
    }

    private fun loadData() {
        viewModel.getPokemonData().removeObservers(viewLifecycleOwner)
        viewModel.getPokemonData().observe(viewLifecycleOwner) { dataList ->

            adapter?.setDataList(dataList) {

                // On region change, scroll to top and disable recycler view anims temporarily
                if (regionChanged) {

                    scrollToTop()

                    val animation = AnimationUtils.loadLayoutAnimation(context, R.anim.list_layout_animation)
                    binding.recyclerView.layoutAnimation = animation;
//                        binding.recyclerView.getAdapter()?.notifyDataSetChanged();

                    binding.recyclerView.itemAnimator = null
                    binding.recyclerView.layoutAnimationListener =
                        object : Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {}
                            override fun onAnimationEnd(p0: Animation?) {
                                binding.recyclerView.itemAnimator = DefaultItemAnimator()
                            }

                            override fun onAnimationRepeat(p0: Animation?) {}
                        }

                    binding.recyclerView.scheduleLayoutAnimation();

                    regionChanged = false
                }

                triggerLoadedObservers(true)
            }
        }
    }

    private fun setupUI() {
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Setup recyclew view
        binding.recyclerView.setHasFixedSize(true)

        // Add click/long click listeners on items
        binding.recyclerView.addOnItemTouchListener(
            RecyclerViewTouchListener(
                context,
                binding.recyclerView,
                object : RecyclerViewTouchListener.ClickListener {

                    // Open details activity on click
                    override fun onClick(view: View?, position: Int) {
                        val pokemonId = adapter?.getCurrentData()?.get(position)?.viewModelData?.pokemonId
                        val intent = Intent(context, ActivityDetails::class.java)
                        intent.putExtra("PokemonId", pokemonId)

                        requireActivity().startActivity(intent)
                    }

                    // Toggle discovered/captured status on long click
                    override fun onLongClick(view: View?, position: Int) {
                        val pokemonId = adapter?.getCurrentData()?.get(position)?.viewModelData?.pokemonId
                        if (pokemonId != null) {

                            val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
                            val isCaptured = SettingsManager.isPokemonCaptured(pokemonId)
                            val showUndiscoveredInfo = SettingsManager.getSetting<Boolean>(PreferenceType.SHOW_UNDISCOVERED_INFO)

                            if (!showUndiscoveredInfo) {
                                if (!isDiscovered) {
                                    SettingsManager.togglePokemonDiscovered(pokemonId)
                                    if (view != null)
                                        UIUtils.createDefaultParticles(requireActivity(), view.findViewById(R.id.imageThumbnail))
                                }
                                else if (!isCaptured) {
                                    SettingsManager.togglePokemonCaptured(pokemonId)
                                    if (view != null)
                                        UIUtils.createDefaultParticles(requireActivity(), view.findViewById(R.id.imagePokeballCaptured))
                                }
                                else {
                                    SettingsManager.togglePokemonDiscovered(pokemonId)
                                    SettingsManager.togglePokemonCaptured(pokemonId)
                                }
                            }
                            else {
                                SettingsManager.togglePokemonCaptured(pokemonId)
                                if (SettingsManager.isPokemonCaptured(pokemonId)) {
                                    SettingsManager.setPokemonDiscovered(pokemonId, true)
                                    if (view != null)
                                        UIUtils.createDefaultParticles(requireActivity(), view.findViewById(R.id.imagePokeballCaptured))
                                }
                            }
                        }
                    }
                })
        )

        // Create adapter
        adapter = ListGridAdapter(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setGridEnabled(gridEnabled: Boolean) {
        val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager
        if (gridEnabled) {
            layoutManager.spanCount = 2
        } else {
            layoutManager.spanCount = 1
        }

        adapter?.setGridEnabled(gridEnabled)
    }

    fun addLoadedObserver(observer: (Boolean) -> Unit) {
        loadedObservers.add(observer)
    }
    private fun triggerLoadedObservers(loaded: Boolean) {
        loadedObservers.forEach { it(loaded) }
    }

    fun filterQuery(text: String?) {
        adapter?.filter?.filter(text) {
            if (text != null && text.isEmpty()) {
                binding.recyclerView.scrollToPosition(0)
            }
        }
    }

    private fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
        binding.fabToTop.isVisible = false
    }
}