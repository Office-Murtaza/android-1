package com.app.belcobtm.presentation.features.settings.verification.blank

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import kotlinx.android.synthetic.main.fragment_verification_blank.*
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.*

@RuntimePermissions
class VerificationBlankActivity : BaseActivity(), BottomSheetImagePicker.OnImagesSelectedListener {
    private val viewModel: VerificationBlankViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.fileUri = uris.first()
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(viewModel.fileUri)
        removeImageButtonView.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
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
            .show(supportFragmentManager)
    }

    @OnNeverAskAgain(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    fun permissionsNeverAskAgain() {
        AlertHelper.showToastShort(this, R.string.verification_please_on_permissions)
    }

    private fun initViews() {
        setContentView(R.layout.fragment_verification_blank)
//        setSupportActionBar(toolbarView)
        supportActionBar?.let { toolbar ->
            toolbar.setDisplayHomeAsUpEnabled(true)
            toolbar.setDisplayShowHomeEnabled(true)
            toolbar.setTitle(R.string.verification_screen_title)
        }
    }

    private fun initListeners() {
        selectImageButtonView.setOnClickListener { showFilePickerWithPermissionCheck() }
        verifyButtonView.setOnClickListener { sendBlank() }
        addressView.actionDoneListener { sendBlank() }

        removeImageButtonView.setOnClickListener {
            viewModel.fileUri = null
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add))
            removeImageButtonView.hide()
        }

        countryView.editText?.keyListener = null
        countryView.editText?.setOnClickListener {
            val countryList = viewModel.countries
            AlertDialog.Builder(this)
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
                AlertDialog.Builder(this)
                    .setTitle(R.string.verification_alert_state_title)
                    .setItems(stateList.map { it.name }.toTypedArray()) { _, which ->
                        provinceView.setText(stateList[which].name)
                        cityView.clearText()
                    }
                    .create()
                    .show()
            } ?: AlertHelper.showToastShort(this, R.string.verification_alert_country_title)
        }

        cityView.editText?.keyListener = null
        cityView.editText?.setOnClickListener {
            viewModel.countries
                .find { it.name == countryView.getString() }
                ?.states
                ?.find { it.name == provinceView.getString() }
                ?.cities?.let { cities ->
                AlertDialog.Builder(this)
                    .setTitle(R.string.verification_alert_city_title)
                    .setItems(cities.toTypedArray()) { _, which -> cityView.setText(cities[which]) }
                    .create()
                    .show()
            } ?: AlertHelper.showToastShort(this, R.string.verification_alert_state_title)
        }
    }

    private fun initObservers() {
        viewModel.uploadingLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
//                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    finish()
//                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
//                    progressView.hide()
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