package com.app.belcobtm.presentation.core.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class CombinedLiveData<T, K, F, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    source3: LiveData<F>,
    private val combine: (data1: T?, data2: K?, data3: F?) -> S
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null
    private var data3: F? = null

    init {
        super.addSource(source1) {
            data1 = it
            value = combine(data1, data2, data3)
        }
        super.addSource(source2) {
            data2 = it
            value = combine(data1, data2, data3)
        }
        super.addSource(source3) {
            data3 = it
            value = combine(data1, data2, data3)
        }
    }

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemove: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}