package com.belcobtm.presentation.features.settings.verification.vip

import android.Manifest
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import com.belcobtm.BuildConfig
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationVipBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.extensions.getString
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.extensions.show
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import org.koin.androidx.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class VerificationVipFragment :
    BaseFragment<FragmentVerificationVipBinding>(),
    BottomSheetImagePicker.OnImagesSelectedListener {

    private val viewModel: VerificationVipViewModel by viewModel()
    private val args by navArgs<VerificationVipFragmentArgs>()
    override val isBackButtonEnabled = true
    override var isMenuEnabled = true
    private var validated = false

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.fileUri = uris.first()
        binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.imageView.setImageURI(viewModel.fileUri)
        binding.validatePhoto()
        binding.imageWrapper.show()
        binding.imagePlaceholder.hide()
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

    override fun FragmentVerificationVipBinding.initViews() {
        setToolbarTitle(R.string.vip_verify_label)
    }

    override fun FragmentVerificationVipBinding.initListeners() {
        selectImageButtonView.setOnClickListener {
            showFilePickerWithPermissionCheck()
        }
        imagePlaceholder.setOnClickListener {
            showFilePickerWithPermissionCheck()
        }
        verifyButtonView.setOnClickListener { sendVip() }

        removeImageButtonView.setOnClickListener {
            viewModel.fileUri = null
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageDrawable(null)
            imagePlaceholder.show()
            imageWrapper.hide()
        }

        snnView.editText?.addTextChangedListener {
            if (validated) {
                validateSnn()
            }
        }

    }

    override fun FragmentVerificationVipBinding.initObservers() {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun sendVip() {
        validated = true
        if (isValidFields()) {
            viewModel.fileUri?.let {
                viewModel.sendVip(
                    it,
                    binding.snnView.getString(),
                    args.info
                )
            }
        }
    }

    private fun isValidFields(): Boolean {
        val photo = binding.validatePhoto()
        val snn = binding.validateSnn()
        return photo && snn
    }

    private fun FragmentVerificationVipBinding.validatePhoto(): Boolean {
        return if (viewModel.fileUri == null) {
            photoErrorView.toggle(true)
            false
        } else {
            photoErrorView.toggle(false)
            true
        }
    }

    private fun FragmentVerificationVipBinding.validateSnn(): Boolean {
        return if (snnView.getString().length == 9 && snnView.getString().toInt() >= 100000000) {
            snnView.isErrorEnabled = false
            true
        } else {
            snnView.isErrorEnabled = true
            snnView.error = getString(R.string.ssn_validation_text)
            false
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVerificationVipBinding =
        FragmentVerificationVipBinding.inflate(inflater, container, false)
}