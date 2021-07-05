package com.belcobtm.presentation.features.settings.verification.vip

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.settings.interactor.SendVerificationVipUseCase
import com.belcobtm.domain.settings.item.VerificationVipDataItem
import com.belcobtm.presentation.core.mvvm.LoadingData

class VerificationVipViewModel(
    private val vipUseCase: SendVerificationVipUseCase
) : ViewModel() {
    val uploadingLiveData = MutableLiveData<LoadingData<Unit>>()
    var fileUri: Uri? = null

    fun sendBlank(
        file: Uri,
        ssn: String
    ) {
        val dataItem = VerificationVipDataItem(file, ssn.toInt())
        uploadingLiveData.value = LoadingData.Loading()
        vipUseCase.invoke(SendVerificationVipUseCase.Params(dataItem),
            onSuccess = { uploadingLiveData.value = LoadingData.Success(it) },
            onError = { uploadingLiveData.value = LoadingData.Error(it) }
        )
    }
}