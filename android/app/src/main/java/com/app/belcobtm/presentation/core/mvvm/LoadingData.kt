package com.app.belcobtm.presentation.core.mvvm

import com.app.belcobtm.domain.Failure

sealed class LoadingData<T> {
    class Success<T>(val data: T) : LoadingData<T>()
    class Loading<T>(val data: T? = null) : LoadingData<T>()
    class Error<T>(val errorType: Failure? = null, val data: T? = null) : LoadingData<T>()
}