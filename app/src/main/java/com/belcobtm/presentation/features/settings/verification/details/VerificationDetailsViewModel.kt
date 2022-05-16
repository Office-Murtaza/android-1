package com.belcobtm.presentation.features.settings.verification.details

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.R
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.helper.ImageHelper
import com.belcobtm.domain.settings.interactor.*
import com.belcobtm.domain.settings.item.*
import com.belcobtm.domain.settings.type.DocumentType
import com.belcobtm.domain.settings.type.RecordStatus
import com.belcobtm.domain.settings.type.VerificationStatus
import com.belcobtm.domain.settings.type.VerificationStep
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData


class VerificationDetailsViewModel(
    private val sendVerificationDocumentUseCase: SendVerificationDocumentUseCase,
    private val sendVerificationIdentityUseCase: SendVerificationIdentityUseCase,
    private val getVerificationDetailsUseCase: GetVerificationDetailsUseCase,
    private val getVerificationFieldsUseCase: GetVerificationFieldsUseCase,
    private val countriesUseCase: GetVerificationCountryListUseCase,
    private val prefHelper: SharedPreferencesHelper,
) : ViewModel() {
    val detailsStateData = MutableLiveData<LoadingData<VerificationDetailsState>>()
    val identityStateData = MutableLiveData<LoadingData<VerificationIdentityState>>()
    val documentStateData = MutableLiveData<LoadingData<VerificationDocumentState>>()
    val actionData = SingleLiveData<VerificationDetailsAction>()
    var currentStep = VerificationStep.COUNTRY_VERIFICATION_STEP
    var verificationStatus = VerificationStatus.UNVERIFIED
    var selectedCountry: VerificationSupportedCountryDataItem? = null
    var selectedDocumentType: DocumentType? = null
    var frontScanDocument: Bitmap? = null
    var backScanDocument: Bitmap? = null
    var selfieScan: Bitmap? = null
    var verificationDetails: VerificationDetailsDataItem? = null
    val imageHelper = ImageHelper()
    val countries = countriesUseCase.invoke()


    fun fetchVerificationStatus() {

        verificationStatus = VerificationStatus.fromString(prefHelper.userStatus)

        if (verificationStatus != VerificationStatus.VERIFIED) {
            detailsStateData.value = LoadingData.Loading()
            getVerificationDetailsUseCase.invoke(Unit,
                onSuccess = {
                    verificationDetails = it
                    getVerificationStatus(it)
                    it.identityVerification?.let { identityResponse ->
                        identityStateData.value = LoadingData.Success<VerificationIdentityState>(
                            createVerificationIdentityState(identityResponse)
                        )
                    }
                    it.documentVerification?.let { documentResponse ->
                        documentStateData.value = LoadingData.Success<VerificationDocumentState>(
                            createVerificationDocumentState(
                                documentResponse,
                            )
                        )
                    }
                    selectedCountry = it.selectedCountry
                    detailsStateData.value = LoadingData.Success(
                        getVerificationDetailsState()
                    )
                },
                onError = {
                    detailsStateData.value = LoadingData.Error(it)
                }
            )
        } else {
            detailsStateData.value = LoadingData.Success(
                getVerificationDetailsState()
            )
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
                getVerificationDetailsState()
            )
        }
    }

    fun onCountryVerificationNext() {
        if (selectedCountry != null) {
            currentStep = VerificationStep.IDENTITY_VERIFICATION_STEP
            detailsStateData.value = LoadingData.Success(
                getVerificationDetailsState()
            )
        }
    }

    fun onIdentityVerificationNext(dataItem: VerificationIdentityDataItem) {
        identityStateData.value = LoadingData.Loading()
        sendVerificationIdentityUseCase.invoke(SendVerificationIdentityUseCase.Params(dataItem),
            onSuccess = {
                identityStateData.value = LoadingData.Success<VerificationIdentityState>(
                    createVerificationIdentityState(it)
                )
                if (it.recordStatus == RecordStatus.MATCH) {
                    currentStep = VerificationStep.DOCUMENT_VERIFICATION_STEP
                    detailsStateData.value = LoadingData.Success(
                        getVerificationDetailsState()
                    )
                }
            },
            onError = {
                identityStateData.value = LoadingData.Error(it)
            }
        )


    }

    fun onDocumentVerificationSubmit() {
        documentStateData.value = LoadingData.Loading()
        if (frontScanDocument != null && selfieScan != null && selectedDocumentType != null && selectedCountry != null) {

            var backScanBase64: String? = null
            backScanDocument?.let {
                backScanBase64 = imageHelper.convert(it).replace("\n", "")
            }

            sendVerificationDocumentUseCase.invoke(SendVerificationDocumentUseCase.Params(
                VerificationDocumentDataItem(
                    frontScanBitmap = frontScanDocument!!,
                    backScanBitmap = backScanDocument,
                    selfieBitmap = selfieScan!!,
                    frontScanBase64 = imageHelper.convert(frontScanDocument!!).replace("\n", ""),
                    backScanBase64 = backScanBase64,
                    selfieBase64 = imageHelper.convert(selfieScan!!).replace("\n", ""),
                    documentType = selectedDocumentType!!,
                    countryDataItem = selectedCountry!!
                )
            ),
                onSuccess = {
                    documentStateData.value = LoadingData.Success<VerificationDocumentState>(
                        createVerificationDocumentState(it)
                    )
                    if (it.recordStatus == RecordStatus.MATCH) {
                        verificationStatus = VerificationStatus.VERIFIED
                    } else if (it.transactionId == null) {
                        verificationStatus = VerificationStatus.VERIFICATION_PENDING
                    }

                    detailsStateData.value = LoadingData.Success(
                        getVerificationDetailsState()
                    )
                },
                onError = {
                    documentStateData.value = LoadingData.Error(it)
                }
            )
        }
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


    private fun getVerificationStatus(verificationDetails: VerificationDetailsDataItem) {
        if (verificationDetails.selectedCountry == null)
            currentStep = VerificationStep.COUNTRY_VERIFICATION_STEP
        else {
            selectedCountry = verificationDetails.selectedCountry
            if (verificationDetails.identityVerification != null && verificationDetails.identityVerification.recordStatus == RecordStatus.MATCH)
                currentStep = VerificationStep.DOCUMENT_VERIFICATION_STEP
            else
                currentStep = VerificationStep.IDENTITY_VERIFICATION_STEP
        }
        verificationStatus = VerificationStatus.UNVERIFIED
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

    private fun getVerificationDetailsState(
    ) = VerificationDetailsState(
        countryStepTextColor = getCountryStepTextColor(currentStep),
        countryStepBackground = getCountryStepBackground(currentStep),
        countryStepIcon = getCountryStepIcon(currentStep),
        countryStepIconColor = getCountryStepIconColor(currentStep),
        identityStepBackground = getIdentityStepBackground(currentStep),
        identityStepTextColor = getIdentityStepTextColor(currentStep),
        identityStepIcon = getIdentityStepIcon(currentStep),
        identityStepIconColor = getIdentityStepIconColor(currentStep),
        documentStepBackground = getDocumentStepBackground(currentStep),
        documentStepIcon = getDocumentStepIcon(currentStep),
        documentStepIconColor = getDocumentStepIconColor(currentStep),
        documentStepTextColor = getDocumentStepTextColor(currentStep),
        currentStep = currentStep,
        verificationStatus = verificationStatus
    )
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
    val currentStep: VerificationStep = VerificationStep.COUNTRY_VERIFICATION_STEP,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_VERIFIED
)


data class VerificationIdentityState(
    val recordStatus: RecordStatus,
    val firstNameValue: String,
    val lastNameValue: String,
    val dayOfBirthValue: Int,
    val monthOfBirthValue: Int,
    val yearOfBirthValue: Int,
    val provinceValue: String,
    val cityValue: String,
    val streetNameValue: String,
    val buildingNumberValue: String,
    val zipCodeValue: String,
    val ssnValue: String,
    val occupation: String,
    val sourceOfFunds: String,
    val firstNameValidationError: Boolean,
    val lastNameValidationError: Boolean,
    val birthDateValidationError: Boolean,
    val provinceValidationError: Boolean,
    val cityValidationError: Boolean,
    val streetNameValidationError: Boolean,
    val buildingNumberValidationError: Boolean,
    val zipCodeValidationError: Boolean,
    val ssnValidationError: Boolean,
)

private fun createVerificationIdentityState(dataItem: VerificationIdentityResponseDataItem): VerificationIdentityState =
    VerificationIdentityState(
        recordStatus = dataItem.recordStatus,
        firstNameValue = dataItem.firstNameValue,
        lastNameValue = dataItem.lastNameValue,
        dayOfBirthValue = dataItem.dayOfBirthValue,
        monthOfBirthValue = dataItem.monthOfBirthValue,
        yearOfBirthValue = dataItem.yearOfBirthValue,
        provinceValue = dataItem.provinceValue,
        cityValue = dataItem.cityValue,
        streetNameValue = dataItem.streetNameValue,
        buildingNumberValue = dataItem.buildingNumberValue,
        zipCodeValue = dataItem.zipCodeValue,
        ssnValue = dataItem.ssnValue,
        occupation = dataItem.occupation,
        sourceOfFunds = dataItem.sourceOfFunds,
        firstNameValidationError = dataItem.firstNameValidationError,
        lastNameValidationError = dataItem.lastNameValidationError,
        birthDateValidationError = dataItem.birthDateValidationError,
        provinceValidationError = dataItem.provinceValidationError,
        cityValidationError = dataItem.cityValidationError,
        streetNameValidationError = dataItem.streetNameValidationError,
        buildingNumberValidationError = dataItem.buildingNumberValidationError,
        zipCodeValidationError = dataItem.zipCodeValidationError,
        ssnValidationError = dataItem.ssnValidationError
    )

data class VerificationDocumentState(
    val selectedDocumentType: DocumentType?,
    val frontImageBitmap: Bitmap?,
    val backImageBitmap: Bitmap?,
    val selfieImageBitmap: Bitmap?,
    val frontImageValidationError: Boolean,
    val backImageValidationError: Boolean,
    val selfieImageValidationError: Boolean,
)

private fun createVerificationDocumentState(
    dataItem: VerificationDocumentResponseDataItem,
): VerificationDocumentState =
    VerificationDocumentState(
        selectedDocumentType = dataItem.documentType,
        frontImageBitmap = dataItem.frontImageBitmap,
        backImageBitmap = dataItem.backImageBitmap,
        selfieImageBitmap = dataItem.selfieImageBitmap,
        frontImageValidationError = dataItem.frontImageValidationError,
        backImageValidationError = dataItem.backImageValidationError,
        selfieImageValidationError = dataItem.selfieImageValidationError,
    )


sealed class VerificationDetailsAction {
    data class NavigateAction(val navDirections: NavDirections) : VerificationDetailsAction()
}