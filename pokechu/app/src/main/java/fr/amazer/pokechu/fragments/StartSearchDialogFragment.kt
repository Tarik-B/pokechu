package fr.amazer.pokechu.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import fr.amazer.pokechu.R
import fr.amazer.pokechu.activities.ActivityDetails
import fr.amazer.pokechu.data.Pokemon
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
                        val pokemonId = editText.text.toString().toIntOrNull()
                        if ( pokemonId != null ) {

                            // Search id and open details
                            suspend fun getPokemonById(id: Int): Pokemon? = withContext(Dispatchers.IO) {
                                return@withContext DatabaseManager.findPokemonById(id)
                            }
                            lifecycleScope.launch { // coroutine on Main
                                val pokemon = getPokemonById(pokemonId) // coroutine on IO
                                // back on main
//                                if ( uniqueCheckbox.isChecked )
//                                    dataPokemon = DataManager.findPokemonData(pokemonId)
//                                else
//                                    dataPokemon = DataManager.findPokemonDataPaldea(pokemonId)

                                // Id found, open details
                                if (pokemon != null) {
                                    val intent = Intent(context, ActivityDetails::class.java)
                                    intent.putExtra("PokemonId", pokemonId.toInt())
                                    startActivityForResult(intent, OPEN_DETAILS)
                                }
                                else {
                                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                        "ID ${pokemonId} not found ", Snackbar.LENGTH_LONG).show();
                                }
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
