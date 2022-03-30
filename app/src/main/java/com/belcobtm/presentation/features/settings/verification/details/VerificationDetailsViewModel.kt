package com.belcobtm.presentation.features.settings.verification.details

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.R
import com.belcobtm.domain.settings.interactor.GetVerificationCountryListUseCase
import com.belcobtm.domain.settings.interactor.GetVerificationDetailsUseCase
import com.belcobtm.domain.settings.interactor.GetVerificationFieldsUseCase
import com.belcobtm.domain.settings.interactor.SendVerificationIdentityUseCase
import com.belcobtm.domain.settings.item.VerificationDetailsDataItem
import com.belcobtm.domain.settings.item.VerificationIdentityDataItem
import com.belcobtm.domain.settings.item.VerificationSupportedCountryDataItem
import com.belcobtm.domain.settings.type.RecordStatus
import com.belcobtm.domain.settings.type.VerificationStep
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.mvvm.LoadingData

class VerificationDetailsViewModel(
    private val sendVerificationIdentityUseCase: SendVerificationIdentityUseCase,
    private val getVerificationDetailsUseCase: GetVerificationDetailsUseCase,
    private val getVerificationFieldsUseCase: GetVerificationFieldsUseCase,
    private val countriesUseCase: GetVerificationCountryListUseCase,
    private val priceFormatter: Formatter<Double>,
) : ViewModel() {
    val detailsStateData = MutableLiveData<LoadingData<VerificationDetailsState>>()
    val identityStateData = MutableLiveData<LoadingData<VerificationIdentityState>>()
    val actionData = SingleLiveData<VerificationDetailsAction>()
    val countries = countriesUseCase.invoke()
    var currentStep = VerificationStep.COUNTRY_VERIFICATION_STEP
    var selectedCountry: VerificationSupportedCountryDataItem? = null
    var fileUri: Uri? = null
    var item: VerificationDetailsDataItem? = null

    fun fetchVerificationStatus() {
        detailsStateData.value = LoadingData.Loading()
        getVerificationDetailsUseCase.invoke(Unit,
            onSuccess = {
                item = it
                getVerificationStep(it)
                detailsStateData.value = LoadingData.Success(
                    getVerificationDetailsStateByStep(currentStep)
                )
            },
            onError = {
                detailsStateData.value = LoadingData.Error(it)
            }
        )
    }

    fun fetchVerificationFields() {
        identityStateData.value = LoadingData.Loading()
        selectedCountry?.let {
            getVerificationFieldsUseCase.invoke(GetVerificationFieldsUseCase.Params(it.code),
                onSuccess = {

                },
                onError = {
                    identityStateData.value = LoadingData.Error(it)
                }
            )
        } ?: run {

        }
    }

    fun onBackClick() {
        if (currentStep != VerificationStep.COUNTRY_VERIFICATION_STEP) {
            currentStep = when (currentStep) {
                VerificationStep.IDENTITY_VERIFICATION_STEP -> VerificationStep.COUNTRY_VERIFICATION_STEP
                VerificationStep.DOCUMENT_VERIFICATION_STEP -> VerificationStep.IDENTITY_VERIFICATION_STEP
                VerificationStep.COUNTRY_VERIFICATION_STEP -> VerificationStep.COUNTRY_VERIFICATION_STEP
            }
            detailsStateData.value = LoadingData.Success(
                getVerificationDetailsStateByStep(currentStep)
            )
        }
    }

    fun onIdentityVerificationNext(dataItem: VerificationIdentityDataItem) {
        identityStateData.value = LoadingData.Loading()
        sendVerificationIdentityUseCase.invoke(SendVerificationIdentityUseCase.Params(dataItem),
            onSuccess = {
                identityStateData.value = LoadingData.Success<VerificationIdentityState>(
                    VerificationIdentityState(recordStatus = it.recordStatus)
                )
                if (it.recordStatus == RecordStatus.MATCH) {
                    currentStep = VerificationStep.DOCUMENT_VERIFICATION_STEP
                    detailsStateData.value = LoadingData.Success(
                        getVerificationDetailsStateByStep(currentStep)
                    )
                }
            },
            onError = {
                identityStateData.value = LoadingData.Error(it)
            }
        )


    }

    fun onCountryVerificationNext() {
        if (selectedCountry != null) {
            currentStep = VerificationStep.IDENTITY_VERIFICATION_STEP
            detailsStateData.value = LoadingData.Success(
                getVerificationDetailsStateByStep(currentStep)
            )
        }
    }

    fun onDocumentVerificationSubmit() {

    }

//        actionData.value = VerificationDetailsAction.NavigateAction(
//            when (item?.status) {
//                VerificationStatus.NOT_VERIFIED,
//                VerificationStatus.VERIFICATION_REJECTED ->
//                    VerificationDetailsFragmentDirections.verificationInfoToVerify(item ?: return)
//                VerificationStatus.VERIFIED,
//                VerificationStatus.VIP_VERIFICATION_REJECTED ->
//                    VerificationDetailsFragmentDirections.verificationInfoToVipVerify(
//                        item ?: return
//                    )
//                else -> throw IllegalStateException("Not available for verification for this state")
//            }
//        )

    private fun getVerificationDetailsStateByStep(step: VerificationStep) =
        VerificationDetailsState(
            countryStepTextColor = getCountryStepTextColor(step),
            countryStepBackground = getCountryStepBackground(step),
            countryStepIcon = getCountryStepIcon(step),
            countryStepIconColor = getCountryStepIconColor(step),
            identityStepBackground = getIdentityStepBackground(step),
            identityStepTextColor = getIdentityStepTextColor(step),
            identityStepIcon = getIdentityStepIcon(step),
            identityStepIconColor = getIdentityStepIconColor(step),
            documentStepBackground = getDocumentStepBackground(step),
            documentStepIcon = getDocumentStepIcon(step),
            documentStepIconColor = getDocumentStepIconColor(step),
            documentStepTextColor = getDocumentStepTextColor(step),
            currentStep = step
        )

    private fun getVerificationStep(verificationDetails: VerificationDetailsDataItem) {
        if (true || verificationDetails.identityVerification != null && verificationDetails.identityVerification.recordStatus == RecordStatus.MATCH)
            currentStep = VerificationStep.DOCUMENT_VERIFICATION_STEP
        else
            currentStep = VerificationStep.COUNTRY_VERIFICATION_STEP
    }

    private fun getCountryStepIcon(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.drawable.ic_1
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.drawable.ic_checked
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.drawable.ic_checked
            else -> R.drawable.ic_1
        }
    }

    private fun getIdentityStepIcon(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.drawable.ic_2
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.drawable.ic_2
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.drawable.ic_checked
            else -> R.drawable.ic_2
        }
    }

    private fun getDocumentStepIcon(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.drawable.ic_3
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.drawable.ic_3
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.drawable.ic_3
            else -> R.drawable.ic_3
        }
    }

    private fun getCountryStepIconColor(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.color.colorWhite
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.color.colorWhite
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.color.colorWhite
            else -> R.color.colorWhite
        }
    }

    private fun getIdentityStepIconColor(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.color.gray_text_color
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.color.colorWhite
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.color.colorWhite
            else -> R.color.colorWhite
        }
    }

    private fun getDocumentStepIconColor(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.color.gray_text_color
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.color.gray_text_color
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.color.colorWhite
            else -> R.color.colorWhite
        }
    }

    private fun getCountryStepTextColor(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.color.black_text_color
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.color.gray_text_color
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.color.gray_text_color
            else -> R.color.gray_text_color
        }
    }

    private fun getIdentityStepTextColor(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.color.gray_text_color
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.color.black_text_color
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.color.gray_text_color
            else -> R.color.gray_text_color
        }
    }

    private fun getDocumentStepTextColor(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.color.gray_text_color
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.color.gray_text_color
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.color.black_text_color
            else -> R.color.gray_text_color
        }
    }

    private fun getCountryStepBackground(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.drawable.circular_blue_background
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.drawable.circular_blue_background
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.drawable.circular_blue_background
            else -> R.drawable.circular_blue_background
        }
    }

    private fun getIdentityStepBackground(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.drawable.gray_border_background
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.drawable.circular_blue_background
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.drawable.circular_blue_background
            else -> R.drawable.circular_blue_background
        }
    }

    private fun getDocumentStepBackground(step: VerificationStep): Int {
        return when (step) {
            VerificationStep.COUNTRY_VERIFICATION_STEP -> R.drawable.gray_border_background
            VerificationStep.IDENTITY_VERIFICATION_STEP -> R.drawable.gray_border_background
            VerificationStep.DOCUMENT_VERIFICATION_STEP -> R.drawable.circular_blue_background
            else -> R.drawable.circular_blue_background
        }
    }

}

