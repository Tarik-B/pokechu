package com.example.pokechu_android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.pokechu_android.databinding.FragmentFirstBinding
import android.widget.Toast

import android.widget.AdapterView

import android.widget.ArrayAdapter

import android.R
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.buttonFirst.setOnClickListener {
        //    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        //}

        val strings: Array<out String> = resources.getStringArray(com.example.pokechu_android.R.array.array_technology)

        val adapter = ArrayAdapter(
            view.getContext(),
            R.layout.simple_list_item_1, R.id.text1, strings
        )
        binding.listView.setAdapter(adapter)
        binding.listView.setOnItemClickListener(OnItemClickListener { adapterView, view, position, l -> // TODO Auto-generated method stub
            val value = adapter.getItem(position) as String
            //Toast.makeText(applicationContext, value, Toast.LENGTH_SHORT).show()
            Snackbar.make(view, value, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        })

        // Get a reference to the AutoCompleteTextView in the layout
        val textView = binding.autoCompleteTextView
        //textView.visibility = View.INVISIBLE
        //textView.visibility = View.VISIBLE

        //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show()
        //textView.isFocusable = true
        //textView.requestFocus()
        //val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        //imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT)

        textView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            Log.i("TAG", "Focus changed = " + hasFocus);
            if (!hasFocus) {
                // code to execute when EditText loses focus
            }
        }

        // Create the adapter and set it to the AutoCompleteTextView
        context?.let {
            ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, strings).also { adapter ->
                textView.setAdapter(adapter)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}