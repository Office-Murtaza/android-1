package com.app.belcobtm.presentation.features.settings.verification.blank

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.settings.interactor.GetVerificationCountryListUseCase
import com.app.belcobtm.domain.settings.interactor.SendVerificationBlankUseCase
import com.app.belcobtm.domain.settings.item.VerificationBlankDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class VerificationBlankViewModel(
    private val tierId: Int,
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
        zipCode: String
    ) {
        val blankItem = VerificationBlankDataItem(
            tierId,
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
        uploadUseCase.invoke(SendVerificationBlankUseCase.Params(blankItem)) { either ->
            either.either(
                { uploadingLiveData.value = LoadingData.Error(it) },
                { uploadingLiveData.value = LoadingData.Success(it) }
            )
        }
    }
}