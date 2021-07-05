package com.belcobtm.presentation.features.wallet.deposit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import com.belcobtm.R
import com.belcobtm.databinding.FragmentDepositBinding
import com.belcobtm.presentation.core.QRUtils
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DepositFragment : BaseFragment<FragmentDepositBinding>() {
    private val viewModel: DepositViewModel by viewModel {
        parametersOf(DepositFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true

    override fun FragmentDepositBinding.initViews() {
        val coinCode = DepositFragmentArgs.fromBundle(requireArguments()).coinCode
        setToolbarTitle(getString(R.string.deposit_screen_title, coinCode))
    }

    override fun FragmentDepositBinding.initObservers() {
        viewModel.addressLiveData.observe(viewLifecycleOwner, Observer { address ->
            addressView.text = address
            imageView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val params = imageView.layoutParams
                    val imageSize = imageView.width
                    params.height = imageSize
                    imageView.layoutParams = params
                    imageView.setImageBitmap(
                        QRUtils.getSpacelessQR(
                            address,
                            imageSize,
                            imageSize
                        )
                    )
                    copyButtonView.setOnClickListener {
                        copyToClipboard(
                            getString(R.string.wallet_code_clipboard),
                            address
                        )
                        AlertHelper.showToastLong(requireContext(), R.string.clipboard)
                    }
                    imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        })
    }

    private fun copyToClipboard(toastText: String, copiedText: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(toastText, copiedText)
        clipboard.setPrimaryClip(clip)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDepositBinding =
        FragmentDepositBinding.inflate(inflater, container, false)
}