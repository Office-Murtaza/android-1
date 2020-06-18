package com.app.belcobtm.presentation.core.mvvm

sealed class LoadingData<T> {
    class Success<T>(val data: T) : LoadingData<T>()
    class Loading<T>(val data: T? = null) : LoadingData<T>()
    class Error<T>(val errorType: Exception? = null, val data: T? = null) : LoadingData<T>()
}

fun <T> LoadingData<T>.isSuccess(): Boolean = this is LoadingData.Success