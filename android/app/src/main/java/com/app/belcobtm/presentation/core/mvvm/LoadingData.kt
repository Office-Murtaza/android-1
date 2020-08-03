package com.app.belcobtm.presentation.core.mvvm

import com.app.belcobtm.domain.Failure

sealed class LoadingData<T>(val commonData: T? = null) {
    class Success<T>(val data: T) : LoadingData<T>(data)
    class Loading<T>(val data: T? = null) : LoadingData<T>(data)
    class Error<T>(val errorType: Failure? = null, val data: T? = null) : LoadingData<T>(data)
}