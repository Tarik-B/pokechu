package com.example.pokechu_material3

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.EditText
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.example.pokechu_material3.FirstFragment.RecyclerTouchListener.ClickListener
import com.example.pokechu_material3.databinding.FragmentFirstBinding
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var Searchtext: EditText? = null
    private var adapter: ExampleAdapter? = null
    private var exampleList: MutableList<ExampleItem?>? = null
    private val examples: List<ExampleItem>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }*/
        fillExampleList()
        setUpRecyclerView()
        initToolbar()
        /*view.findViewById<View>(R.id.filter).setOnClickListener {
            val intent = Intent(context, Country_A::class.java)
            startActivity(intent)
        }
        Searchtext = view.findViewById<View>(R.id.search_input) as EditText
        Searchtext!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                filterQuery(editable.toString())
            }
        })*/

        // Associate searchable configuration with the SearchView

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fillExampleList() {
        exampleList = ArrayList<ExampleItem?>()
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "One", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Two", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Three", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Four", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Five", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Six", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Seven", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Eight", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Nine", "sub title"))
        exampleList!!.add(ExampleItem(R.drawable.ic_filter_list_black_24dp, "Ten", "sub title"))
    }

    private fun setUpRecyclerView() {
        val recyclerView = view?.findViewById<View>(R.id.RecyclerView) as RecyclerView
        recyclerView.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        adapter = exampleList?.let { ExampleAdapter(it) }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                context,
                recyclerView,
                object : ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        val pokemonId = exampleList?.get(position)?.text1
                        val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(pokemonId)
                        findNavController().navigate(action)
                    }

                    override fun onLongClick(view: View?, position: Int) {

                    }
                })
        )
    }

    class RecyclerTouchListener(
        context: Context?,
        recyclerView: RecyclerView,
        private val clickListener: ClickListener?
    ) :
        OnItemTouchListener {
        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y)
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child))
                    }
                }
            })
        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        interface ClickListener {
            fun onClick(view: View?, position: Int)
            fun onLongClick(view: View?, position: Int)
        }
    }


    private fun initToolbar() {
        //setSupportActionBar(view.findViewById<View>(R.id.toolbar) as Toolbar)
        //supportActionBar!!.title = "Filter Activity"
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    /* access modifiers changed from: private */
    fun filterQuery(text: String?) {
        val filterdNames = ArrayList<ExampleItem?>()
        for (s in exampleList!!) {
            if (s!!.text1.lowercase(Locale.getDefault()).contains(text!!) || s.text2.lowercase(
                    Locale.getDefault()
                ).contains(
                    text
                )
            ) {
                filterdNames.add(s)
            }
        }
        adapter!!.setFilter(filterdNames)
    }
}