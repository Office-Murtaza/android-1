package com.belcobtm.presentation.features.settings.verification.vip

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.settings.interactor.SendVerificationVipUseCase
import com.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.belcobtm.domain.settings.item.VerificationVipDataItem
import com.belcobtm.presentation.core.mvvm.LoadingData

class VerificationVipViewModel(
    private val vipUseCase: SendVerificationVipUseCase
) : ViewModel() {
    val uploadingLiveData = MutableLiveData<LoadingData<Unit>>()
    var fileUri: Uri? = null

    fun sendBlank(
        file: Uri,
        ssn: String,
        verificationDataItem: VerificationInfoDataItem
    ) {

        val dataItem = with(verificationDataItem) {
            VerificationVipDataItem(
                idCardNumber = idCardNumber,
                idCardNumberFilename = idCardNumberFilename,
                address = address,
                city = city,
                country = country,
                province = province,
                zipCode = zipCode,
                firstName = firstName,
                lastName = lastName,
                fileUri = file,
                ssn = ssn.toInt()
            )
        }
        uploadingLiveData.value = LoadingData.Loading()
        vipUseCase.invoke(SendVerificationVipUseCase.Params(dataItem),
            onSuccess = { uploadingLiveData.value = LoadingData.Success(it) },
            onError = { uploadingLiveData.value = LoadingData.Error(it) }
        )
    }
}