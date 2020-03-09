package com.app.belcobtm.presentation.features.settings.verification.vip

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.settings.interactor.SendVerificationVipUseCase
import com.app.belcobtm.domain.settings.item.VerificationVipDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class VerificationVipViewModel(
    private val tierId: Int,
    private val vipUseCase: SendVerificationVipUseCase
) : ViewModel() {
    val uploadingLiveData = MutableLiveData<LoadingData<Unit>>()
    var fileUri: Uri? = null

    fun sendBlank(
        file: Uri,
        ssn: String
    ) {
        val dataItem = VerificationVipDataItem(tierId, file, ssn)
        uploadingLiveData.value = LoadingData.Loading()
        vipUseCase.invoke(SendVerificationVipUseCase.Params(dataItem)) { either ->
            either.either(
                { uploadingLiveData.value = LoadingData.Error(it) },
                { uploadingLiveData.value = LoadingData.Success(it) }
            )
        }
    }
}