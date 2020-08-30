package com.app.belcobtm.presentation.features.settings.verification.vip

import android.Manifest
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import kotlinx.android.synthetic.main.fragment_verification_vip.*
import kotlinx.android.synthetic.main.fragment_verification_vip.imageContainer
import kotlinx.android.synthetic.main.fragment_verification_vip.imageView
import kotlinx.android.synthetic.main.fragment_verification_vip.photoErrorView
import kotlinx.android.synthetic.main.fragment_verification_vip.removeImageButtonView
import kotlinx.android.synthetic.main.fragment_verification_vip.selectImageButtonView
import kotlinx.android.synthetic.main.fragment_verification_vip.verifyButtonView
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class VerificationVipFragment : BaseFragment(), BottomSheetImagePicker.OnImagesSelectedListener {
    private val viewModel: VerificationVipViewModel by viewModel()
    override val resourceLayout = R.layout.fragment_verification_vip
    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true

    private var validated = false

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.fileUri = uris.first()
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(viewModel.fileUri)
        validatePhoto()
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

        removeImageButtonView.setOnClickListener {
            viewModel.fileUri = null
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_add
                )
            )
            removeImageButtonView.hide()
            selectImageButtonView.show()
        }

        snnView.editText?.addTextChangedListener {
            if (validated) {
                validateSnn()
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

    private fun sendVip() {
        validated = true
        if (isValidFields()) {
            viewModel.fileUri?.let { viewModel.sendBlank(it, snnView.getString()) }
        }
    }

    private fun isValidFields(): Boolean {
        val photo = validatePhoto()
        val snn = validateSnn()
        return photo && snn
    }

    private fun validatePhoto(): Boolean {
        return if (viewModel.fileUri == null) {
            photoErrorView.toggle(true)
            false
        } else {
            photoErrorView.toggle(false)
            true
        }
    }

    private fun validateSnn(): Boolean {
        return if (snnView.getString().length == 9 && snnView.getString().toInt() >= 100000000) {
            snnView.isErrorEnabled = false
            true
        } else {
            snnView.isErrorEnabled = true
            snnView.error = getString(R.string.ssn_validation_text)
            false
        }
    }
}