package fr.amazer.pokechu.managers.settings

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

@Suppress("UNCHECKED_CAST")
class MultiPrefixedLivePreference<T> constructor(
    private val updates: Observable<String>,
    private val preferences: SharedPreferences,
    private val prefix: String,
    private val defaultValue: T?
) : MutableLiveData<Map<String, T?>>() {
    private var disposable: Disposable? = null
    private val values = mutableMapOf<String, T?>()

    init {
        initializeValues()
    }

    private fun initializeValues() {
        preferences.all.forEach { (key, value) ->
            if (key.startsWith(prefix))
                values[key] = value as T
        }
        value = values
    }

    override fun onActive() {
        super.onActive()

        // TODO done this to rescan discovered/captured preference keys on main activity
        // getting active back again (back from settings)
        initializeValues()

        disposable = updates
            .filter { t -> t.startsWith(prefix) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                values[it] = (preferences.all[it] as T) ?: defaultValue
                postValue(values)
            }
    }

    override fun onInactive() {
        super.onInactive()
        disposable?.dispose()
    }
}