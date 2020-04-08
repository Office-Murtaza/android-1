package com.app.belcobtm.ui.main.coins.details

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
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
import com.app.belcobtm.api.model.response.*
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.QRUtils.Companion.getSpacelessQR
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toggle
import com.giphy.sdk.ui.GiphyCoreUI
import com.giphy.sdk.ui.views.GPHMediaView
import com.giphy.sdk.ui.views.GiphyDialogFragment
import kotlinx.android.synthetic.main.activity_details_coin.*
import org.parceler.Parcels


class DetailsActivity : BaseMvpActivity<DetailsContract.View, DetailsContract.Presenter>(),
    DetailsContract.View {
    private lateinit var gifsDialog: GiphyDialogFragment
    private lateinit var mCoin: CoinModel
    private lateinit var transaction: TransactionModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GiphyCoreUI.configure(this, GIPHY_API_KEY)

        setContentView(R.layout.activity_details_coin)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mCoin = Parcels.unwrap(intent.getParcelableExtra(KEY_COIN))
        transaction = intent.getSerializableExtra(KEY_TRANS) as TransactionModel
        supportActionBar?.title = getString(R.string.title_trans_details) + " " + mCoin.coinId

        mPresenter.bindData(mCoin, transaction)
        mPresenter.getDetails()
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

    fun trimTrailingZero(value: String?): String? {
        return if (!value.isNullOrEmpty()) {
            if (value.indexOf(".") < 0) {
                value
            } else {
                value.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
            }
        } else {
            value
        }
    }

    private fun getTextFromClipboard(): String {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text.toString()
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

    private fun showTypeView(type: Int?) = if (type == null) {
        typeContainerView.hide()
    } else {
        typeContainerView.show()
        typeView.text = getString(
            when (type) {
                1 -> R.string.deposit
                2 -> R.string.withdraw
                3 -> R.string.send_gift
                4 -> R.string.receive_gift
                5 -> R.string.buy
                else -> R.string.sell
            }
        )
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
        amountView.text = String.format(" %.8f", amount)
        amountView.text = """${trimTrailingZero(amountView.text.toString())?.replace(" ", "")} ${mCoin.coinId}"""
    }

    private fun showFiatAmountView(amount: Double?) = if (amount == null) {
        fiatAmountContainerView.hide()
    } else {
        fiatAmountContainerView.show()
        fiatAmountView.text = String.format(" %.8f", amount)
        fiatAmountView.text =
            """${trimTrailingZero(fiatAmountView.text.toString())?.replace(" ", "")} $TAG_USD"""
    }

    private fun showFeeView(fee: Double?) = if (fee == null) {
        feeContainerView.hide()
    } else {
        feeContainerView.show()
        feeView.text = String.format(" %.8f", fee)
        feeView.text = """${trimTrailingZero(feeView.text.toString())?.replace(" ", "")} ${mCoin.coinId}"""
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
        refAmountView.text = refAmountView.toString()
    }

    private fun showDividers() {
        phoneDividerView.toggle(phoneContainerView.isVisible || imageContainerView.isVisible || messageContainerView.isVisible)
        qrCodeDividerView.toggle(qrCodeView.isVisible)
    }

    companion object {
        private const val KEY_TRANS = "KEY TRANS"
        private const val KEY_COIN = "KEY_COIN"
        private const val TAG_USD = "USD"

        @JvmStatic
        fun start(context: Context?, trans: TransactionModel, coin: CoinModel) {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra(KEY_TRANS, trans)
            intent.putExtra(KEY_COIN, Parcels.wrap(coin))
            context?.startActivity(intent)
        }
    }
}
