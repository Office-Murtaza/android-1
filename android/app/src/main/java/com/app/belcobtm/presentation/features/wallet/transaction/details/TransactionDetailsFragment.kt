package com.app.belcobtm.presentation.features.wallet.transaction.details

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.QRUtils.Companion.getSpacelessQR
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.giphy.sdk.ui.GiphyCoreUI
import com.giphy.sdk.ui.views.GPHMediaView
import kotlinx.android.synthetic.main.fragment_recover_wallet.*
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import kotlinx.android.synthetic.main.fragment_transaction_details.phoneView
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TransactionDetailsFragment : BaseFragment() {
    private val viewModel: TransactionDetailsViewModel by viewModel {
        val args = TransactionDetailsFragmentArgs.fromBundle(requireArguments())
        parametersOf(args.txId, args.coinCode)
    }
    override val resourceLayout: Int = R.layout.fragment_transaction_details
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { viewModel.getTransactionDetails() }

    override fun initViews() {
        setToolbarTitle(getString(R.string.transaction_details_screen_title))
        fromPhoneView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        toPhoneView.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        GiphyCoreUI.configure(requireContext(), GIPHY_API_KEY)
    }

    override fun initObservers() {
        viewModel.transactionDetailsLiveData.listen({
            showTxId(it.txId, it.txDbId, it.link)
            showTypeView(it.type)
            showStatusView(it.statusType)
            showCashStatus(it.cashStatusType)
            showAmountView(it.cryptoAmount, it.fiatAmount)
            showFeeView(it.cryptoFee)
            showDateView(it.date)
            showFromPhoneView(it.fromPhone)
            showFromAddressView(it.fromAddress)
            showToPhoneView(it.toPhone)
            showToAddressView(it.toAddress)
            showPhoneView(it.phone)
            showImageView(it.imageId)
            showMessageView(it.message)
            showSellInfoView(it.sellInfo)
            showRefTxIdView(it.refTxId, it.refLink)
            showRefCoinView(it.refCoin)
            showRefAmountView(it.refCryptoAmount)
        })
    }

    private fun showTxId(txId: String, txDbId: String, link: String) = when {
        txId.isNotBlank() -> {
            txDbldContainerView.hide()
            txIdContainerView.show()
            txIdView.text = SpannableString(txId).also { it.setSpan(UnderlineSpan(), 0, it.length, 0) }
            txIdView.setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(link)
                startActivity(i)
            }
        }
        txDbId.isNotBlank() -> {
            txIdContainerView.hide()
            txDbldContainerView.show()
            txDbldView.text = txDbId
        }
        else -> {
            txDbldContainerView.hide()
            txIdContainerView.hide()
        }
    }

    private fun showTypeView(transactionType: TransactionType) = if (transactionType == TransactionType.UNKNOWN) {
        typeContainerView.hide()
    } else {
        typeContainerView.show()
        typeView.setText(transactionType.getResText())
    }

    private fun showStatusView(statusType: TransactionStatusType) = when (statusType) {
        TransactionStatusType.PENDING -> {
            statusView.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_pending))
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorStatusPending))
            statusView.setText(R.string.transition_details_screen_pending)
            statusContainerView.show()
        }
        TransactionStatusType.COMPLETE -> {
            statusView.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_complete))
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorStatusComplete))
            statusView.setText(R.string.transition_details_screen_complete)
            statusContainerView.show()
        }
        TransactionStatusType.FAIL -> {
            statusView.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_fail))
            statusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorStatusFail))
            statusView.setText(R.string.transition_details_screen_fail)
            statusContainerView.show()
        }
        else -> statusContainerView.hide()
    }

    private fun showCashStatus(statusType: TransactionCashStatusType) = when (statusType) {
        TransactionCashStatusType.NOT_AVAILABLE -> {
            cashStatusView.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_fail))
            cashStatusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorStatusFail))
            cashStatusView.setText(R.string.transition_details_screen_not_available)
            cashStatusContainerView.show()
        }
        TransactionCashStatusType.AVAILABLE -> {
            cashStatusView.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_status_complete
                )
            )
            cashStatusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorStatusComplete))
            cashStatusView.setText(R.string.transition_details_screen_available)
            cashStatusContainerView.show()
        }
        TransactionCashStatusType.WITHDRAWN -> {
            cashStatusView.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_status_pending
                )
            )
            cashStatusView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorStatusPending))
            cashStatusView.setText(R.string.transition_details_screen_withdrawn)
            cashStatusContainerView.show()
        }
        else -> cashStatusContainerView.hide()
    }

    private fun showAmountView(cryptoAmount: Double, fiatAmount: Double) = if (cryptoAmount < 0) {
        amountContainerView.hide()
    } else {
        val coinCode = TransactionDetailsFragmentArgs.fromBundle(requireArguments()).coinCode
        amountContainerView.show()
        amountCryptoView.text =
            getString(R.string.text_text, cryptoAmount.toStringCoin(), coinCode)
        amountUsdView.text = getString(R.string.text_usd, fiatAmount.toStringUsd())
        amountArrowsView.toggle(fiatAmount >= 0)
        amountUsdView.toggle(fiatAmount >= 0)
    }

    private fun showFeeView(fee: Double) = if (fee < 0) {
        feeContainerView.hide()
    } else {
        val coinCode = TransactionDetailsFragmentArgs.fromBundle(requireArguments()).coinCode
        feeContainerView.show()
        feeView.text = getString(R.string.text_text, fee.toStringCoin(), coinCode)
    }

    private fun showDateView(date: String) = if (date.isBlank()) {
        dateContainerView.hide()
    } else {
        dateContainerView.show()
        dateView.text = date
    }

    private fun showFromPhoneView(fromPhone: String) = if (fromPhone.isBlank()) {
        fromPhoneContainerView.hide()
    } else {
        fromPhoneContainerView.show()
        fromPhoneView.text = fromPhone
    }

    private fun showToPhoneView(fromPhone: String) = if (fromPhone.isBlank()) {
        toPhoneContainerView.hide()
    } else {
        toPhoneContainerView.show()
        toPhoneView.text = fromPhone
    }

    private fun showFromAddressView(fromAddress: String) = if (fromAddress.isBlank()) {
        fromAddressContainerView.hide()
    } else {
        fromAddressContainerView.show()
        fromAddressView.text = fromAddress
    }

    private fun showToAddressView(toAddress: String) = if (toAddress.isBlank()) {
        toAddressContainerView.hide()
    } else {
        toAddressContainerView.show()
        toAddressView.text = toAddress
    }

    private fun showPhoneView(phone: String) = if (phone.isBlank()) {
        phoneContainerView.hide()
    } else {
        phoneContainerView.show()
        phoneView.text = PhoneNumberUtils.formatNumber(phone, "US")
    }

    private fun showImageView(imageId: String) = if (imageId.isBlank()) {
        imageContainerView.hide()
    } else {
        imageContainerView.show()
        val mediaView = GPHMediaView(requireContext())
        mediaView.setMediaWithId(imageId)
        imageView.setImageDrawable(mediaView.drawable)
    }

    private fun showMessageView(message: String) = if (message.isBlank()) {
        messageContainerView.hide()
    } else {
        messageContainerView.show()
        messageView.text = message
    }

    private fun showSellInfoView(sellInfo: String) = if (sellInfo.isBlank()) {
        qrCodeTitleView.hide()
        qrCodeView.hide()
    } else {
        val point = Point().also { requireActivity().windowManager.defaultDisplay.getSize(it) }
        val qrCodeSize = (if (point.x > point.y) point.y else point.x) / 2
        qrCodeView?.setImageBitmap(getSpacelessQR(sellInfo, qrCodeSize, qrCodeSize))
        qrCodeTitleView.show()
        qrCodeView.show()
    }

    private fun showRefTxIdView(refTxId: String, refLink: String) = if (refTxId.isBlank()) {
        refTxIdContainerView.hide()
    } else {
        refTxIdContainerView.show()
        refTxIdView.text = SpannableString(refTxId).also { it.setSpan(UnderlineSpan(), 0, it.length, 0) }
        refTxIdView.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(refLink)
            startActivity(i)
        }
    }

    private fun showRefCoinView(refCoin: String) = if (refCoin.isBlank()) {
        refCoinContainerView.hide()
    } else {
        refCoinContainerView.show()
        refCoinView.text = refCoin
    }

    private fun showRefAmountView(refAmount: Double) = if (refAmount < 0) {
        refAmountContainerView.hide()
    } else {
        refAmountContainerView.show()
        refAmountView.text = refAmount.toStringCoin()
    }
}
