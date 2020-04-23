package com.app.belcobtm.ui.main.coins.send_gift

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.extensions.*
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.enums.RenditionType
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.GiphyCoreUI
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.themes.LightTheme
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_send_gift.*
import org.parceler.Parcels

class SendGiftActivity : BaseMvpActivity<SendGiftContract.View, SendGiftContract.Presenter>(),
    SendGiftContract.View, GiphyDialogFragment.GifSelectionListener {

    private lateinit var gifsDialog: GiphyDialogFragment
    private lateinit var mCoin: CoinModel
    var cryptoBalanceToSend = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GiphyCoreUI.configure(this, GIPHY_API_KEY)

        setContentView(R.layout.activity_send_gift)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        mCoin = Parcels.unwrap(intent.getParcelableExtra(KEY_COIN))
        supportActionBar?.title = "Send Gift" + " " + mCoin.coinId

        initListeners()
        initView()
    }

    private fun initListeners() {
        phoneView?.editText?.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        pastePhoneView.setOnClickListener { phoneView.setText(getTextFromClipboard()) }
        maxUsdView.setOnClickListener { selectMaxPrice() }
        maxCryptoView.setOnClickListener { selectMaxPrice() }
        addGifButtonView.setOnClickListener { openGify() }
        gifEmptyView.setOnClickListener { openGify() }
        removeGifButtonView.setOnClickListener {
            if (mPresenter.gifMedia != null) {
                mPresenter.gifMedia = null
                gifView.setMedia(null, RenditionType.original)
                gifEmptyView.visibility = View.VISIBLE
                gifView.visibility = View.INVISIBLE
            }
        }

        amountCryptoView?.editText?.addTextChangedListener(coinFromTextWatcher)
        amountCryptoView.actionDoneListener { validateAndSubmit() }
        amountUsdView?.editText?.keyListener = null
        nextButtonView.setOnClickListener { validateAndSubmit() }
    }

    private fun initView() {
        phonePickerView.registerCarrierNumberEditText(phoneView.editText)

        amountCryptoView.hint = mCoin.coinId
        val settings = GPHSettings(
            gridType = GridType.waterfall,
            theme = LightTheme,
            dimBackground = true,
            mediaTypeConfig = arrayOf(GPHContentType.gif)
        )
        gifsDialog = GiphyDialogFragment.newInstance(settings)

        initPrice()
        initBalance()
    }

    private fun initPrice() {
        val convertedPrice = if (mCoin.price.uSD > 0) String.format("%.2f", mCoin.price.uSD).trimEnd('0') else "0"
        priceUsdView.text = getString(R.string.transaction_price_usd, convertedPrice)
    }

    private fun initBalance() {
        balanceCryptoView.text =
            getString(R.string.transaction_crypto_balance, mCoin.balance.toStringCoin(), mCoin.coinId)
        balanceUsdView.text = getString(R.string.transaction_price_usd, (mCoin.balance * mCoin.price.uSD).toStringUsd())
    }

    private fun selectMaxPrice() {
        val balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
        cryptoBalanceToSend = if (balance > 0) {
            balance
        } else {
            0.0
        }
        amountCryptoView.setText(balance.toStringCoin())
    }

    private fun openGify() {
        gifsDialog.show(supportFragmentManager, "gifs_dialog")
        gifsDialog.gifSelectionListener = this
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

    private fun validateAndSubmit() {
        amountCryptoView.error = null
        phoneView.error = null


        val phoneStrng = phonePickerView.formattedFullNumber
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")


        var errors = 0

        //Validate amount
        if (cryptoBalanceToSend <= 0) {
            errors++
            amountCryptoView.error = getString(R.string.should_be_filled)
        }

        //Validate max amount
        if (cryptoBalanceToSend > (mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId))) {
            errors++
            amountCryptoView.error = "Not enough balance"
        }

        //Validate amount
        if (!phonePickerView.isValidFullNumber) {
            errors++
            showError("Wrong phone number")
        }

        if (errors == 0) {
            mPresenter.createTransaction(
                this,
                mCoin,
                phoneStrng,
                cryptoBalanceToSend,
                messageView.getString()
            )
        }

    }

    override fun onTransactionDone() {
        showMessage("Transaction Done")
        finish()
    }

    override fun openSmsCodeDialog(error: String?) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        val smsCode = view.findViewById<AppCompatEditText>(R.id.sms_code)
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next)
            { _, _ ->
                val code = smsCode.text.toString()
                if (code.length != 4) {
                    openSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    mPresenter.completeTransaction(code)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setView(view)
            .create()
            .show()
        val tilSmsCode = view.findViewById<TextInputLayout>(R.id.til_sms_code)
        tilSmsCode.error = error
    }

    override fun onDismissed() {

    }

    override fun onGifSelected(media: Media) {
        mPresenter.gifMedia = media
        gifView.visibility = View.VISIBLE
        gifView.setMedia(media, RenditionType.original)
        gifEmptyView.visibility = View.INVISIBLE
    }

    private val coinFromTextWatcher = object : TextWatcher {
        val dotChar: Char = '.'
        var isRunning = false
        var isDeleting = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            isDeleting = count > after
        }

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun afterTextChanged(editable: Editable) {
            if (isRunning) return

            isRunning = true

            when {
                editable.isNotEmpty() && editable.first() == DOT_CHAR -> editable.insert(0, "0")
                editable.isNotEmpty() && editable.last() == DOT_CHAR && editable.count { it == DOT_CHAR } > 1 -> editable.delete(
                    editable.lastIndex,
                    editable.length
                )
                editable.contains(DOT_CHAR) && (editable.lastIndex - editable.indexOf(DOT_CHAR)) > MAX_CHARS_AFTER_DOT -> editable.delete(
                    editable.lastIndex - 1,
                    editable.lastIndex
                )
                editable.isEmpty() || editable.toString().replace(DOT_CHAR.toString(), "").toInt() <= 0 -> {
                    val isContainsDot = editable.contains(DOT_CHAR)
                    val indexOfDot = editable.indexOf(DOT_CHAR)
                    when {
                        isContainsDot && indexOfDot > 1 -> editable.delete(0, indexOfDot - 1)
                        !isContainsDot && editable.length > 1 -> editable.delete(0, editable.length - 1)
                    }
                    amountUsdView.clearText()
                }
                else -> {
                    val balanceCoin = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)
                    val fromCoinTemporaryValue = amountCryptoView.getString().toDouble()
                    val fromCoinAmount: Double =
                        if (fromCoinTemporaryValue > balanceCoin) balanceCoin
                        else fromCoinTemporaryValue

                    if (fromCoinTemporaryValue > balanceCoin) {
                        editable.clear()
                        editable.insert(0, fromCoinAmount.toStringCoin())
                    }
                    amountUsdView.setText((fromCoinAmount * mCoin.price.uSD).toStringUsd())
                    cryptoBalanceToSend = fromCoinAmount
                }
            }

            isRunning = false
        }
    }

    companion object {
        private const val KEY_COIN = "KEY_COIN"
        const val MAX_CHARS_AFTER_DOT = 6
        const val DOT_CHAR: Char = '.'

        @JvmStatic
        fun start(context: Context?, coin: CoinModel) {
            val intent = Intent(context, SendGiftActivity::class.java)
            intent.putExtra(KEY_COIN, Parcels.wrap(coin))
            context?.startActivity(intent)
        }
    }
}
