package com.belcobtm.presentation.features.settings.verification.details.document

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.belcobtm.BuildConfig
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationDocumentPageBinding
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.extensions.show
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class VerificationDocumentPageFragment : Fragment(),
    BottomSheetImagePicker.OnImagesSelectedListener {
    val viewModel by sharedViewModel<VerificationDetailsViewModel>()
    lateinit var binding: FragmentVerificationDocumentPageBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerificationDocumentPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews() {

    }

    private fun initListeners() {
        with(binding) {
            frontScanButtonView.setOnClickListener {
                showFilePickerWithPermissionCheck()
            }
            frontScanPlaceholder.setOnClickListener {
                showFilePickerWithPermissionCheck()
            }

            removeFrontScanButtonView.setOnClickListener {
                viewModel.fileUri = null
                frontScanImageView.setImageDrawable(null)
                frontScanPlaceholder.show()
                frontScanWrapper.hide()
            }
            submitButton.setOnClickListener {
                viewModel.onDocumentVerificationSubmit()
            }

        }


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

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.fileUri = uris.first()
        binding.frontScanImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.frontScanImageView.setImageURI(viewModel.fileUri)
        validatePhoto()
        binding.frontScanWrapper.show()
        binding.frontScanPlaceholder.hide()
    }
    private fun validatePhoto(): Boolean =
        if (viewModel.fileUri == null) {
            binding.frontScanErrorView.toggle(true)
            false
        } else {
            binding.frontScanErrorView.toggle(false)
            true
        }

}