package fr.amazer.pokechu.managers

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

@Suppress("UNCHECKED_CAST")
open class MultiLivePreference<T> constructor(
    private val updates: Observable<String>,
    private val preferences: SharedPreferences,
    private val keys: List<String>,
    private val defaultValue: T?
) : MutableLiveData<Map<String, T?>>() {

    private var disposable: Disposable? = null
    private val values = mutableMapOf<String, T?>()

    init {
        for (key in keys) {
            values[key] = (preferences.all[key] as T) ?: defaultValue
        }

        value = values
    }

    override fun onActive() {
        super.onActive()
        disposable = updates
            .filter { t -> keys.contains(t) }
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