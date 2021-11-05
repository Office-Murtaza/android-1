package com.belcobtm.presentation.features.settings.verification.blank

import android.Manifest
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import com.belcobtm.BuildConfig
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationBlankBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.item.VerificationCountryDataItem
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import org.koin.androidx.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class VerificationBlankFragment :
    BaseFragment<FragmentVerificationBlankBinding>(),
    BottomSheetImagePicker.OnImagesSelectedListener {
    private val viewModel: VerificationBlankViewModel by viewModel()
    private val args by navArgs<VerificationBlankFragmentArgs>()

    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true

    private var validated = false

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.fileUri = uris.first()
        binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.imageView.setImageURI(viewModel.fileUri)
        validatePhoto()
        binding.imageWrapper.show()
        binding.imagePlaceholder.hide()
    }

    override fun FragmentVerificationBlankBinding.initViews() {
        setToolbarTitle(R.string.verify_label)
        viewModel.countries.firstOrNull()?.let(::onCountrySelected)
    }

    @NeedsPermission(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    fun showFilePicker() {
        BottomSheetImagePicker.Builder("${BuildConfig.APPLICATION_ID}.fileprovider")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun FragmentVerificationBlankBinding.initListeners() {
        selectImageButtonView.setOnClickListener {
            showFilePickerWithPermissionCheck()
        }
        imagePlaceholder.setOnClickListener {
            showFilePickerWithPermissionCheck()
        }
        verifyButtonView.setOnClickListener { sendBlank() }

        removeImageButtonView.setOnClickListener {
            viewModel.fileUri = null
            imageView.setImageDrawable(null)
            imagePlaceholder.show()
            imageWrapper.hide()
        }

        countryView.editText?.keyListener = null
        countryView.editText?.setOnClickListener {
            val countryList = viewModel.countries
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.verification_alert_country_title)
                .setItems(countryList.map { it.name }.toTypedArray()) { dialog, which ->
                    onCountrySelected(countryList[which])
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
                            checkProvince()
                        }
                        .create()
                        .show()
                } ?: checkCountry()
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
                } ?: checkProvince()
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

    private fun onCountrySelected(country: VerificationCountryDataItem) {
        binding.countryView.setText(country.name)
        binding.provinceView.clearText()
        binding.cityView.clearText()
        checkCountry()
    }

    override fun FragmentVerificationBlankBinding.initObservers() {
        viewModel.uploadingLiveData.observe(viewLifecycleOwner) { loadingData ->
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
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVerificationBlankBinding =
        FragmentVerificationBlankBinding.inflate(inflater, container, false)

    private fun checkCountry() {
        binding.validateCountry()
    }

    private fun checkProvince() {
        binding.validateProvince()
    }

    private fun sendBlank() {
        validated = true
        if (isValidFields()) {
            viewModel.fileUri?.let { imageUri ->
                viewModel.sendBlank(
                    imageUri,
                    binding.idNumberView.getString(),
                    binding.firstNameView.getString(),
                    binding.lastNameView.getString(),
                    binding.addressView.getString(),
                    binding.cityView.getString(),
                    binding.countryView.getString(),
                    binding.provinceView.getString(),
                    binding.zipCodeView.getString(),
                    args.info
                )
            }
        }
    }

    private fun isValidFields(): Boolean {
        val photo = validatePhoto()
        val idNumber = binding.validateIdNumber()
        val firstName = binding.validateFirstName()
        val lastName = binding.validateLastName()
        val address = binding.validateAddress()
        val city = binding.validateCity()
        val country = binding.validateCountry()
        val province = binding.validateProvince()
        val zip = binding.validateZipCode()
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

    private fun validatePhoto(): Boolean =
        if (viewModel.fileUri == null) {
            binding.photoErrorView.toggle(true)
            false
        } else {
            binding.photoErrorView.toggle(false)
            true
        }

    private fun FragmentVerificationBlankBinding.validateIdNumber(): Boolean {
        return if (idNumberView.getString().length > 9 || idNumberView.getString().isEmpty()) {
            idNumberView.isErrorEnabled = true
            idNumberView.error = getString(R.string.id_number_validation_text)
            false
        } else {
            idNumberView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationBlankBinding.validateFirstName(): Boolean {
        return if (firstNameView.getString().length > 255 || firstNameView.getString().isEmpty()) {
            firstNameView.isErrorEnabled = true
            firstNameView.error = getString(R.string.first_name_validation_text)
            false
        } else {
            firstNameView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationBlankBinding.validateLastName(): Boolean {
        return if (lastNameView.getString().length > 255 || lastNameView.getString().isEmpty()) {
            lastNameView.isErrorEnabled = true
            lastNameView.error = getString(R.string.last_name_validation_text)
            false
        } else {
            lastNameView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationBlankBinding.validateAddress(): Boolean {
        return if (addressView.getString().length > 255 || addressView.getString().isEmpty()) {
            addressView.isErrorEnabled = true
            addressView.error = getString(R.string.address_validation_text)
            false
        } else {
            addressView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationBlankBinding.validateCity(): Boolean {
        return if (cityView.getString().isEmpty()) {
            cityView.isErrorEnabled = true
            cityView.error = getString(R.string.city_validation_text)
            false
        } else {
            cityView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationBlankBinding.validateCountry(): Boolean {
        return if (countryView.getString().isEmpty()) {
            countryView.isErrorEnabled = true
            countryView.error = getString(R.string.country_validation_text)
            false
        } else {
            countryView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationBlankBinding.validateProvince(): Boolean {
        return if (provinceView.getString().isEmpty()) {
            provinceView.isErrorEnabled = true
            provinceView.error = getString(R.string.province_validation_text)
            false
        } else {
            provinceView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationBlankBinding.validateZipCode(): Boolean {
        return if (zipCodeView.getString().length == 5 && zipCodeView.getString()
                .toInt() >= 10000
        ) {
            zipCodeView.isErrorEnabled = false
            true
        } else {
            zipCodeView.isErrorEnabled = true
            zipCodeView.error = getString(R.string.zip_validation_text)
            false
        }
    }
}