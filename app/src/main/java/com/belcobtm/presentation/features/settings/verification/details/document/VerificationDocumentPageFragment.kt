package com.belcobtm.presentation.features.settings.verification.details.document

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.acuant.acuantcommon.model.Credential
import com.acuant.acuantcommon.model.Error
import com.acuant.acuantimagepreparation.AcuantImagePreparation
import com.acuant.acuantimagepreparation.background.EvaluateImageListener
import com.acuant.acuantimagepreparation.initializer.ImageProcessorInitializer
import com.acuant.acuantimagepreparation.model.AcuantImage
import com.acuant.acuantimagepreparation.model.CroppingData
import com.acuant.acuantipliveness.AcuantIPLiveness
import com.acuant.acuantipliveness.IPLivenessListener
import com.acuant.acuantipliveness.facialcapture.model.FacialCaptureResult
import com.acuant.acuantipliveness.facialcapture.model.FacialSetupResult
import com.acuant.acuantipliveness.facialcapture.service.FacialCaptureCredentialListener
import com.acuant.acuantipliveness.facialcapture.service.FacialCaptureLisenter
import com.acuant.acuantipliveness.facialcapture.service.FacialSetupLisenter
import com.belcobtm.BuildConfig
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationDocumentPageBinding
import com.belcobtm.domain.settings.type.DocumentCaptureType
import com.belcobtm.presentation.core.extensions.getString
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.extensions.setText
import com.belcobtm.presentation.core.extensions.show
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import com.belcobtm.presentation.features.settings.verification.details.VerificationDocumentState
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
    private var validated = false
    private var isIPLivenessEnabled = false
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
                showFilePicker()
            }
            selfiePlaceholder.setOnClickListener {
                showFilePicker()
            }

            removeFrontScanButtonView.setOnClickListener {
                viewModel.frontScanDocument = null
                frontScanImageView.setImageDrawable(null)
                frontScanPlaceholder.show()
                frontScanWrapper.hide()
            }
            removeBackScanButtonView.setOnClickListener {
                viewModel.backScanDocument = null
                backScanImageView.setImageDrawable(null)
                backScanPlaceholder.show()
                backScanWrapper.hide()
            }
            removeSelfieButtonView.setOnClickListener {
                viewModel.selfieScan = null
                selfieImageView.setImageDrawable(null)
                selfiePlaceholder.show()
                selfieWrapper.hide()
            }
            submitButton.setOnClickListener {
                validated = true
                if (validatePhotos())
                    viewModel.onDocumentVerificationSubmit()
            }

            documentTypeView.editText?.keyListener = null
            documentTypeView.editText?.setOnClickListener {
                viewModel.item?.let { item ->
                    val documentTypeList = listOf(
                        "Driving license",
                        "Identity Card",
                        "Residence Permit",
                        "Passport",
                        "Voterd ID",
                        "Work Visa"
                    )
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(R.string.verification_alert_country_title)
                        .setItems(documentTypeList.toTypedArray()) { dialog, which ->
                            documentTypeView.setText(documentTypeList[which])
                        }
                        .create().show()
                }
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
                    Toast.makeText(requireActivity(), "SUCCESSSS", Toast.LENGTH_LONG).show()
                }
                is LoadingData.Error<VerificationDocumentState> -> {
                    hideLoading()
                    Toast.makeText(requireActivity(), "ERORRRRR", Toast.LENGTH_LONG).show()
                }
                else -> {
                }
            }

        }
    }


    private fun validatePhotos(): Boolean {
        val documentType = binding.validateDocumentType()

        val frontScan = if (viewModel.frontScanDocument == null) {
            binding.toggleFrontScanError(
                getString(R.string.front_document_scan_validation_text),
                true
            )
            false
        } else true

        val backScan = if (viewModel.backScanDocument == null) {
            binding.toggleBackScanError(
                getString(R.string.back_document_scan_validation_text),
                true
            )
            false
        } else true

        val selfie = if (viewModel.selfieScan == null) {
            binding.toggleSelfieError(getString(R.string.selfie_validation_text), true)
            false
        } else true

        return documentType && frontScan && backScan && selfie
    }

    private fun initializeSDK() {
        //  setProgress(true, "Initializing...")
        initializeAcuantSdk(object : IAcuantPackageCallback {

            override fun onInitializeSuccess() {
                requireActivity().runOnUiThread {
                    // isInitialized = true
                    //setProgress(false)
                }
            }

            override fun onInitializeFailed(error: List<com.acuant.acuantcommon.model.Error>) {
                requireActivity().runOnUiThread {
                    //setProgress(false)
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
                        if (Credential.get().subscription == null || Credential.get().subscription.isEmpty()) {
                            isIPLivenessEnabled = false
                            callback.onInitializeSuccess()
                        } else {
                            getFacialLivenessCredentials(callback)
                        }
                    }

                    override fun onInitializeFailed(error: List<com.acuant.acuantcommon.model.Error>) {
                        callback.onInitializeFailed(error)
                    }

                })
        } catch (e: AcuantException) {
            Log.e("Acuant Error", e.toString())
            // setProgress(false)
            val alert = AlertDialog.Builder(requireActivity())
            alert.setTitle("Error")
            alert.setMessage(e.toString())
            alert.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            alert.show()

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
        val x = uris.first()
        binding.selfieImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        val selfieBitmap = BitmapFactory.decodeStream(
            requireContext().contentResolver.openInputStream(uris.first())
        )
        viewModel.selfieScan = selfieBitmap
        binding.toggleSelfieError("", false)
        binding.selfieImageView.setImageBitmap(selfieBitmap)
        binding.selfieWrapper.show()
        binding.selfiePlaceholder.hide()
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
                                binding.toggleFrontScanError(null, false)
                                binding.frontScanWrapper.show()
                                binding.frontScanPlaceholder.hide()
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
                                val x = url
                                viewModel.backScanDocument = image.image
                                binding.toggleBackScanError(null, false)
                                binding.backScanWrapper.show()
                                binding.backScanPlaceholder.hide()
                            }

                            override fun onError(error: Error) {
                                val x = 1
                            }
                        })
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
            //  capturingSelfieImage = true

            if (isIPLivenessEnabled) {
                showIPLiveness()
            } else {
                showHGLiveness()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showIPLiveness() {
        AcuantIPLiveness.getFacialSetup(object : FacialSetupLisenter {
            override fun onDataReceived(result: FacialSetupResult?) {
                if (result != null) {
                    AcuantIPLiveness.runFacialCapture(
                        requireActivity(),
                        result,
                        object : IPLivenessListener {
                            override fun onCancel() {
                                TODO("Not yet implemented")
                            }

                            override fun onFail(error: Error) {
                                TODO("Not yet implemented")
                            }

                            override fun onProgress(status: String, progress: Int) {
                                TODO("Not yet implemented")
                            }

                            override fun onSuccess(userId: String, token: String) {
                                startFacialLivelinessRequest(token, userId)
                            }

                        })

                } else {
                    // handleInternalError()
                }
            }

            override fun onError(errorCode: Int, description: String?) {
                //handleInternalError()
            }
        })
    }

    private fun startFacialLivelinessRequest(token: String, userId: String) {

        AcuantIPLiveness.getFacialLiveness(
            token,
            userId,
            object : FacialCaptureLisenter {
                override fun onDataReceived(result: FacialCaptureResult) {
                    val facialLivelinessResultString = "Facial Liveliness: " + result.isPassed
                    val decodedString = Base64.decode(result.frame, Base64.DEFAULT)
                    val capturedSelfieImage =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)


                }

                override fun onError(errorCode: Int, errorDescription: String) {
                    val capturingSelfieImage = false

                    val facialLivelinessResultString = "Facial Liveliness Failed"
                    val alert = AlertDialog.Builder(requireActivity())
                    alert.setTitle("Error Retreiving Facial Data")
                    alert.setMessage(errorDescription)
                    alert.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alert.show()
                }
            }
        )
    }

    private fun showHGLiveness() {
//        val cameraIntent = Intent(
//            requireActivity(),
//            FacialLivenessActivity::class.java
//        )
//        startActivityForResult(cameraIntent, 3)//Constants.REQUEST_CAMERA_HG_SELFIE)
    }

    private fun getFacialLivenessCredentials(callback: IAcuantPackageCallback) {
        AcuantIPLiveness.getFacialCaptureCredential(object :
            FacialCaptureCredentialListener {
            override fun onDataReceived(result: Boolean) {
                isIPLivenessEnabled = result
                callback.onInitializeSuccess()
            }

            override fun onError(errorCode: Int, description: String) {
                callback.onInitializeFailed(listOf())
            }
        })
    }


    private fun FragmentVerificationDocumentPageBinding.toggleFrontScanError(
        errorText: String?,
        toggle: Boolean
    ) {
        if (toggle) {
            frontScanErrorView.visibility = View.VISIBLE
            frontScanErrorView.text = errorText
            frontScanPlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg_error)
        } else {
            frontScanErrorView.visibility = View.INVISIBLE
            frontScanPlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg)
        }
    }

    private fun FragmentVerificationDocumentPageBinding.toggleBackScanError(
        errorText: String?,
        toggle: Boolean
    ) {
        if (toggle) {
            backScanErrorView.visibility = View.VISIBLE
            backScanErrorView.text = errorText
            backScanPlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg_error)
        } else {
            backScanErrorView.visibility = View.INVISIBLE
            backScanPlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg)
        }
    }

    private fun FragmentVerificationDocumentPageBinding.toggleSelfieError(
        errorText: String?,
        toggle: Boolean
    ) {
        if (toggle) {
            selfieErrorView.visibility = View.VISIBLE
            selfieErrorView.text = errorText
            selfiePlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg_error)
        } else {
            selfieErrorView.visibility = View.INVISIBLE
            selfiePlaceholder.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.attach_scan_bg)
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

    private fun FragmentVerificationDocumentPageBinding.validateDocumentType(): Boolean {
        return if (documentTypeView.getString().isEmpty()) {
            documentTypeView.isErrorEnabled = true
            documentTypeView.error = getString(R.string.document_type_validation_text)
            false
        } else {
            documentTypeView.isErrorEnabled = false
            true
        }
    }

}