data class VerificationDetailsState(
    @DrawableRes val countryStepIcon: Int = R.drawable.ic_1,
    @DrawableRes val countryStepIconColor: Int = R.color.dark_text_color,
    @DrawableRes val countryStepTextColor: Int = R.color.dark_text_color,
    @DrawableRes val countryStepBackground: Int = R.drawable.gray_border_background,
    @DrawableRes val identityStepIcon: Int = R.drawable.ic_2,
    @DrawableRes val identityStepIconColor: Int = R.color.dark_text_color,
    @DrawableRes val identityStepTextColor: Int = R.color.dark_text_color,
    @DrawableRes val identityStepBackground: Int = R.drawable.gray_border_background,
    @DrawableRes val documentStepIcon: Int = R.drawable.ic_3,
    @DrawableRes val documentStepIconColor: Int = R.color.dark_text_color,
    @DrawableRes val documentStepTextColor: Int = R.color.dark_text_color,
    @DrawableRes val documentStepBackground: Int = R.drawable.gray_border_background,
    val currentStep: VerificationStep = VerificationStep.COUNTRY_VERIFICATION_STEP
)

data class VerificationIdentityState(val recordStatus: RecordStatus)

data class VerificationFieldsState(val currentStep: Int = 1)


sealed class VerificationDetailsAction {
    data class NavigateAction(val navDirections: NavDirections) : VerificationDetailsAction()
}