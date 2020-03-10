package com.app.belcobtm.presentation.features.settings.verification.vip

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import kotlinx.android.synthetic.main.activity_verification_vip.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import kotlin.text.isNotBlank

@RuntimePermissions
class VerificationVipActivity : BaseActivity(), BottomSheetImagePicker.OnImagesSelectedListener {
    private val viewModel: VerificationVipViewModel by viewModel()

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

    private fun initViews() {
        setContentView(R.layout.activity_verification_vip)
        setSupportActionBar(toolbarView)
        supportActionBar?.let { toolbar ->
            toolbar.setDisplayHomeAsUpEnabled(true)
            toolbar.setDisplayShowHomeEnabled(true)
            toolbar.setTitle(R.string.verification_screen_title)
        }
    }

    private fun initListeners() {
        selectImageButtonView.setOnClickListener { showFilePickerWithPermissionCheck() }
        verifyButtonView.setOnClickListener { sendVip() }
        snnView.actionDoneListener { sendVip() }

        removeImageButtonView.setOnClickListener {
            viewModel.fileUri = null
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add))
            removeImageButtonView.hide()
        }

    }

    private fun initObservers() {
        viewModel.uploadingLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    finish()
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
    }

    private fun sendVip() {
        if (isValidFields()) {
            viewModel.fileUri?.let { viewModel.sendBlank(it, snnView.getString()) }
        } else {
            showError(R.string.verification_alert_please_fill_fields)
        }
    }

    private fun isValidFields(): Boolean = viewModel.fileUri != null && snnView.isNotBlank()
}