package fr.amazer.pokechu.ui.fragments.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.FragmentListBinding
import fr.amazer.pokechu.managers.SettingType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.ListAdapter
import fr.amazer.pokechu.ui.RecyclerViewTouchListener
import fr.amazer.pokechu.ui.activities.ActivityDetails
import fr.amazer.pokechu.utils.UIUtils
import fr.amazer.pokechu.viewmodel.ViewModelPokemonData
import fr.amazer.pokechu.viewmodel.ViewModelPokemons

class FragmentList : Fragment() {
    private lateinit var binding: FragmentListBinding
    private var adapter: ListAdapter? = null

    private lateinit var detailsActivityLauncher: ActivityResultLauncher<Intent>

    // Only used to trigger observers
    private val loadedObservers = mutableListOf<(Boolean) -> Unit>()

    private val viewModel: ViewModelPokemons by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        viewModel.getPokemonFilters().observe(viewLifecycleOwner) {
            triggerLoadedObservers(false)
        }

        loadData()

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            triggerLoadedObservers(false)
            loadData()
        }

        // Notify data changed when coming back from details activity
        // as discovered status might have changed
        detailsActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { _ ->
            notifyDataSetChanged()
        }
    }

    private fun loadData() {
        viewModel.getPokemonData().removeObservers(viewLifecycleOwner)
        viewModel.getPokemonData().observe(viewLifecycleOwner) { dataMap ->
            buildPokemonList(dataMap)
            triggerLoadedObservers(true)
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.setHasFixedSize(true)

        // Grid or list
        val gridEnabled = !SettingsManager.getSetting<Boolean>(SettingType.LIST_VIEW)
        var layoutManager: RecyclerView.LayoutManager?
        if ( gridEnabled )
            layoutManager = GridLayoutManager(context, 2)
        else
            layoutManager  = LinearLayoutManager(context)

        binding.recyclerView.layoutManager = layoutManager

        // Add click/long click listeners on items
        binding.recyclerView.addOnItemTouchListener(
            RecyclerViewTouchListener(
                context,
                binding.recyclerView,
                object : RecyclerViewTouchListener.ClickListener {

                    // Open details activity on click
                    override fun onClick(view: View?, position: Int) {
                        val pokemonId = adapter?.getCurrentIds()?.get(position)
                        val intent = Intent(context, ActivityDetails::class.java)
                        intent.putExtra("PokemonId", pokemonId)

                        detailsActivityLauncher.launch(intent)
                    }

                    // Toggle discovered/captured status on long click
                    override fun onLongClick(view: View?, position: Int) {
                        val pokemonId = adapter?.getCurrentIds()?.get(position)
                        if (pokemonId != null) {

                            val isDiscovered = SettingsManager.isPokemonDiscovered(pokemonId)
                            val isCaptured = SettingsManager.isPokemonCaptured(pokemonId)
                            val showUndiscoveredInfo = SettingsManager.getSetting<Boolean>(SettingType.SHOW_UNDISCOVERED_INFO)

                            if (!showUndiscoveredInfo) {
                                if (!isDiscovered) {
                                    SettingsManager.togglePokemonDiscovered(pokemonId)
                                    if (view != null)
                                        activity?.let { UIUtils.createDefaultParticles(it, view.findViewById(R.id.imageThumbnail)) }
                                }
                                else if (!isCaptured) {
                                    SettingsManager.togglePokemonCaptured(pokemonId)
                                    if (view != null)
                                        activity?.let { UIUtils.createDefaultParticles(it, view.findViewById(R.id.imagePokeballCaptured)) }
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
                                        activity?.let { UIUtils.createDefaultParticles(it, view.findViewById(R.id.imagePokeballCaptured)) }
                                }
                            }

                            // Refresh data if required
                            val capturedOnly = SettingsManager.getSetting<Boolean>(SettingType.SHOW_CAPTURED_ONLY)
                            val discoveredOnly = SettingsManager.getSetting<Boolean>(SettingType.SHOW_DISCOVERED_ONLY)
                            // Refresh whole data set if captured/discovered only is checked
                            // and captured/discovered status has changed

//                            if ( capturedOnly && isCaptured != SettingsManager.isPokemonCaptured(pokemonId))
//                                rebuildDataSet() // TODO this is overkill, just need to remove items if not visible anymore
//                            else if ( discoveredOnly && isDiscovered != SettingsManager.isPokemonDiscovered(pokemonId))
//                                rebuildDataSet() // TODO ditto
//                            else
                            notifyItemChanged(position)
                        }
                    }
                })
        )
    }

    private fun buildPokemonList(dataMap: Map<Int, ViewModelPokemonData>) {
        val gridEnabled = !SettingsManager.getSetting<Boolean>(SettingType.LIST_VIEW)
        // TODO dont reconstruct an adapter everytime, use diffutil stuff instead
        adapter = ListAdapter(context, dataMap, gridEnabled)
        binding.recyclerView.adapter = adapter
    }

    fun addLoadedObserver(observer: (Boolean) -> Unit) {
        loadedObservers.add(observer)
    }
    private fun triggerLoadedObservers(loaded: Boolean) {
        loadedObservers.forEach { it(loaded) }
    }

    fun notifyDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    fun notifyItemChanged(position: Int) {
        adapter?.notifyItemChanged(position)
    }

//    fun rebuildDataSet() {
//        buildPokemonList()
//        notifyDataSetChanged()
//    }

    fun filterQuery(text: String?) {
        adapter?.filter?.filter(text)
    }
}