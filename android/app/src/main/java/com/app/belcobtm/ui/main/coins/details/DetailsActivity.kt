package com.app.belcobtm.ui.main.coins.details

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.MenuItem
import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.TransactionDetailsResponse
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.util.Const.GIPHY_API_KEY
import com.app.belcobtm.util.QRUtils.Companion.getSpacelessQR
import com.giphy.sdk.ui.GiphyCoreUI
import com.giphy.sdk.ui.views.GPHMediaView
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_details_coin.*
import org.parceler.Parcels
import java.util.*


class DetailsActivity : BaseMvpActivity<DetailsContract.View, DetailsContract.Presenter>(),
    DetailsContract.View {
    override fun showTransactionDetails(resp: TransactionDetailsResponse?) {

        val content = SpannableString(transaction.txid)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        textTv.text = content

        resp?.link?.let { url ->

            textTv?.setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }

        val transactionTypeTextId = when (resp?.type) {
            1 -> R.string.deposit
            2 -> R.string.withdraw
            3 -> R.string.send_gift
            4 -> R.string.receive_gift
            5 -> R.string.buy
            else -> R.string.sell
        }

        typeTv.text = getString(transactionTypeTextId)


        val transactionStatusText = when (resp?.status) {
            1 -> {
                "Pending"
            }
            2 -> {
                "Complete"
            }
            3 -> {
                "Fail"
            }
            else -> {
                "Unknown"
            }
        }

        statusTv.text = transactionStatusText

        amountTv.text = "${String.format(" %.8f", resp?.amount)}"
        amountTv.text =
            """${trimTrailingZero(amountTv.text.toString())?.replace(" ", "")} ${mCoin.coinId}"""

        feeTv.text = "${String.format(" %.8f", resp?.fee)}"
        feeTv.text =
            """${trimTrailingZero(feeTv.text.toString())?.replace(" ", "")} ${mCoin.coinId}"""

        dateTv.text = resp?.date
        fromAddressTv.text = resp?.fromAddress
        toAddressTv.text = resp?.toAddress


        val mediaView = GPHMediaView(this)

        resp?.imageId?.let {
            giftContainer.visibility = View.VISIBLE
            imageIv.visibility = View.VISIBLE
            imageLabel.visibility = View.VISIBLE
            mediaView.setMediaWithId(resp.imageId)
            imageIv.setImageDrawable(mediaView.drawable)
        }

        resp?.message?.let {
            giftContainer.visibility = View.VISIBLE
            messageLabel.visibility = View.VISIBLE
            messageTv.visibility = View.VISIBLE
            messageTv.text = resp.message
        }

        resp?.phone?.let {
            phoneTv.text = PhoneNumberUtils.formatNumber(it, "US")

            giftContainer.visibility = View.VISIBLE
            phoneLabel.visibility = View.VISIBLE
            phoneTv.visibility = View.VISIBLE
        }

        resp?.sellInfo?.let {

          /*  val walletQrCode =
                BarcodeEncoder().encodeBitmap(
                    it,
                    BarcodeFormat.QR_CODE, 124, 124
                )



            qrCodeIv?.setImageBitmap(walletQrCode)
            */
            qrCodeIv?.setImageBitmap(getSpacelessQR(it,104,104))
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

    private lateinit var gifsDialog: GiphyDialogFragment
    private lateinit var mCoin: CoinModel
    private lateinit var transaction: TransactionModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GiphyCoreUI.configure(this, GIPHY_API_KEY)

        setContentView(R.layout.activity_details_coin)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mCoin = Parcels.unwrap(intent.getParcelableExtra(KEY_COIN))
        transaction = intent.getSerializableExtra(KEY_TRANS) as TransactionModel
        supportActionBar?.title = getString(R.string.title_trans_details) + " " + mCoin.coinId

        initView()
        mPresenter.bindData(mCoin, transaction)
        mPresenter.getDetails()
    }

    private fun initView() {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getTextFromClipboard(): String {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text.toString()
    }


}
