package com.app.belcobtm.presentation.features.settings.verification.blank

import android.Manifest
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import kotlinx.android.synthetic.main.activity_verification_blank.*
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class VerificationBlankFragment : BaseFragment(), BottomSheetImagePicker.OnImagesSelectedListener {
    private val viewModel: VerificationBlankViewModel by viewModel()

    override val resourceLayout = R.layout.activity_verification_blank
    override val isHomeButtonEnabled = true

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.fileUri = uris.first()
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(viewModel.fileUri)
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
        } else {
            showError(R.string.verification_alert_please_fill_fields)
        }
    }

    private fun isValidFields(): Boolean = viewModel.fileUri != null
            && idNumberView.isNotBlank()
            && firstNameView.isNotBlank()
            && lastNameView.isNotBlank()
            && addressView.isNotBlank()
            && cityView.isNotBlank()
            && countryView.isNotBlank()
            && provinceView.isNotBlank()
            && zipCodeView.isNotBlank()
}