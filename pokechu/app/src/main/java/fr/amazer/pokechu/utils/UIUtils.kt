package fr.amazer.pokechu.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import com.plattysoft.leonids.ParticleSystem
import fr.amazer.pokechu.R


class UIUtils {
    companion object {

        fun showKeyboard(context: Context, view: View) {
            if (view.isFocused) {
                view.post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        fun focusAndShowKeyboard(context: Context, view: View) {
            view.requestFocus()
            if (view.hasWindowFocus()) {
                // No need to wait for the window to get focus.
                showKeyboard(context, view)
            } else {
                // We need to wait until the window gets focus.
                view.viewTreeObserver.addOnWindowFocusChangeListener(
                    object : ViewTreeObserver.OnWindowFocusChangeListener {
                        override fun onWindowFocusChanged(hasFocus: Boolean) {
                            // This notification will arrive just before the InputMethodManager gets set up.
                            if (hasFocus) {
                                showKeyboard(context, view)
                                // It’s very important to remove this listener once we are done.
                                view.viewTreeObserver.removeOnWindowFocusChangeListener(this)
                            }
                        }
                    })
            }
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

        fun createDefaultParticles(activity: Activity, view: View, resId: Int = R.drawable.star,
                                   numParticles: Int = 25, timeToLive: Long = 500L) {
            ParticleSystem(activity, numParticles, resId, timeToLive)
                .setSpeedRange(0.1f, 0.25f)
                .oneShot(view, numParticles)
        }
    }
}