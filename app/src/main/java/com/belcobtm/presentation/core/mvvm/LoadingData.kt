package com.belcobtm.presentation.core.mvvm

import com.belcobtm.domain.Failure

sealed class LoadingData<T>(val commonData: T? = null) {
    class Success<T>(val data: T) : LoadingData<T>(data)
    class Loading<T>(val data: T? = null) : LoadingData<T>(data)
    class DismissProgress<T>(val data: T? = null) : LoadingData<T>(data)
    class Error<T>(val errorType: Failure? = null, val data: T? = null) : LoadingData<T>(data)
}