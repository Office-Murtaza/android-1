package com.belcobtm.presentation.features.settings.verification.details.document

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.acuant.acuantcamera.camera.AcuantCameraActivity
import com.acuant.acuantcamera.camera.AcuantCameraOptions
import com.acuant.acuantcamera.constant.ACUANT_EXTRA_CAMERA_OPTIONS
import com.acuant.acuantcamera.constant.ACUANT_EXTRA_IMAGE_URL
import com.acuant.acuantcommon.exception.AcuantException
import com.acuant.acuantcommon.initializer.AcuantInitializer
import com.acuant.acuantcommon.initializer.IAcuantPackageCallback
import com.acuant.acuantcommon.model.Error
import com.acuant.acuanthgliveness.model.FaceCapturedImage
import com.acuant.acuantimagepreparation.AcuantImagePreparation
import com.acuant.acuantimagepreparation.background.EvaluateImageListener
import com.acuant.acuantimagepreparation.initializer.ImageProcessorInitializer
import com.acuant.acuantimagepreparation.model.AcuantImage
import com.acuant.acuantimagepreparation.model.CroppingData
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationDocumentPageBinding
import com.belcobtm.domain.settings.type.DocumentCaptureType
import com.belcobtm.domain.settings.type.DocumentType
import com.belcobtm.presentation.tools.extensions.getString
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.setText
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.settings.verification.acuant.FacialLivenessActivity
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import com.belcobtm.presentation.features.settings.verification.details.VerificationDocumentState
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class VerificationDocumentPageFragment : Fragment() {
    val viewModel by sharedViewModel<VerificationDetailsViewModel>()
    lateinit var binding: FragmentVerificationDocumentPageBinding
    private var validated = false
    private var firstInvalidView: View? = null

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
        initializeSDK()
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        viewModel.selectedDocumentType?.let {
            binding.documentTypeView.setText(it.shownStringValue)
        } ?: run {
            binding.documentTypeView.setText(DocumentType.DRIVING_LICENSE.shownStringValue)
            viewModel.selectedDocumentType = DocumentType.DRIVING_LICENSE
        }
        checkForMandatoryBackPhoto()
    }

    private fun checkForMandatoryBackPhoto() {
        viewModel.selectedDocumentType?.let {
            if (it.needsBackPhoto)
                toggleBackScanContainer(true)
            else
                toggleBackScanContainer(false)
        }
    }

    private fun toggleBackScanContainer(toggle: Boolean) {
        with(binding) {
            if (toggle) {
                backScanContainer.show()
                backScanButtonView.show()
            } else {
                backScanContainer.hide()
                backScanImageView.setImageDrawable(null)
                backScanWrapper.hide()
                toggleBackScanError(false)
                viewModel.backScanDocument = null
            }
        }
    }

    private fun initListeners() {
        with(binding) {
            frontScanButtonView.setOnClickListener {
                showDocumentCaptureCamera(DocumentCaptureType.FRONT_SCAN_DOCUMENT)
            }
            frontScanPlaceholder.setOnClickListener {
                showDocumentCaptureCamera(DocumentCaptureType.FRONT_SCAN_DOCUMENT)
            }

            backScanButtonView.setOnClickListener {
                showDocumentCaptureCamera(DocumentCaptureType.BACK_SCAN_DOCUMENT)
            }
            backScanPlaceholder.setOnClickListener {
                showDocumentCaptureCamera(DocumentCaptureType.BACK_SCAN_DOCUMENT)
            }

            selfieButtonView.setOnClickListener {
                showFrontCamera()
            }
            selfiePlaceholder.setOnClickListener {
                showFrontCamera()
            }

            removeFrontScanButtonView.setOnClickListener {
                viewModel.frontScanDocument = null
                frontScanImageView.setImageDrawable(null)
                frontScanWrapper.hide()
                frontScanButtonView.show()
                toggleFrontScanError(false)
            }
            removeBackScanButtonView.setOnClickListener {
                viewModel.backScanDocument = null
                backScanImageView.setImageDrawable(null)
                backScanWrapper.hide()
                backScanButtonView.show()
                toggleBackScanError(false)
            }
            removeSelfieButtonView.setOnClickListener {
                viewModel.selfieScan = null
                selfieImageView.setImageDrawable(null)
                selfieWrapper.hide()
                selfieButtonView.show()
                toggleSelfieError(false)
            }
            submitButton.setOnClickListener {
                validated = true
                if (validatePhotos())
                    viewModel.onDocumentVerificationSubmit()
                else
                    firstInvalidView?.let {
                        binding.documentScanContainer.post {
                            binding.documentScanContainer.smoothScrollTo(0, it.top)
                        }
                    }
            }

            documentTypeView.editText?.keyListener = null
            documentTypeView.editText?.setOnClickListener {
                val documentTypeList = listOf(
                    DocumentType.DRIVING_LICENSE.shownStringValue,
                    DocumentType.IDENTITY_CARD.shownStringValue,
                    DocumentType.RESIDENCE_PERMIT.shownStringValue,
                    DocumentType.PASSPORT.shownStringValue,
                    DocumentType.VOTER_ID.shownStringValue,
                    DocumentType.WORK_VISA.shownStringValue,
                )
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.verification_alert_document_type_title)
                    .setItems(documentTypeList.toTypedArray()) { dialog, which ->
                        documentTypeView.setText(documentTypeList[which])
                        viewModel.selectedDocumentType =
                            DocumentType.fromShownString(documentTypeList[which])
                        checkForMandatoryBackPhoto()
                    }
                    .create().show()
            }
            documentTypeView.editText?.addTextChangedListener {
                if (validated) {
                    validateDocumentType()
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.documentStateData.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<VerificationDocumentState> -> showLoading()
                is LoadingData.Success<VerificationDocumentState> -> {
                    hideLoading()
                    populateUiWithDocumentStateData(loadingData.data)
                }
                is LoadingData.Error<VerificationDocumentState> -> {
                    hideLoading()
                }
                else -> {
                }
            }

        }
    }

    private fun initializeSDK() {
        initializeAcuantSdk(object : IAcuantPackageCallback {
            override fun onInitializeSuccess() {
            }

            override fun onInitializeFailed(error: List<Error>) {
                requireActivity().runOnUiThread {
                    val alert = AlertDialog.Builder(requireActivity())
                    alert.setTitle("Error")
                    alert.setMessage("Could not initialize")
                    alert.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alert.show()
                }
            }
        })
    }

    private fun initializeAcuantSdk(callback: IAcuantPackageCallback) {
        try {
            AcuantInitializer.initialize("acuant.config.xml",
                requireActivity(),
                listOf(ImageProcessorInitializer()),
                object : IAcuantPackageCallback {
                    override fun onInitializeSuccess() {
                        callback.onInitializeSuccess()
                    }

                    override fun onInitializeFailed(error: List<Error>) {
                        callback.onInitializeFailed(error)
                    }
                })
        } catch (e: AcuantException) {
            val alert = AlertDialog.Builder(requireActivity())
            alert.setTitle("Error")
            alert.setMessage(e.toString())
            alert.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            alert.show()
        }
    }

    private val frontScanResultCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_FIRST_USER) {
                val url = result.data?.getStringExtra(ACUANT_EXTRA_IMAGE_URL)
                if (url != null) {
                    AcuantImagePreparation.evaluateImage(
                        requireActivity(),
                        CroppingData(url),
                        object :
                            EvaluateImageListener {
                            override fun onSuccess(image: AcuantImage) {
                                binding.frontScanImageView.scaleType =
                                    ImageView.ScaleType.CENTER_CROP
                                binding.frontScanImageView.setImageBitmap(image.image)
                                viewModel.frontScanDocument = image.image
                                binding.toggleFrontScanError(false)
                                binding.frontScanWrapper.show()
                                binding.frontScanButtonView.hide()
                            }

                            override fun onError(error: Error) {
                                val x = 1
                            }
                        })
                }
            }
        }

    private val backScanResultCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_FIRST_USER) {
                val url = result.data?.getStringExtra(ACUANT_EXTRA_IMAGE_URL)
                if (url != null) {
                    AcuantImagePreparation.evaluateImage(
                        requireActivity(),
                        CroppingData(url),
                        object :
                            EvaluateImageListener {
                            override fun onSuccess(image: AcuantImage) {
                                binding.backScanImageView.scaleType =
                                    ImageView.ScaleType.CENTER_CROP
                                binding.backScanImageView.setImageBitmap(image.image)
                                viewModel.backScanDocument = image.image
                                binding.toggleBackScanError(false)
                                binding.backScanWrapper.show()
                                binding.backScanButtonView.hide()
                            }

                            override fun onError(error: Error) {
                                val x = 1
                            }
                        })
                }
            }
        }

    private val selfieResultCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == FacialLivenessActivity.RESPONSE_SUCCESS_CODE) {
                FaceCapturedImage.bitmapImage?.let {
                    binding.selfieImageView.scaleType =
                        ImageView.ScaleType.CENTER_CROP
                    binding.selfieImageView.setImageBitmap(it)
                    viewModel.selfieScan = it
                    binding.toggleSelfieError(false)
                    binding.selfieWrapper.show()
                    binding.selfieButtonView.hide()
                }
            }
        }

    //Show Rear Camera to Capture Image of ID or Passport
    fun showDocumentCaptureCamera(scanType: DocumentCaptureType) {
        val cameraIntent = Intent(
            requireActivity(),
            AcuantCameraActivity::class.java
        )
        cameraIntent.putExtra(
            ACUANT_EXTRA_CAMERA_OPTIONS,
            AcuantCameraOptions.DocumentCameraOptionsBuilder()
                .setAutoCapture(true)
                .build()
        )
        when (scanType) {
            DocumentCaptureType.FRONT_SCAN_DOCUMENT -> {
                frontScanResultCallback.launch(cameraIntent)
            }
            DocumentCaptureType.BACK_SCAN_DOCUMENT -> {
                backScanResultCallback.launch(cameraIntent)
            }
            else -> {
            }
        }


    }

    //Show Front Camera to Capture Live Selfie
    fun showFrontCamera() {
        try {
            val cameraIntent = Intent(
                requireActivity(),
                FacialLivenessActivity::class.java
            )
            selfieResultCallback.launch(cameraIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun validatePhotos(): Boolean {

        val selfie = if (viewModel.selfieScan == null) {
            binding.toggleSelfieError(true, getString(R.string.selfie_validation_text))
            false
        } else true

        val backScan =
            if (viewModel.backScanDocument == null && viewModel.selectedDocumentType != null && viewModel.selectedDocumentType?.needsBackPhoto != false) {
                binding.toggleBackScanError(
                    true,
                    getString(R.string.back_document_scan_validation_text)
                )
                false
            } else true

        val frontScan = if (viewModel.frontScanDocument == null) {
            binding.toggleFrontScanError(
                true,
                getString(R.string.front_document_scan_validation_text)
            )
            false
        } else true

        val documentType = binding.validateDocumentType()

        return documentType && frontScan && backScan && selfie
    }

    private fun populateUiWithDocumentStateData(documentState: VerificationDocumentState) {
        with(binding) {

            documentState.selectedDocumentType?.let {
                documentTypeView.setText(it.shownStringValue)
                viewModel.selectedDocumentType = documentState.selectedDocumentType
            }


            documentState.selfieImageBitmap?.let {
                binding.selfieImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                viewModel.selfieScan = it
                binding.toggleSelfieError(false)
                binding.selfieImageView.setImageBitmap(it)
                binding.selfieWrapper.show()
                binding.selfieButtonView.hide()
            }

            documentState.frontImageBitmap?.let {
                binding.frontScanImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                viewModel.frontScanDocument = it
                binding.toggleFrontScanError(false)
                binding.frontScanImageView.setImageBitmap(it)
                binding.frontScanWrapper.show()
                binding.frontScanButtonView.hide()
            }

            documentState.backImageBitmap?.let {
                binding.backScanImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                viewModel.backScanDocument = it
                binding.toggleSelfieError(false)
                binding.backScanImageView.setImageBitmap(it)
                binding.backScanWrapper.show()
                binding.backScanButtonView.hide()
            }

            if (documentState.frontImageValidationError)
                toggleFrontScanError(
                    true,
                    getString(R.string.front_document_scan_invalid_error_text)

                )
            if (documentState.backImageValidationError)
                toggleBackScanError(
                    true,
                    getString(R.string.back_document_scan_invalid_error_text)

                )
            if (documentState.selfieImageValidationError)
                toggleSelfieError(true, getString(R.string.selfie_invalid_error_text))

            checkForMandatoryBackPhoto()
        }
    }

    private fun FragmentVerificationDocumentPageBinding.toggleFrontScanError(
        toggle: Boolean,
        errorText: String? = null,
    ) {
        if (toggle) {
            frontScanErrorView.visibility = View.VISIBLE
            frontScanErrorView.text = errorText
            frontScanPlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg_error)
            firstInvalidView = frontScanPlaceholder
        } else {
            frontScanErrorView.visibility = View.INVISIBLE
            frontScanPlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg)
        }
    }

    private fun FragmentVerificationDocumentPageBinding.toggleBackScanError(
        toggle: Boolean,
        errorText: String? = null,
    ) {
        if (toggle) {
            backScanErrorView.visibility = View.VISIBLE
            backScanErrorView.text = errorText
            backScanPlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg_error)
            firstInvalidView = backScanPlaceholder
        } else {
            backScanErrorView.visibility = View.INVISIBLE
            backScanPlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg)
        }
    }

    private fun FragmentVerificationDocumentPageBinding.toggleSelfieError(
        toggle: Boolean,
        errorText: String? = null,
    ) {
        if (toggle) {
            selfieErrorView.visibility = View.VISIBLE
            selfieErrorView.text = errorText
            selfiePlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg_error)
            firstInvalidView = selfiePlaceholder
        } else {
            selfieErrorView.visibility = View.INVISIBLE
            selfiePlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg)
        }
    }

    private fun FragmentVerificationDocumentPageBinding.validateDocumentType(): Boolean {
        return if (documentTypeView.getString().isEmpty()) {
            documentTypeView.isErrorEnabled = true
            documentTypeView.error = getString(R.string.document_type_validation_text)
            firstInvalidView = documentTypeView
            false
        } else {
            documentTypeView.isErrorEnabled = false
            true
        }
    }

    private fun showLoading() {
        binding.progressView.visibility = View.VISIBLE
        binding.documentScanContainer.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressView.visibility = View.GONE
        binding.documentScanContainer.visibility = View.VISIBLE
    }


    //    @NeedsPermission(
