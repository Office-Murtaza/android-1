package com.belcobtm.presentation.features.settings.verification.details

import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.R
import com.belcobtm.domain.settings.interactor.GetVerificationCountryListUseCase
import com.belcobtm.domain.settings.interactor.GetVerificationDetailsUseCase
import com.belcobtm.domain.settings.interactor.GetVerificationFieldsUseCase
import com.belcobtm.domain.settings.item.VerificationDetailsDataItem
import com.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.belcobtm.domain.settings.item.VerificationSupportedCountryDataItem
import com.belcobtm.domain.settings.type.VerificationStatus
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.mvvm.LoadingData

class VerificationDetailsViewModel(
    private val getVerificationDetailsUseCase: GetVerificationDetailsUseCase,
    private val getVerificationFieldsUseCase: GetVerificationFieldsUseCase,
    private val countriesUseCase: GetVerificationCountryListUseCase,
    private val priceFormatter: Formatter<Double>,
) : ViewModel() {
    val detailsStateData = MutableLiveData<LoadingData<VerificationDetailsState>>()
    val fieldsStateData = MutableLiveData<LoadingData<VerificationFieldsState>>()
    val actionData = SingleLiveData<VerificationDetailsAction>()
    val countries = countriesUseCase.invoke()
    var currentStep = 1
    var selectedCountry: VerificationSupportedCountryDataItem? = null

    var item: VerificationDetailsDataItem? = null

    fun getVerificationStatus() {
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

    fun getVerificationFields() {
        fieldsStateData.value = LoadingData.Loading()
        selectedCountry?.let {
            getVerificationFieldsUseCase.invoke(GetVerificationFieldsUseCase.Params(it.code),
                onSuccess = {

                },
                onError = {
                    fieldsStateData.value = LoadingData.Error(it)
                }
            )
        } ?: run {

        }
    }

    fun onBackClick() {
        if (currentStep > 1) {
            currentStep--
            detailsStateData.value = LoadingData.Success(
                getVerificationDetailsStateByStep(currentStep)
            )
        }
    }

    fun onNextClick() {
        if (currentStep < 3) {
            currentStep++
            detailsStateData.value = LoadingData.Success(
                getVerificationDetailsStateByStep(currentStep)

            )
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
    }


    private fun isButtonEnabled(verificationInfoDataItem: VerificationInfoDataItem): Boolean {
        return when (verificationInfoDataItem.status) {
            VerificationStatus.NOT_VERIFIED,
            VerificationStatus.VERIFICATION_REJECTED,
            VerificationStatus.VERIFIED,
            VerificationStatus.VIP_VERIFICATION_REJECTED -> true
            else -> false
        }
    }

    private fun getVerificationDetailsStateByStep(step: Int) = VerificationDetailsState(
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
        if (verificationDetails.identityVerification != null)
            currentStep = 3
        else
            currentStep = 1
    }


    private fun getCountryStepIcon(step: Int): Int {
        return when (step) {
            1 -> R.drawable.ic_1
            2 -> R.drawable.ic_checked
            3 -> R.drawable.ic_checked
            else -> R.drawable.ic_1
        }
    }

    private fun getIdentityStepIcon(step: Int): Int {
        return when (step) {
            1 -> R.drawable.ic_2
            2 -> R.drawable.ic_2
            3 -> R.drawable.ic_checked
            else -> R.drawable.ic_2
        }
    }

    private fun getDocumentStepIcon(step: Int): Int {
        return when (step) {
            1 -> R.drawable.ic_3
            2 -> R.drawable.ic_3
            3 -> R.drawable.ic_3
            else -> R.drawable.ic_3
        }
    }

    private fun getCountryStepIconColor(step: Int): Int {
        return when (step) {
            1 -> R.color.colorWhite
            2 -> R.color.colorWhite
            3 -> R.color.colorWhite
            else -> R.color.colorWhite
        }
    }

    private fun getIdentityStepIconColor(step: Int): Int {
        return when (step) {
            1 -> R.color.gray_text_color
            2 -> R.color.colorWhite
            3 -> R.color.colorWhite
            else -> R.color.colorWhite
        }
    }

    private fun getDocumentStepIconColor(step: Int): Int {
        return when (step) {
            1 -> R.color.gray_text_color
            2 -> R.color.gray_text_color
            3 -> R.color.colorWhite
            else -> R.color.colorWhite
        }
    }

    private fun getCountryStepTextColor(step: Int): Int {
        return when (step) {
            1 -> R.color.black_text_color
            2 -> R.color.gray_text_color
            3 -> R.color.gray_text_color
            else -> R.color.gray_text_color
        }
    }

    private fun getIdentityStepTextColor(step: Int): Int {
        return when (step) {
            1 -> R.color.gray_text_color
            2 -> R.color.black_text_color
            3 -> R.color.gray_text_color
            else -> R.color.gray_text_color
        }
    }

    private fun getDocumentStepTextColor(step: Int): Int {
        return when (step) {
            1 -> R.color.gray_text_color
            2 -> R.color.gray_text_color
            3 -> R.color.black_text_color
            else -> R.color.gray_text_color
        }
    }

    private fun getCountryStepBackground(step: Int): Int {
        return when (step) {
            1 -> R.drawable.circular_blue_background
            2 -> R.drawable.circular_blue_background
            3 -> R.drawable.circular_blue_background
            else -> R.drawable.circular_blue_background
        }
    }

    private fun getIdentityStepBackground(step: Int): Int {
        return when (step) {
            1 -> R.drawable.gray_border_background
            2 -> R.drawable.circular_blue_background
            3 -> R.drawable.circular_blue_background
            else -> R.drawable.circular_blue_background
        }
    }

    private fun getDocumentStepBackground(step: Int): Int {
        return when (step) {
            1 -> R.drawable.gray_border_background
            2 -> R.drawable.gray_border_background
            3 -> R.drawable.circular_blue_background
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
    val currentStep: Int = 1
)

data class VerificationFieldsState(val currentStep: Int = 1)


sealed class VerificationDetailsAction {
    data class NavigateAction(val navDirections: NavDirections) : VerificationDetailsAction()
}