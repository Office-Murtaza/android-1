package com.app.belcobtm.ui.main.coins.details

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.TransactionCashStatusType
import com.app.belcobtm.api.model.response.TransactionDetailsResponse
import com.app.belcobtm.api.model.response.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.QRUtils.Companion.getSpacelessQR
import com.app.belcobtm.presentation.core.extensions.*
import com.giphy.sdk.ui.GiphyCoreUI
import com.giphy.sdk.ui.views.GPHMediaView
import kotlinx.android.synthetic.main.activity_details_coin.*

class DetailsActivity : BaseMvpActivity<DetailsContract.View, DetailsContract.Presenter>(),
    DetailsContract.View {
    private val coinCode: String by lazy { intent.getStringExtra(TAG_TRANSACTION_DETAILS_COIN_CODE) ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GiphyCoreUI.configure(this, GIPHY_API_KEY)

        setContentView(R.layout.activity_details_coin)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.title_trans_details) + " " + coinCode
        mPresenter.getDetails(
            coinCode,
            intent.getStringExtra(TAG_TRANSACTION_DETAILS_ID) ?: "",
            intent.getStringExtra(TAG_TRANSACTION_DETAILS_DB_ID) ?: ""
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    override fun showTransactionDetails(detailsResponse: TransactionDetailsResponse?) {
        showTxIdView(detailsResponse?.txId, detailsResponse?.link)
        showTxDbId(detailsResponse?.txDbId)
        showTypeView(detailsResponse?.type)
        showStatusView(detailsResponse?.getStatusType())
        showCashStatus(detailsResponse?.getCashStatusType())
        showAmountView(detailsResponse?.amount)
        showFiatAmountView(detailsResponse?.fiatAmount)
        showFeeView(detailsResponse?.fee)
        showDateView(detailsResponse?.date)
        showFromAddressView(detailsResponse?.fromAddress)
        showToAddressView(detailsResponse?.toAddress)
        showPhoneView(detailsResponse?.phone)
        showImageView(detailsResponse?.imageId)
        showMessageView(detailsResponse?.message)
        showSellInfoView(detailsResponse?.sellInfo)
        showRefTxIdView(detailsResponse?.refTxId, detailsResponse?.refLink)
        showRefCoinView(detailsResponse?.refCoin)
        showRefAmountView(detailsResponse?.refCryptoAmount?.toStringCoin())
        showDividers()
    }

    private fun showTxIdView(txId: String?, link: String?) = if (txId == null) {
        txIdContainerView.hide()
    } else {
        txIdContainerView.show()
        txIdView.text = SpannableString(txId).also { it.setSpan(UnderlineSpan(), 0, it.length, 0) }
        txIdView.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(link)
            startActivity(i)
        }
    }

    private fun showTxDbId(txDbId: Int?) = if (txDbId == null) {
        txDbldContainerView.hide()
    } else {
        txDbldContainerView.show()
        txDbldView.text = txDbId.toString()
    }

    private fun showTypeView(typeCode: Int?) = if (typeCode == null) {
        typeContainerView.hide()
    } else {
        val transactionType = TransactionType.values().firstOrNull { it.code == typeCode } ?: TransactionType.UNKNOWN
        typeContainerView.show()
        typeView.setText(transactionType.getResText())
    }

    private fun changeDrawableColor(
        textView: AppCompatTextView,
        resColor: Int
    ) = (textView.background as GradientDrawable).setColor(ContextCompat.getColor(applicationContext, resColor))

    private fun showStatusView(statusType: TransactionStatusType?) = when (statusType) {
        TransactionStatusType.PENDING -> {
            changeDrawableColor(statusView, R.color.bt_orange)
            statusView.setText(R.string.transition_details_screen_pending)
        }
        TransactionStatusType.COMPLETE -> {
            changeDrawableColor(statusView, R.color.transaction_green)
            statusView.setText(R.string.transition_details_screen_complete)
        }
        TransactionStatusType.FAIL -> {
            changeDrawableColor(statusView, R.color.transaction_red)
            statusView.setText(R.string.transition_details_screen_fail)
        }
        else -> statusContainerView.hide()
    }

    private fun showCashStatus(statusType: TransactionCashStatusType?) = when (statusType) {
        TransactionCashStatusType.NOT_AVAILABLE -> {
            cashStatusView.setTextColor(ContextCompat.getColor(applicationContext, R.color.cash_status_not_available))
            cashStatusView.setText(R.string.transition_details_screen_not_available)
            cashStatusContainerView.show()
        }
        TransactionCashStatusType.AVAILABLE -> {
            cashStatusView.setTextColor(ContextCompat.getColor(applicationContext, R.color.cash_status_available))
            cashStatusView.setText(R.string.transition_details_screen_available)
            cashStatusContainerView.show()
        }
        TransactionCashStatusType.WITHDRAWN -> {
            cashStatusView.setTextColor(ContextCompat.getColor(applicationContext, R.color.cash_status_withdrawn))
            cashStatusView.setText(R.string.transition_details_screen_withdrawn)
            cashStatusContainerView.show()
        }
        else -> cashStatusContainerView.hide()
    }

    private fun showAmountView(amount: Double?) = if (amount == null) {
        amountContainerView.hide()
    } else {
        amountContainerView.show()
        amountView.text = getString(R.string.transition_details_balance_crypto, amount.toStringCoin(), coinCode)
    }

    private fun showFiatAmountView(amount: Double?) = if (amount == null) {
        fiatAmountContainerView.hide()
    } else {
        fiatAmountContainerView.show()
        fiatAmountView.text = getString(R.string.transition_details_balance_usd, amount.toStringUsd())
    }

    private fun showFeeView(fee: Double?) = if (fee == null) {
        feeContainerView.hide()
    } else {
        feeContainerView.show()
        feeView.text = getString(R.string.transition_details_balance_crypto, fee.toStringCoin(), coinCode)
    }

    private fun showDateView(date: String?) = if (date.isNullOrBlank()) {
        dateContainerView.hide()
    } else {
        dateContainerView.show()
        dateView.text = date
    }

    private fun showFromAddressView(fromAddress: String?) = if (fromAddress.isNullOrBlank()) {
        fromAddressContainerView.hide()
    } else {
        fromAddressContainerView.show()
        fromAddressView.text = fromAddress
    }

    private fun showToAddressView(toAddress: String?) = if (toAddress.isNullOrBlank()) {
        toAddressContainerView.hide()
    } else {
        toAddressContainerView.show()
        toAddressView.text = toAddress
    }

    private fun showPhoneView(phone: String?) = if (phone.isNullOrBlank()) {
        phoneContainerView.hide()
    } else {
        phoneContainerView.show()
        phoneView.text = PhoneNumberUtils.formatNumber(phone, "US")
    }

    private fun showImageView(imageId: String?) = if (imageId.isNullOrBlank()) {
        imageContainerView.hide()
    } else {
        imageContainerView.show()
        val mediaView = GPHMediaView(this)
        mediaView.setMediaWithId(imageId)
        imageView.setImageDrawable(mediaView.drawable)
    }

    private fun showMessageView(message: String?) = if (message.isNullOrBlank()) {
        messageContainerView.hide()
    } else {
        messageContainerView.show()
        messageView.text = message
    }

    private fun showSellInfoView(sellInfo: String?) = if (sellInfo.isNullOrBlank()) {
        qrCodeView.hide()
    } else {
        val point = Point().also { windowManager.defaultDisplay.getSize(it) }
        val qrCodeSize = (if (point.x > point.y) point.y else point.x) / 2
        qrCodeView?.setImageBitmap(getSpacelessQR(sellInfo, qrCodeSize, qrCodeSize))
        qrCodeView.show()
    }

    private fun showRefTxIdView(refTxId: String?, refLink: String?) = if (refTxId == null) {
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

    private fun showRefCoinView(refCoin: String?) = if (refCoin == null) {
        refCoinContainerView.hide()
    } else {
        refCoinContainerView.show()
        refCoinView.text = refCoin.toString()
    }

    private fun showRefAmountView(refAmount: String?) = if (refAmount == null) {
        refAmountContainerView.hide()
    } else {
        refAmountContainerView.show()
        refAmountView.text = refAmount
    }

    private fun showDividers() {
        phoneDividerView.toggle(phoneContainerView.isVisible || imageContainerView.isVisible || messageContainerView.isVisible)
        qrCodeDividerView.toggle(qrCodeView.isVisible)
    }

    companion object {
        const val TAG_TRANSACTION_DETAILS_COIN_CODE = "tag_transaction_details_coin_code"
        const val TAG_TRANSACTION_DETAILS_ID = "tag_transaction_details_id"
        const val TAG_TRANSACTION_DETAILS_DB_ID = "tag_transaction_details_db_id"
    }
}
