package fr.amazer.pokechu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.DialogFragment
import fr.amazer.pokechu.R
import fr.amazer.pokechu.activities.ActivityDetails
import fr.amazer.pokechu.data.PokemonData
import fr.amazer.pokechu.managers.PokemonManager
import fr.amazer.pokechu.utils.UIUtils


class StartSearchDialogFragment : DialogFragment() {

    private lateinit var binding: View

    val OPEN_DETAILS = 123456

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            binding = inflater.inflate(R.layout.dialog_search, null)

            val editText = binding.findViewById(R.id.text_id) as EditText
            editText.requestFocus()
            context?.let { it1 -> UIUtils.showKeyboard(editText, it1) }
            UIUtils.showSoftKeyboard(editText, requireActivity().applicationContext)

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(binding)
                .setMessage(R.string.search_dialog_title)
                // Add action buttons
                .setPositiveButton(R.string.dialog_ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        val editText = binding.findViewById(R.id.text_id) as EditText
                        val pokemonId = editText.text.toString()

                        if ( pokemonId.toIntOrNull() != null ) {
                            val uniqueCheckbox = binding.findViewById(R.id.checkbox_unique) as CheckBox
                            var pokemonData: PokemonData? = null

                            if ( uniqueCheckbox.isChecked )
                                pokemonData = PokemonManager.findPokemonData(pokemonId)
                            else
                                pokemonData = PokemonManager.findPokemonDataPaldea(pokemonId)

                            if (pokemonData != null) {
                                val intent = Intent(context, ActivityDetails::class.java)
                                intent.putExtra("PokemonId", pokemonData.ids.unique)
                                startActivityForResult(intent, OPEN_DETAILS)
                            }
                        }
                    })
//                .setNegativeButton(R.string.dialog_cancel,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        getDialog()?.cancel()
//                    })

            editText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i(this::class.simpleName, "setOnEditorActionListener = ${actionId} ")

                    val alertDialog = dialog as AlertDialog
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                    return@OnEditorActionListener true
                }
                false
            })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}
