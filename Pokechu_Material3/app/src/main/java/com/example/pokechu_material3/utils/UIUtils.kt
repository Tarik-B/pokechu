package com.example.pokechu_material3.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService

class UIUtils {
    companion object {

        fun showKeyboard(view: View, context: Context) {
            val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }

        fun showSoftKeyboard(view: View, context: Context) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }

        fun closeKeyboard(view: View, context: Context) {
            val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.toggleSoftInput()
//            showSoftInput(view, InputMethodManager.SHOW_FORCED)
        }

        fun reloadActivity(activity: Activity, skipTransition: Boolean) {
//            activity.recreate()
            activity.finish()

            if (skipTransition)
                activity.overridePendingTransition(0, 0)
            activity.startActivity(activity.intent)

            if (skipTransition)
                activity.overridePendingTransition(0, 0)
        }
    }
}