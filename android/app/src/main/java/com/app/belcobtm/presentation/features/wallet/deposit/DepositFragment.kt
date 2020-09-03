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

class DepositFragment : BaseFragment() {
    override val resourceLayout: Int = R.layout.activity_deposit
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true

    override fun initListeners() {
        val address = DepositFragmentArgs.fromBundle(requireArguments()).coinAddress
        copyButtonView.setOnClickListener {
            copyToClipboard(getString(R.string.wallet_code_clipboard), address)
            AlertHelper.showToastLong(requireContext(), R.string.alert_copy_to_clipboard)
        }
        imageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = imageView.layoutParams
                val imageSize = imageView.width
                params.height = imageSize
                imageView.layoutParams = params
                imageView.setImageBitmap(QRUtils.getSpacelessQR(address, imageSize, imageSize))
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun initViews() {
        val coinCode = DepositFragmentArgs.fromBundle(requireArguments()).coinCode
        setToolbarTitle(getString(R.string.deposit_screen_title, coinCode))
        addressView.text = DepositFragmentArgs.fromBundle(requireArguments()).coinAddress
    }

    private fun copyToClipboard(toastText: String, copiedText: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(toastText, copiedText)
        clipboard.setPrimaryClip(clip)
    }
}