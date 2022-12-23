package fr.amazer.pokechu.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.R
import fr.amazer.pokechu.activities.ActivityDetails
import fr.amazer.pokechu.data.NationalIdLocalId
import fr.amazer.pokechu.data.PokedexType
import fr.amazer.pokechu.data.PokemonIdTypesId
import fr.amazer.pokechu.data.PokemonType
import fr.amazer.pokechu.databinding.FragmentListBinding
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.ListAdapter
import fr.amazer.pokechu.ui.ListAdapterData
import fr.amazer.pokechu.ui.RecyclerTouchListener
import fr.amazer.pokechu.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class FragmentList : Fragment() {
    private lateinit var binding: FragmentListBinding
    private var adapter: ListAdapter? = null
    private var typesMap = HashMap<Int,List<PokemonType>>()

    private lateinit var detailsActivityLauncher: ActivityResultLauncher<Intent>

    // Only used to trigger observers
    private var loaded: Boolean by Delegates.observable(false) { _, _, newValue ->
        loadedObservers.forEach { it(newValue) }
    }
    private val loadedObservers = mutableListOf<(Boolean) -> Unit>()
    fun addLoadedObserver(observer: (Boolean) -> Unit) {
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
//        val view = inflater.inflate(R.layout.fragment_list, container, false)
        binding = FragmentListBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suspend fun getPokemonsTypes(): List<PokemonIdTypesId> = withContext(Dispatchers.IO) {
            return@withContext DatabaseManager.findPokemonsTypes()
        }
        lifecycleScope.launch { // coroutine on main
            val pokemonTypes = getPokemonsTypes()
            // back on main

            // Build pokemon id -> types map
            typesMap = HashMap<Int, List<PokemonType>>()
            pokemonTypes.forEach{ value ->
                typesMap[value.pokemon_id] = value.type_id_list as List<PokemonType>
            }

            setUpRecyclerView()

            // Get pokemons for selected region and build map of id -> adapter data
            buildPokemonList()

            // Add listener on selected region
            SettingsManager.addSelectedRegionListener {
                rebuildDataSet()
            }
        }

        detailsActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            notifyDataSetChanged()
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.setHasFixedSize(true)

        // Grid or list
        val gridEnabled = !SettingsManager.isListViewEnabled()
        var layoutManager: RecyclerView.LayoutManager? = null
        if ( gridEnabled )
            layoutManager = GridLayoutManager(context, 2)
        else
            layoutManager  = LinearLayoutManager(context)

        binding.recyclerView.layoutManager = layoutManager

        // Add click/long click listeners on items
        binding.recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                context,
                binding.recyclerView,
                object : RecyclerTouchListener.ClickListener {

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
                            val showUndiscoveredInfo = context?.let { SettingsManager.isShowUndiscoveredInfoEnabled() }

                            if (showUndiscoveredInfo == false) {
                                if (!isDiscovered) {
                                    SettingsManager.togglePokemonDiscovered(pokemonId)
                                    if (view != null)
                                        activity?.let { UIUtils.createDefaultParticles(it, view.findViewById(R.id.image_thumbnail)) }
                                }
                                else if (!isCaptured) {
                                    SettingsManager.togglePokemonCaptured(pokemonId)
                                    if (view != null)
                                        activity?.let { UIUtils.createDefaultParticles(it, view.findViewById(R.id.image_pokeball_captured)) }
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
                                        activity?.let { UIUtils.createDefaultParticles(it, view.findViewById(R.id.image_pokeball_captured)) }
                                }
                            }


                            adapter?.notifyItemChanged(position)
                        }
                    }
                })
        )
    }

    private fun isPokemonDisplayed(id: Int): Boolean {
        val capturedOnly = SettingsManager.isShowCapturedOnlyEnabled()
        val discoveredOnly = SettingsManager.isShowDiscoveredOnlyEnabled()

        return ( (!capturedOnly!! || SettingsManager.isPokemonCaptured(id))
                && (!discoveredOnly!! || SettingsManager.isPokemonDiscovered(id)) )
    }

    private fun buildPokemonList() {
        loaded = false

        val selectedRegion = SettingsManager.getSelectedRegion()

        suspend fun getPokemonData(region: Int): Map<Int, ListAdapterData> = withContext(Dispatchers.IO) {
            if (region == PokedexType.NATIONAL.ordinal) {
                val pokemonIds: List<Int> = DatabaseManager.findPokemonIds()
                val dataMap = HashMap<Int, ListAdapterData>()
//                val pokemonData = ArrayList<ListAdapterData>()
                pokemonIds.forEach{ id ->
                    if (isPokemonDisplayed(id))
//                        pokemonData.add(ListAdapterData(id))
                        dataMap[id] = ListAdapterData(id)
                }

//                val dataMap = pokemonIds.zip(pokemonData).toMap()
                return@withContext dataMap
            }
            else {
                val pokemonLocalIds: List<NationalIdLocalId> = DatabaseManager.findPokemonRegions(selectedRegion)
                val dataMap = HashMap<Int, ListAdapterData>()
                pokemonLocalIds.forEach{ id ->
                    if (isPokemonDisplayed(id.pokemon_id))
                        dataMap[id.pokemon_id] = ListAdapterData(id.local_id)
                }
                return@withContext dataMap
            }
        }
        lifecycleScope.launch { // coroutine on main
            val pokemonData = getPokemonData(selectedRegion) // coroutine on IO
            // back on main

            // Fill localized names in data
            pokemonData.forEach{ (key, data) ->
                LocalizationManager.getLanguages().forEach{ lang ->
                    data.names[lang] = LocalizationManager.getPokemonName(requireContext(), key, lang) ?: ""
                }
            }
            val gridEnabled = !SettingsManager.isListViewEnabled()
            val uiItemId = if (gridEnabled) R.layout.list_grid_item else R.layout.list_item

            adapter = ListAdapter(context, pokemonData, typesMap, uiItemId)
            binding.recyclerView.adapter = adapter

//            delay(2000L)

            loaded = true
        }
    }

    public fun notifyDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    public fun rebuildDataSet() {
        buildPokemonList()
        notifyDataSetChanged()
    }

    public fun filterQuery(text: String?) {
        adapter?.filter?.filter(text)
    }

    companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentList.
     */
    @JvmStatic
    fun newInstance() =
        FragmentList().apply {
            arguments = Bundle().apply {
            }
        }
    }
}