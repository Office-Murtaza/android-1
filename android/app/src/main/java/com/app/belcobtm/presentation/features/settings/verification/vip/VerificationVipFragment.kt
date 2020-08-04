package com.app.belcobtm.presentation.features.settings.verification.vip

import android.Manifest
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import kotlinx.android.synthetic.main.activity_verification_vip.*
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class VerificationVipFragment : BaseFragment(), BottomSheetImagePicker.OnImagesSelectedListener {
    private val viewModel: VerificationVipViewModel by viewModel()
    override val resourceLayout = R.layout.activity_verification_vip
    override val isHomeButtonEnabled = true

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.fileUri = uris.first()
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(viewModel.fileUri)
        removeImageButtonView.show()
        selectImageButtonView.hide()
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

    override fun initViews() {
        setToolbarTitle(R.string.vip_verify_label)
    }

    override fun initListeners() {
        selectImageButtonView.setOnClickListener {
            showFilePickerWithPermissionCheck()
        }
        imageContainer.setOnClickListener {
            showFilePickerWithPermissionCheck()
        }
        verifyButtonView.setOnClickListener { sendVip() }
        snnView.actionDoneListener { sendVip() }

        removeImageButtonView.setOnClickListener {
            viewModel.fileUri = null
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
            removeImageButtonView.hide()
            selectImageButtonView.show()
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
                        is Failure.MessageError -> showError(loadingData.errorType.message?: getString(R.string.error_something_went_wrong))
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
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