//        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.CAMERA
//    )
//    fun showFilePicker() {
//        BottomSheetImagePicker.Builder("${BuildConfig.APPLICATION_ID}.fileprovider")
//            .cameraButton(ButtonType.Tile)
//            .galleryButton(ButtonType.Tile)
//            .singleSelectTitle(R.string.verification_select_photo_please)
//            .columnSize(R.dimen.photo_picker_column_size)
//            .requestTag("single")
//            .show(childFragmentManager)
//    }
//
//    @OnNeverAskAgain(
//        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.CAMERA
//    )
//    fun permissionsNeverAskAgain() {
//        AlertHelper.showToastShort(requireContext(), R.string.verification_please_on_permissions)
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        // NOTE: delegate the permission handling to generated method
//        onRequestPermissionsResult(requestCode, grantResults)
//    }
//
//    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
//        val x = uris.first()
//        binding.selfieImageView.scaleType = ImageView.ScaleType.CENTER_CROP
//        val selfieBitmap = BitmapFactory.decodeStream(
//            requireContext().contentResolver.openInputStream(uris.first())
//        )
//        viewModel.selfieScan = selfieBitmap
//        binding.toggleSelfieError("", false)
//        binding.selfieImageView.setImageBitmap(selfieBitmap)
//        binding.selfieWrapper.show()
//        //binding.selfiePlaceholder.hide()
//    }

