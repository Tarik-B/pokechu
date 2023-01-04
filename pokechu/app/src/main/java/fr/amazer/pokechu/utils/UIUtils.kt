package fr.amazer.pokechu.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.plattysoft.leonids.ParticleSystem
import fr.amazer.pokechu.R


class UIUtils {
    companion object {

        fun showKeyboard(view: View, context: Context) {
            val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        fun closeKeyboard(context: Context) {
            val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY)
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

        fun animateView(view: View, toVisibility: Int, toAlpha: Float, duration: Int) {
            val show = toVisibility == View.VISIBLE
            if (show) {
                view.alpha = 0f
            }
            view.visibility = View.VISIBLE
            view.animate()
                .setDuration(duration.toLong())
                .alpha(if (show) toAlpha else 0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = toVisibility
                    }
                })
        }

        fun createDefaultParticles(activity: Activity, view: View) {
            ParticleSystem(activity, 25, R.drawable.star, 500)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(view, 25)
        }
    }
}