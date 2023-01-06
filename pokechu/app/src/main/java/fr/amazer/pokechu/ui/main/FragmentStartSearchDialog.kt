package fr.amazer.pokechu.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.DialogFragment
import fr.amazer.pokechu.R
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.UIUtils


class FragmentStartSearchDialog : DialogFragment() {

    private lateinit var binding: View

    // (Int,Boolean) => (Id,IsNational)
    private val searchQueryListeners = mutableListOf<(Int,Boolean) -> Unit>()
    fun addsearchQueryListeners(listener: (Int,Boolean) -> Unit) {
        searchQueryListeners.add(listener)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater

        binding = inflater.inflate(R.layout.dialog_search, null)

        val editText = binding.findViewById(R.id.textId) as EditText
        UIUtils.focusAndShowKeyboard(requireContext(), editText)

        val uniqueCheckbox = binding.findViewById(R.id.checkboxUnique) as CheckBox

        val isNationalPokedex = SettingsManager.getSetting<Int>(PreferenceType.SELECTED_REGION) == Region.NATIONAL.ordinal
        uniqueCheckbox.isChecked = isNationalPokedex
        uniqueCheckbox.isClickable = !isNationalPokedex

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(binding)
            .setMessage(R.string.search_dialog_title)
            // Add action buttons
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val pokemonId = editText.text.toString().toIntOrNull()
                if (pokemonId != null) {
                    searchQueryListeners.forEach { it(pokemonId, uniqueCheckbox.isChecked) }
                }
            }
        editText.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val alertDialog = dialog as AlertDialog
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                return@OnEditorActionListener true
            }
            false
        })

        val dialog = builder.create()

        return dialog
    }
}