//    private fun showIPLiveness() {
//        AcuantIPLiveness.getFacialSetup(object : FacialSetupLisenter {
//            override fun onDataReceived(result: FacialSetupResult?) {
//                if (result != null) {
//                    AcuantIPLiveness.runFacialCapture(
//                        requireActivity(),
//                        result,
//                        object : IPLivenessListener {
//                            override fun onCancel() {
//                                TODO("Not yet implemented")
//                            }
//
//                            override fun onFail(error: Error) {
//                                TODO("Not yet implemented")
//                            }
//
//                            override fun onProgress(status: String, progress: Int) {
//                                TODO("Not yet implemented")
//                            }
//
//                            override fun onSuccess(userId: String, token: String) {
//                                startFacialLivelinessRequest(token, userId)
//                            }
//
//                        })
//
//                } else {
//                    // handleInternalError()
//                }
//            }
//
//            override fun onError(errorCode: Int, description: String?) {
//                //handleInternalError()
//            }
//        })
//    }
//    private fun startFacialLivelinessRequest(token: String, userId: String) {
//
//        AcuantIPLiveness.getFacialLiveness(
//            token,
//            userId,
//            object : FacialCaptureLisenter {
//                override fun onDataReceived(result: FacialCaptureResult) {
//                    val facialLivelinessResultString = "Facial Liveliness: " + result.isPassed
//                    val decodedString = Base64.decode(result.frame, Base64.DEFAULT)
//                    val capturedSelfieImage =
//                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
//
//
//                }
//
//                override fun onError(errorCode: Int, errorDescription: String) {
//                    val capturingSelfieImage = false
//
//                    val facialLivelinessResultString = "Facial Liveliness Failed"
//                    val alert = AlertDialog.Builder(requireActivity())
//                    alert.setTitle("Error Retreiving Facial Data")
//                    alert.setMessage(errorDescription)
//                    alert.setPositiveButton("OK") { dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    alert.show()
//                }
//            }
//        )
//    }
    //    private fun getFacialLivenessCredentials(callback: IAcuantPackageCallback) {
//        AcuantIPLiveness.getFacialCaptureCredential(object :
//            FacialCaptureCredentialListener {
//            override fun onDataReceived(result: Boolean) {
//                isIPLivenessEnabled = result
//                callback.onInitializeSuccess()
//            }
//
//            override fun onError(errorCode: Int, description: String) {
//                callback.onInitializeFailed(listOf())
//            }
//        })
//    }

}