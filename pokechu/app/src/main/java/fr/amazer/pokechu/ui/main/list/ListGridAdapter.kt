package fr.amazer.pokechu.ui.main.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import fr.amazer.pokechu.databinding.ListGridItemBinding
import fr.amazer.pokechu.databinding.ListItemBinding
import fr.amazer.pokechu.viewmodel.ViewModelPokemonListData

val diffUtil = object : DiffUtil.ItemCallback<ListViewHolderData>(){
    override fun areItemsTheSame(
        oldItem: ListViewHolderData,
        newItem: ListViewHolderData
    ): Boolean {
        return oldItem.viewModelData.pokemonId == newItem.viewModelData.pokemonId
    }

    override fun areContentsTheSame(oldItem: ListViewHolderData, newItem: ListViewHolderData): Boolean {
        return (oldItem.viewModelData.pokemonId == newItem.viewModelData.pokemonId
                && oldItem.viewModelData.localId == newItem.viewModelData.localId
                && oldItem.viewModelData.isDiscovered == newItem.viewModelData.isDiscovered
                && oldItem.viewModelData.isCaptured == newItem.viewModelData.isCaptured
                && oldItem.filter == newItem.filter)
    }

//    override fun getChangePayload(oldItem: ViewModelPokemonListData, newItem: ViewModelPokemonListData): Any? {
//        val diff = mutableMapOf<String, Any>()
//        if (oldItem.isDiscovered != newItem.isDiscovered) {
//            diff["discovered"] = newItem.isDiscovered
//        }
//        if (oldItem.isCaptured != newItem.isCaptured) {
//            diff["captured"] = newItem.isCaptured
//        }
//        return if (diff.isEmpty()) { null } else diff
//    }
}

data class ListViewHolderData(
    val viewModelData: ViewModelPokemonListData,
    var filter: String
)

class ListGridAdapter internal constructor(
    private val context: Context?,
    ) : ListAdapter<ListViewHolderData, ListViewHolder>(diffUtil), Filterable, FastScrollRecyclerView.SectionedAdapter {
    private var originalList: List<ListViewHolderData> = currentList.toList()
    private var gridEnabled: Boolean = false
    private var filterPattern = ""

    enum class ViewType { LIST, GRID }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val binding: ViewDataBinding = when (viewType) {
            ViewType.GRID.ordinal -> ListGridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewType.LIST.ordinal -> ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            else -> throw Exception()
        }

        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentData = currentList[position]

        if (currentData != null)
            holder.bind(context!!, currentData)
    }

//    override fun onBindViewHolder(holder: PokemonListViewHolder, position: Int, payloads: MutableList<Any>) {
//        if (payloads.isEmpty()) {
//            super.onBindViewHolder(holder, position, payloads)
//        } else {
//            payloads.forEach { payload ->
//                val payloadMap = payload as Map<String, Any>
//                payloadMap.forEach{ (key, value) ->
//                    when(key) {
//                        "discovered" -> holder.bindIsDiscovered(value as Boolean)
//                        "captured" -> holder.bindIsCaptured(value as Boolean)
//                        else -> {}
//                    }
//                }
//            }
//            Log.i(this::class.simpleName, "payloads = $payloads")
//        }
//    }

    override fun getItemId(position: Int): Long {
        return currentList[position]?.viewModelData?.pokemonId?.toLong() ?: 0
    }

    fun getCurrentData(): List<ListViewHolderData> {
        return currentList
    }

    fun setGridEnabled(enabled: Boolean) {
        gridEnabled = enabled
        notifyItemRangeChanged(0, itemCount)
    }

    override fun getItemViewType(position: Int): Int {
        return if (gridEnabled) ViewType.GRID.ordinal else ViewType.LIST.ordinal
    }

    fun setDataList(list: List<ViewModelPokemonListData>, commitCallback: Runnable) {
        submitList( List(list.size){ i -> ListViewHolderData(list[i], filterPattern) }, commitCallback )
    }

    override fun submitList(list: List<ListViewHolderData>?) {
        submitList(list,null)
    }
    override fun submitList(list: List<ListViewHolderData>?, commitCallback: Runnable?) {
        if (filterPattern.isNotEmpty()) {

            val filter = filter as ListFilter
            filter.apply {
                newOriginalList = list
                callback = commitCallback
            }
            filter.filter(filterPattern)
        }
        else {
            internalSubmitList(list, true, commitCallback)
        }
    }

    private fun internalSubmitList(list: List<ListViewHolderData>?, original: Boolean, commitCallback: Runnable?) {
        if (original)
            originalList = list?: listOf()

        if (commitCallback != null)
            super.submitList(list, commitCallback)
        else
            super.submitList(list)
    }

    override fun getFilter(): Filter {
        return ListFilter()
    }

    inner class ListFilter : Filter() {

        public var newOriginalList: List<ListViewHolderData>? = null
        public var callback: Runnable? = null

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            filterPattern = constraint?.toString() ?: ""

            val filteredOriginalList = newOriginalList?: originalList
//            filteredOriginalList.forEach{ it -> it.filter = filterPattern }

            val filteredList:List<ListViewHolderData>
            if (filterPattern.isEmpty()) {
                filteredList = filteredOriginalList
            } else {
                filteredList = mutableListOf()
                filteredOriginalList
                    ?.filter { filterPokemon(it.viewModelData.pokemonId, filterPattern) }
                    ?.forEach { filteredList.add(ListViewHolderData(it.viewModelData, filterPattern)) }
            }

            return FilterResults().apply {
                values = filteredList
            }
        }

        private fun filterPokemon(id: Int, pattern: String): Boolean {
            val filteredOriginalList = newOriginalList?: originalList

            if (id.toString().contains(pattern))
                return true

            var pokemonData = filteredOriginalList.find { data -> (data.viewModelData.pokemonId == id)  } ?: return false

            if (pokemonData.viewModelData.localId.toString().contains(pattern))
                return true

            pokemonData.viewModelData.names.forEach{ (_, name) ->
                if (name.contains(pattern, true))
                    return true
            }

            return false
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (newOriginalList != null)
                originalList = newOriginalList!!

            internalSubmitList(results?.values as? List<ListViewHolderData>, false, callback)
        }
    }

    override fun getSectionName(position: Int): String {
        val id = currentList[position]?.viewModelData?.pokemonId!!
        var hundred = (id/100)
        var hundredString = "${hundred}${if (id > 100) "00" else ""}"
        var endHundred = (id+100)/100 // TODO max this by id max? count?
        val endHundredString = "${endHundred}00"

        return "$hundredString - $endHundredString"
    }

//    fun setData(dataList: List<ViewModelPokemonListData>) {
//        // Build sorted national ids by local ids
//        val nationalIds = List(dataList.size){ it -> dataList[it].pokemonId}
//        val localIds = List(dataList.size){ it -> dataList[it].localId}
//
//        val sortedLocalIds = localIds.sorted()
//
//        val sortedNationalIds = ArrayList<Int>()
//        sortedLocalIds.forEach{ local_id -> sortedNationalIds.add(nationalIds[localIds.indexOf(local_id)]) }
//
//
//        pokemonIdsFiltered = sortedNationalIds
//
//        pokemonIdsFull = ArrayList<Int>(pokemonIdsFiltered)
//        pokemonList = dataList
//    }
}