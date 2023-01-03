package fr.amazer.pokechu.ui.main.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.databinding.ListGridItemBinding
import fr.amazer.pokechu.databinding.ListItemBinding
import fr.amazer.pokechu.viewmodel.ViewModelPokemonData


class ListAdapter internal constructor(
    private var context: Context?,
    private var pokemonsMap: Map<Int, ViewModelPokemonData>,
    private var gridEnabled: Boolean
//    private var uiItemId: Int
    ) : RecyclerView.Adapter<ListViewHolder>(), Filterable {
    private val pokemonIdsFull: List<Int>
    private var pokemonIdsFiltered: List<Int>
    private var currentFilter = ""

    init {
        // Build sorted national ids by local ids
        val nationalIds = pokemonsMap.keys.toList()
        val localIds = ArrayList<Int>()
        nationalIds.forEach{ id -> pokemonsMap[id]?.let { localIds.add(it.localId) } }

        val sortedLocalIds = localIds.sorted()

        val sortedNationalIds = ArrayList<Int>()
        sortedLocalIds.forEach{ local_id -> sortedNationalIds.add(nationalIds[localIds.indexOf(local_id)]) }

        pokemonIdsFiltered = sortedNationalIds
        pokemonIdsFull = ArrayList<Int>(pokemonIdsFiltered)

//        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding: ViewDataBinding
        if (gridEnabled)
            binding = ListGridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        else
            binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentId = pokemonIdsFiltered[position]
        val currentLocalId = pokemonsMap[currentId]!!.localId
        val currentData = pokemonsMap[currentId]

        if (currentData != null)
            holder.bind(context!!, currentId, currentLocalId, currentData, currentData.types, currentFilter)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                currentFilter = constraint?.toString() ?: ""
                if (currentFilter.isEmpty()) {
                    pokemonIdsFiltered = pokemonIdsFull
                }
                else {
                    val filteredList = ArrayList<Int>()
                    pokemonIdsFull
                        .filter { filterPokemon(it, currentFilter) }
                        .forEach { filteredList.add(it) }
                    pokemonIdsFiltered = filteredList

                }
                return FilterResults().apply { values = pokemonIdsFiltered }
            }

            fun filterPokemon(id: Int, pattern: String): Boolean {
                var pokemonData = pokemonsMap[id]
                pokemonData!!.names.forEach{ (_, name) ->
                    if (name.contains(pattern, true))
                        return true
                }
                return false
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                pokemonIdsFiltered = if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<Int>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return pokemonIdsFiltered.size
    }

    fun getCurrentIds():List<Int> {
        return pokemonIdsFiltered
    }
}