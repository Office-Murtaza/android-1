package com.app.belcobtm.presentation.features.settings.verification.blank

import android.Manifest
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import kotlinx.android.synthetic.main.fragment_verification_blank.*
import kotlinx.android.synthetic.main.fragment_verification_blank.imageContainer
import kotlinx.android.synthetic.main.fragment_verification_blank.imageView
import kotlinx.android.synthetic.main.fragment_verification_blank.removeImageButtonView
import kotlinx.android.synthetic.main.fragment_verification_blank.selectImageButtonView
import kotlinx.android.synthetic.main.fragment_verification_blank.verifyButtonView
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class VerificationBlankFragment : BaseFragment(), BottomSheetImagePicker.OnImagesSelectedListener {
    private val viewModel: VerificationBlankViewModel by viewModel()

    override val resourceLayout = R.layout.fragment_verification_blank
    override val isHomeButtonEnabled = true

    private var validated = false

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.fileUri = uris.first()
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(viewModel.fileUri)
        validatePhoto()
        removeImageButtonView.show()
        selectImageButtonView.hide()
    }

    override fun initViews() {
        setToolbarTitle(R.string.verify_label)
    }

    @NeedsPermission(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    fun showFilePicker() {
        BottomSheetImagePicker.Builder(getString(R.string.file_provider))
            .cameraButton(ButtonType.Tile)
            .galleryButton(ButtonType.Tile)
            .singleSelectTitle(R.string.verification_select_photo_please)
            .columnSize(R.dimen.photo_picker_column_size)
            .requestTag("single")
            .show(childFragmentManager)
    }

    @OnNeverAskAgain(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    fun permissionsNeverAskAgain() {
        AlertHelper.showToastShort(requireContext(), R.string.verification_please_on_permissions)
    }

    override fun initListeners() {
        selectImageButtonView.setOnClickListener {
            showFilePickerWithPermissionCheck()
        }
        imageContainer.setOnClickListener {
            showFilePickerWithPermissionCheck()
        }
        verifyButtonView.setOnClickListener { sendBlank() }
        addressView.actionDoneListener { sendBlank() }

        removeImageButtonView.setOnClickListener {
            viewModel.fileUri = null
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_add
                )
            )

            selectImageButtonView.show()
            removeImageButtonView.hide()
        }

        countryView.editText?.keyListener = null
        countryView.editText?.setOnClickListener {
            val countryList = viewModel.countries
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.verification_alert_country_title)
                .setItems(countryList.map { it.name }.toTypedArray()) { dialog, which ->
                    countryView.setText(countryList[which].name)
                    provinceView.clearText()
                    cityView.clearText()
                }
                .create().show()
        }

        provinceView.editText?.keyListener = null
        provinceView.editText?.setOnClickListener {
            viewModel.countries
                .find { it.name == countryView.getString() }
                ?.states?.let { stateList ->
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.verification_alert_state_title)
                        .setItems(stateList.map { it.name }.toTypedArray()) { _, which ->
                            provinceView.setText(stateList[which].name)
                            cityView.clearText()
                        }
                        .create()
                        .show()
                } ?: AlertHelper.showToastShort(
                requireContext(),
                R.string.verification_alert_country_title
            )
        }

        cityView.editText?.keyListener = null
        cityView.editText?.setOnClickListener {
            viewModel.countries
                .find { it.name == countryView.getString() }
                ?.states
                ?.find { it.name == provinceView.getString() }
                ?.cities?.let { cities ->
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.verification_alert_city_title)
                        .setItems(cities.toTypedArray()) { _, which -> cityView.setText(cities[which]) }
                        .create()
                        .show()
                } ?: AlertHelper.showToastShort(
                requireContext(),
                R.string.verification_alert_state_title
            )
        }

        idNumberView.editText?.addTextChangedListener {
            if (validated) {
                validateIdNumber()
            }
        }
        firstNameView.editText?.addTextChangedListener {
            if (validated) {
                validateFirstName()
            }
        }
        lastNameView.editText?.addTextChangedListener {
            if (validated) {
                validateLastName()
            }
        }
        addressView.editText?.addTextChangedListener {
            if (validated) {
                validateAddress()
            }
        }
        countryView.editText?.addTextChangedListener {
            if (validated) {
                validateCountry()
            }
        }
        cityView.editText?.addTextChangedListener {
            if (validated) {
                validateCity()
            }
        }
        provinceView.editText?.addTextChangedListener {
            if (validated) {
                validateProvince()
            }
        }
        zipCodeView.editText?.addTextChangedListener {
            if (validated) {
                validateZipCode()
            }
        }
    }

    override fun initObservers() {
        viewModel.uploadingLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> showLoading()
                is LoadingData.Success -> {
                    popBackStack()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.MessageError -> showError(
                            loadingData.errorType.message
                                ?: getString(R.string.error_something_went_wrong)
                        )
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                }
            }
        })
    }

    private fun sendBlank() {
        validated = true
        if (isValidFields()) {
            viewModel.fileUri?.let { imageUri ->
                viewModel.sendBlank(
                    imageUri,
                    idNumberView.getString(),
                    firstNameView.getString(),
                    lastNameView.getString(),
                    addressView.getString(),
                    cityView.getString(),
                    countryView.getString(),
                    provinceView.getString(),
                    zipCodeView.getString()
                )
            }
        }
    }

    private fun isValidFields(): Boolean {
        val photo = validatePhoto()
        val idNumber = validateIdNumber()
        val firstName = validateFirstName()
        val lastName = validateLastName()
        val address = validateAddress()
        val city = validateCity()
        val country = validateCountry()
        val province = validateProvince()
        val zip = validateZipCode()
        return photo
                && idNumber
                && firstName
                && lastName
                && address
                && city
                && country
                && province
                && zip
    }

    private fun validatePhoto(): Boolean {
        if (viewModel.fileUri == null) {
            photoErrorView.toggle(true)
            return false
        } else {
            photoErrorView.toggle(false)
            return true
        }
    }

    private fun validateIdNumber(): Boolean {
        return if (idNumberView.getString().length > 9) {
            idNumberView.isErrorEnabled = true
            idNumberView.error = getString(R.string.id_number_validation_text)
            false
        } else {
            idNumberView.isErrorEnabled = false
            true
        }
    }

    private fun validateFirstName(): Boolean {
        return if (firstNameView.getString().length > 255 || firstNameView.getString().isEmpty()) {
            firstNameView.isErrorEnabled = true
            firstNameView.error = getString(R.string.first_name_validation_text)
            false
        } else {
            firstNameView.isErrorEnabled = false
            true
        }
    }

    private fun validateLastName(): Boolean {
        return if (lastNameView.getString().length > 255 || lastNameView.getString().isEmpty()) {
            lastNameView.isErrorEnabled = true
            lastNameView.error = getString(R.string.last_name_validation_text)
            false
        } else {
            lastNameView.isErrorEnabled = false
            true
        }
    }

    private fun validateAddress(): Boolean {
        return if (addressView.getString().length > 255 || addressView.getString().isEmpty()) {
            addressView.isErrorEnabled = true
            addressView.error = getString(R.string.address_validation_text)
            false
        } else {
            addressView.isErrorEnabled = false
            true
        }
    }

    private fun validateCity(): Boolean {
        return if (cityView.getString().isEmpty()) {
            cityView.isErrorEnabled = true
            cityView.error = getString(R.string.city_validation_text)
            false
        } else {
            cityView.isErrorEnabled = false
            true
        }
    }

    private fun validateCountry(): Boolean {
        return if (countryView.getString().isEmpty()) {
            countryView.isErrorEnabled = true
            countryView.error = getString(R.string.country_validation_text)
            false
        } else {
            countryView.isErrorEnabled = false
            true
        }
    }

    private fun validateProvince(): Boolean {
        return if (provinceView.getString().isEmpty()) {
            provinceView.isErrorEnabled = true
            provinceView.error = getString(R.string.province_validation_text)
            false
        } else {
            provinceView.isErrorEnabled = false
            true
        }
    }

    private fun validateZipCode(): Boolean {
        return if (zipCodeView.getString().length == 5 && zipCodeView.getString().toInt() >= 10000) {
            zipCodeView.isErrorEnabled = false
            true
        } else {
            zipCodeView.isErrorEnabled = true
            zipCodeView.error = getString(R.string.zip_validation_text)
            false
        }
    }
}