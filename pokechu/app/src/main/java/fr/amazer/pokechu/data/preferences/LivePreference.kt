package fr.amazer.pokechu.data.preferences

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

@Suppress("UNCHECKED_CAST")
class LivePreference<T> constructor(
    private val updates: Observable<String>,
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: T?
) : MutableLiveData<T>() {

    private var disposable: Disposable? = null
    private var lastValue: T? = null

    init {
        lastValue = getPreferenceValue(key) ?: defaultValue
        value = lastValue
    }

    override fun onActive() {
        super.onActive()

        // First condition prevents "null" from being posted after default value
        // if the preference doesn't exist (fresh install)
        if (preferences.all.containsKey(key) && lastValue != getPreferenceValue(key)) {
            lastValue = getPreferenceValue(key)
            postValue(lastValue)
        }

        disposable = updates
            .filter { t -> t == key }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                lastValue = (getPreferenceValue(it) ?: defaultValue)
                postValue(lastValue)
            }
    }

    override fun onInactive() {
        super.onInactive()
        disposable?.dispose()
    }

    private fun getPreferenceValue(key: String): T {
        return preferences.all[key] as T
    }
}