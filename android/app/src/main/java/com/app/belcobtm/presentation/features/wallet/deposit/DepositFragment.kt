package com.app.belcobtm.presentation.features.wallet.deposit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.ViewTreeObserver
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.QRUtils
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.activity_deposit.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DepositFragment : BaseFragment() {
    private val viewModel: DepositViewModel by viewModel {
        parametersOf(DepositFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    override val resourceLayout: Int = R.layout.activity_deposit
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true

    override fun initListeners() {
        copyButtonView.setOnClickListener {
            copyToClipboard(
                getString(R.string.wallet_code_clipboard),
                viewModel.addressLiveData.value ?: ""
            )
            AlertHelper.showToastLong(requireContext(), R.string.clipboard)
        }

        imageView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = imageView.layoutParams
                val imageSize = imageView.width
                params.height = imageSize
                imageView.layoutParams = params
                imageView.setImageBitmap(
                    QRUtils.getSpacelessQR(
                        viewModel.addressLiveData.value ?: "",
                        imageSize,
                        imageSize
                    )
                )
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun initViews() {
        val coinCode = DepositFragmentArgs.fromBundle(requireArguments()).coinCode
        setToolbarTitle(getString(R.string.deposit_screen_title, coinCode))
    }

    override fun initObservers() {
        viewModel.addressLiveData.observe(viewLifecycleOwner, { addressView.text = it })
    }

    private fun copyToClipboard(toastText: String, copiedText: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(toastText, copiedText)
        clipboard.setPrimaryClip(clip)
    }
}