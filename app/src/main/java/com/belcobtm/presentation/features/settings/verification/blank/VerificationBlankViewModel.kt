package com.belcobtm.presentation.features.settings.verification.blank

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.settings.interactor.GetVerificationCountryListUseCase
import com.belcobtm.domain.settings.interactor.SendVerificationBlankUseCase
import com.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.belcobtm.presentation.core.mvvm.LoadingData

class VerificationBlankViewModel(
    private val uploadUseCase: SendVerificationBlankUseCase,
    private val countriesUseCase: GetVerificationCountryListUseCase
) : ViewModel() {
    val uploadingLiveData = MutableLiveData<LoadingData<Unit>>()
    val countries = countriesUseCase.invoke()
    var fileUri: Uri? = null

    fun sendBlank(
        file: Uri,
        idNumber: String,
        firstName: String,
        lastName: String,
        address: String,
        city: String,
        country: String,
        province: String,
        zipCode: String,
        verificationDataItem: VerificationInfoDataItem
    ) {
        val blankItem = VerificationBlankDataItem(
            verificationDataItem.id,
            file,
            idNumber,
            firstName,
            lastName,
            address,
            city,
            country,
            province,
            zipCode
        )
        uploadingLiveData.value = LoadingData.Loading()
        uploadUseCase.invoke(SendVerificationBlankUseCase.Params(blankItem),
            onSuccess = { uploadingLiveData.value = LoadingData.Success(it) },
            onError = { uploadingLiveData.value = LoadingData.Error(it) }
        )
    }
}