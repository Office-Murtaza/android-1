package com.app.belcobtm.ui.main.coins.details

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CashStatusType
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.TransactionDetailsResponse
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.core.Const.GIPHY_API_KEY
import com.app.belcobtm.core.QRUtils.Companion.getSpacelessQR
import com.app.belcobtm.core.extensions.hide
import com.app.belcobtm.core.extensions.show
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
        typeView.text = getString(
            when (detailsResponse?.type) {
                1 -> R.string.deposit
                2 -> R.string.withdraw
                3 -> R.string.send_gift
                4 -> R.string.receive_gift
                5 -> R.string.buy
                else -> R.string.sell
            }
        )
        statusView.text = when (detailsResponse?.status) {
            1 -> "Pending"
            2 -> "Complete"
            3 -> "Fail"
            else -> "Unknown"
        }

        amountView.text = String.format(" %.8f", detailsResponse?.amount)
        amountView.text = """${trimTrailingZero(amountView.text.toString())?.replace(" ", "")} ${mCoin.coinId}"""
        feeView.text = String.format(" %.8f", detailsResponse?.fee)
        feeView.text = """${trimTrailingZero(feeView.text.toString())?.replace(" ", "")} ${mCoin.coinId}"""

        detailsResponse?.let { response ->
            showTxIdView(response.txId)
            showTxDbId(response.txDbId)
            showCashStatus(response.getCashStatusType())

            txIdView?.setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(response.link)
                startActivity(i)
            }
//
            dateView.text = response.date
            fromAddressView.text = response.fromAddress
            toAddressView.text = response.toAddress

            response.imageId?.let { imageId ->
                val mediaView = GPHMediaView(this)
                giftContainerView.show()
                imageContainerView.show()
                mediaView.setMediaWithId(imageId)
                imageView.setImageDrawable(mediaView.drawable)
            }

            response.message?.let { message ->
                giftContainerView.show()
                messageContainerView.show()
                messageView.text = message
            }

            response.phone?.let { phone ->
                giftContainerView.show()
                phoneContainerView.show()
                phoneView.text = PhoneNumberUtils.formatNumber(phone, "US")
            }

            response.sellInfo?.let { sellInfo ->
                /*  val walletQrCode = BarcodeEncoder().encodeBitmap(it, BarcodeFormat.QR_CODE, 124, 124  )
             qrCodeIv?.setImageBitmap(walletQrCode)
             */
                qrCodeView?.setImageBitmap(getSpacelessQR(sellInfo, 104, 104))
            }
        }
    }

    fun trimTrailingZero(value: String?): String? {
        return if (!value.isNullOrEmpty()) {
            if (value!!.indexOf(".") < 0) {
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

    private fun showTxIdView(txId:String?)  = if (txId == null) {
        txIdContainerView.hide()
    } else {
        txIdContainerView.show()
        txIdView.text = SpannableString(txId).also { it.setSpan(UnderlineSpan(), 0, it.length, 0) }
    }

    private fun showTxDbId(txDbId: Int?) = if (txDbId == null) {
        txDbldContainerView.hide()
    } else {
        txDbldContainerView.show()
        txDbldView.text = txDbId.toString()
    }

    private fun showCashStatus(statusType: CashStatusType) = when (statusType) {
        CashStatusType.NOT_AVAILABLE -> {
            cashStatusView.setTextColor(ContextCompat.getColor(applicationContext, R.color.cash_status_not_available))
            cashStatusView.setText(R.string.transition_details_screen_not_available)
            cashStatusContainerView.show()
        }
        CashStatusType.AVAILABLE -> {
            cashStatusView.setTextColor(ContextCompat.getColor(applicationContext, R.color.cash_status_available))
            cashStatusView.setText(R.string.transition_details_screen_available)
            cashStatusContainerView.show()
        }
        CashStatusType.WITHDRAWN -> {
            cashStatusView.setTextColor(ContextCompat.getColor(applicationContext, R.color.cash_status_withdrawn))
            cashStatusView.setText(R.string.transition_details_screen_withdrawn)
            cashStatusContainerView.show()
        }
        else -> cashStatusContainerView.hide()
    }

    companion object {
        private const val KEY_TRANS = "KEY TRANS"
        private const val KEY_COIN = "KEY_COIN"

        @JvmStatic
        fun start(context: Context?, trans: TransactionModel, coin: CoinModel) {
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra(KEY_TRANS, trans)
            intent.putExtra(KEY_COIN, Parcels.wrap(coin))
            context?.startActivity(intent)
        }
    }
}
