package com.app.belcobtm.presentation.features.wallet.transaction.details

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTransactionDetailsBinding
import com.app.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType
import com.app.belcobtm.presentation.core.QRUtils.Companion.getSpacelessQR
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.giphy.sdk.ui.views.GPHMediaView
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TransactionDetailsFragment : BaseFragment<FragmentTransactionDetailsBinding>() {
    private val viewModel: TransactionDetailsViewModel by viewModel {
        val args = TransactionDetailsFragmentArgs.fromBundle(requireArguments())
        parametersOf(args.txId, args.coinCode)
    }
    private val phoneNumberFormatter: Formatter<String> by inject()
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { viewModel.getTransactionDetails() }

    override fun FragmentTransactionDetailsBinding.initViews() {
        setToolbarTitle(getString(R.string.transaction_details_screen_title))
        fromPhoneView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        toPhoneView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun FragmentTransactionDetailsBinding.initObservers() {
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

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionDetailsBinding =
        FragmentTransactionDetailsBinding.inflate(inflater, container, false)

    private fun FragmentTransactionDetailsBinding.showTxId(txId: String, txDbId: String, link: String) = when {
        txId.isNotBlank() -> {
            txDBldContainerView.hide()
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
            txDBldContainerView.show()
            txDBldView.text = txDbId
        }
        else -> {
            txDBldContainerView.hide()
            txIdContainerView.hide()
        }
    }

    private fun FragmentTransactionDetailsBinding.showTypeView(transactionType: TransactionType) =
        if (transactionType == TransactionType.UNKNOWN) {
            typeContainerView.hide()
        } else {
            typeContainerView.show()
            typeView.setText(transactionType.getResText())
        }

    private fun updateStatusView(textView: AppCompatTextView, textColor: Int, backgroundColor: Int, resText: Int) {
        textView.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        textView.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), backgroundColor))
        textView.setText(resText)
    }

    private fun FragmentTransactionDetailsBinding.showStatusView(statusType: TransactionStatusType) =
        when (statusType) {
            TransactionStatusType.PENDING -> {
                updateStatusView(
                    statusView,
                    R.color.colorStatusPending,
                    R.drawable.bg_status_pending,
                    R.string.transition_details_screen_pending
                )
                statusContainerView.show()
            }
            TransactionStatusType.COMPLETE -> {
                updateStatusView(
                    statusView,
                    R.color.colorStatusComplete,
                    R.drawable.bg_status_complete,
                    R.string.transition_details_screen_complete
                )
                statusContainerView.show()
            }
            TransactionStatusType.FAIL -> {
                updateStatusView(
                    statusView,
                    R.color.colorStatusFail,
                    R.drawable.bg_status_fail,
                    R.string.transition_details_screen_fail
                )
                statusContainerView.show()
            }
            else -> statusContainerView.hide()
        }

    private fun FragmentTransactionDetailsBinding.showCashStatus(statusType: TransactionCashStatusType) =
        when (statusType) {
            TransactionCashStatusType.NOT_AVAILABLE -> {
                updateStatusView(
                    cashStatusView,
                    R.color.colorStatusFail,
                    R.drawable.bg_status_fail,
                    R.string.transition_details_screen_not_available
                )
                cashStatusContainerView.show()
            }
            TransactionCashStatusType.AVAILABLE -> {
                updateStatusView(
                    cashStatusView,
                    R.color.colorStatusComplete,
                    R.drawable.bg_status_complete,
                    R.string.transition_details_screen_available
                )
                cashStatusContainerView.show()
            }
            TransactionCashStatusType.WITHDRAWN -> {
                updateStatusView(
                    cashStatusView,
                    R.color.colorStatusPending,
                    R.drawable.bg_status_pending,
                    R.string.transition_details_screen_withdrawn
                )
                cashStatusContainerView.show()
            }
            else -> cashStatusContainerView.hide()
        }

    private fun FragmentTransactionDetailsBinding.showAmountView(cryptoAmount: Double, fiatAmount: Double) =
        if (cryptoAmount < 0) {
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

    private fun FragmentTransactionDetailsBinding.showFeeView(fee: Double) = if (fee < 0) {
        feeContainerView.hide()
    } else {
        val coinCode = TransactionDetailsFragmentArgs.fromBundle(requireArguments()).coinCode
        feeContainerView.show()
        feeView.text = getString(R.string.text_text, fee.toStringCoin(), coinCode)
    }

    private fun FragmentTransactionDetailsBinding.showDateView(date: String) = if (date.isBlank()) {
        dateContainerView.hide()
    } else {
        dateContainerView.show()
        dateView.text = date
    }

    private fun FragmentTransactionDetailsBinding.showFromPhoneView(fromPhone: String) = if (fromPhone.isBlank()) {
        fromPhoneContainerView.hide()
    } else {
        fromPhoneContainerView.show()
        fromPhoneView.text = fromPhone
    }

    private fun FragmentTransactionDetailsBinding.showToPhoneView(fromPhone: String) = if (fromPhone.isBlank()) {
        toPhoneContainerView.hide()
    } else {
        toPhoneContainerView.show()
        toPhoneView.text = fromPhone
    }

    private fun FragmentTransactionDetailsBinding.showFromAddressView(fromAddress: String) =
        if (fromAddress.isBlank()) {
            fromAddressContainerView.hide()
        } else {
            fromAddressContainerView.show()
            fromAddressView.text = fromAddress
        }

    private fun FragmentTransactionDetailsBinding.showToAddressView(toAddress: String) = if (toAddress.isBlank()) {
        toAddressContainerView.hide()
    } else {
        toAddressContainerView.show()
        toAddressView.text = toAddress
    }

    private fun FragmentTransactionDetailsBinding.showPhoneView(phone: String) = if (phone.isBlank()) {
        phoneContainerView.hide()
    } else {
        phoneContainerView.show()
        phoneView.text = phoneNumberFormatter.format(phone)
    }

    private fun FragmentTransactionDetailsBinding.showImageView(imageId: String) = if (imageId.isBlank()) {
        imageContainerView.hide()
    } else {
        imageContainerView.show()
        val mediaView = GPHMediaView(requireContext())
        mediaView.setMediaWithId(imageId)
        imageView.setImageDrawable(mediaView.drawable)
    }

    private fun FragmentTransactionDetailsBinding.showMessageView(message: String) = if (message.isBlank()) {
        messageContainerView.hide()
    } else {
        messageContainerView.show()
        messageView.text = message
    }

    private fun FragmentTransactionDetailsBinding.showSellInfoView(sellInfo: String) = if (sellInfo.isBlank()) {
        qrCodeTitleView.hide()
        qrCodeView.hide()
    } else {
        val point = Point().also { requireActivity().windowManager.defaultDisplay.getSize(it) }
        val qrCodeSize = (if (point.x > point.y) point.y else point.x) / 2
        qrCodeView.setImageBitmap(getSpacelessQR(sellInfo, qrCodeSize, qrCodeSize))
        qrCodeTitleView.show()
        qrCodeView.show()
    }

    private fun FragmentTransactionDetailsBinding.showRefTxIdView(refTxId: String, refLink: String) =
        if (refTxId.isBlank()) {
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

    private fun FragmentTransactionDetailsBinding.showRefCoinView(refCoin: String) = if (refCoin.isBlank()) {
        refCoinContainerView.hide()
    } else {
        refCoinContainerView.show()
        refCoinView.text = refCoin
    }

    private fun FragmentTransactionDetailsBinding.showRefAmountView(refAmount: Double) = if (refAmount < 0) {
        refAmountContainerView.hide()
    } else {
        refAmountContainerView.show()
        refAmountView.text = refAmount.toStringCoin()
    }
}
