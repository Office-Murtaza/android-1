package com.app.belcobtm.ui.main.coins.sell

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.app.belcobtm.R
import com.app.belcobtm.domain.transaction.item.SellLimitsDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.QRUtils
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.giphy.sdk.ui.GiphyCoreUI
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_sell.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class SellActivity : BaseMvpActivity<SellContract.View, SellContract.Presenter>(), SellContract.View {
    private var alertDialog: AlertDialog? = null
    private var limits: SellLimitsDataItem? = null

    private lateinit var coinDataItem: CoinDataItem

    override fun showErrorAndHideDialogs(resError: Int) {
        showError(resError)
        alertDialog?.dismiss()
    }

    override fun showNewBalanceError() {
        showError("coin stock value has been changed")
        alertDialog?.dismiss()
    }

    override fun showPretransactionError() {
        showError("the transaction can not be created")
        alertDialog?.dismiss()
    }

    override fun showLimits(limitsItem: SellLimitsDataItem) {
        this.limits = limitsItem
        dayLimitView.text = getString(R.string.transaction_price_usd, limitsItem.usdDailyLimit.toStringUsd())
        txLimitView.text = getString(R.string.transaction_price_usd, limitsItem.usdTxLimit.toStringUsd())
    }

    companion object {
        private const val KEY_COIN = "KEY_COIN"

        @JvmStatic
        fun start(context: Context?, coin: CoinDataItem?) {
            val intent = Intent(context, SellActivity::class.java)
            intent.putExtra(KEY_COIN, coin)
            context?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GiphyCoreUI.configure(this, GIPHY_API_KEY)

        setContentView(R.layout.activity_sell)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        coinDataItem = intent.getParcelableExtra(KEY_COIN) as CoinDataItem
        supportActionBar?.title = "Sell" + " " + coinDataItem.code

        initListeners()
        initViews()

        mPresenter.bindData(coinDataItem)
        mPresenter.getDetails()
    }

    private fun initListeners() {
        amountUsdView?.editText?.addTextChangedListener(coinFromTextWatcher)
        maxUsdView.setOnClickListener { selectMaxPrice() }
        amountUsdView.actionDoneListener { validateAndSubmit() }
        nextButtonView.setOnClickListener { validateAndSubmit() }
        doneButtonView.setOnClickListener { finish() }
    }

    private val coinFromTextWatcher = object : TextWatcher {
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
                editable.isEmpty() -> amountCryptoView.clearText()
                editable.toString().toInt() == 0 -> {
                    editable.clear()
                    editable.insert(0, 0.toString())
                    amountCryptoView.setText(editable.toString())
                }
                else -> {
                    val temporaryUsdAmount = amountUsdView.getString().toInt()
                    val usdBalance = coinDataItem.balanceUsd.toInt()
                    val usdAmount: Int = if (temporaryUsdAmount >= usdBalance) usdBalance else temporaryUsdAmount

                    if (!checkNotesForATM(usdAmount)) {
                        amountUsdView.showError(R.string.sell_screen_atm_contains_only_count_banknotes)
                    } else {
                        amountUsdView.clearError()
                    }
                    val price = coinDataItem.priceUsd
                    val rate = limits?.profitRate ?: Double.MIN_VALUE
                    var cryptoAmount = (usdAmount / price * rate)
                    cryptoAmount = round(cryptoAmount * 100000) / 100000

                    editable.clear()
                    editable.insert(0, usdAmount.toString())
                    amountCryptoView.setText(cryptoAmount.toStringCoin())
                }
            }

            isRunning = false
        }
    }

    private fun initViews() {
        sellContainerGroupView.visibility = View.VISIBLE
        amountCryptoView.hint = getString(R.string.sell_screen_crypto_amount, coinDataItem.code)
        initPrice()
        initBalance()
    }

    private fun initPrice() {
        priceUsdView.text = getString(R.string.transaction_price_usd, coinDataItem.priceUsd.toStringUsd())
    }

    private fun initBalance() {
        balanceCryptoView.text =
            getString(R.string.transaction_crypto_balance, coinDataItem.balanceCoin.toStringCoin(), coinDataItem.code)
        balanceUsdView.text = getString(R.string.transaction_price_usd, coinDataItem.balanceUsd.toStringUsd())
    }

    private fun selectMaxPrice() {
        val price = coinDataItem.priceUsd
        val rate = limits?.profitRate ?: Double.MIN_VALUE
        val balance = coinDataItem.balanceCoin - mPresenter.getTransactionFee(coinDataItem.code)

        val fiatAmount = try {
            ((balance * price / rate).toInt() / 10 * 10)
        } catch (e: Exception) {
            0
        }

        val mult20 = if (fiatAmount % 20 != 0) {
            (fiatAmount / 20) * 20
        } else fiatAmount

        val mult50 = if (fiatAmount % 50 != 0) {
            (fiatAmount / 50) * 50
        } else fiatAmount

        val fiatMax = max(mult20, mult50)
        amountUsdView.setText("$fiatMax")

        if (!anotherAddressButtonView.isChecked) {
            amountUsdView.setText("${max(mult20, mult50)}")

        } else {
            amountUsdView.setText(
                "${min(
                    limits?.usdDailyLimit?.toInt() ?: 0,
                    limits?.usdTxLimit?.toInt() ?: 0
                )}"
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun validateAndSubmit() {
        amountUsdView.clearError()

        val fiatAmount = try {
            amountUsdView.getString().toInt()
        } catch (e: Exception) {
            0
        }

        var errors = 0

        //Validate amount
        if (fiatAmount <= 0) {
            errors++
            amountUsdView.showError(R.string.should_be_filled)
        }

        // if (fiatAmount % 20 != 0 && fiatAmount % 50 != 0) {
        if (!checkNotesForATM(fiatAmount)) {
            errors++
            amountUsdView.showError(R.string.sell_screen_atm_contains_only_count_banknotes)
        }

        val balance = coinDataItem.balanceCoin - mPresenter.getTransactionFee(coinDataItem.code)
        val price = coinDataItem.priceUsd
        val rate = limits?.profitRate ?: Double.MIN_VALUE

        var cryptoAmount = fiatAmount / price * rate
        cryptoAmount = round(cryptoAmount * 100000) / 100000


        if (balance < cryptoAmount && !anotherAddressButtonView.isChecked) {
            errors++
            amountCryptoView.showError(R.string.sell_screen_not_enough_tradable_balance)
        }

        if (errors == 0) {
            mPresenter.preSubmit(fiatAmount, cryptoAmount, balance, anotherAddressButtonView.isChecked)
        }
    }

    /**
    Alternative way (banknotes limit)
    var isValid = false
    for( x in 1..40){
    if((sum - 20 * x) % 50 == 0){
    isValid = true
    }
    }
    return isValid
     **/

    fun checkNotesForATM(sum: Int): Boolean {
        val nearestNumberThatCanBeGivenByTwentyAndFifty = when {
            sum < 20 -> 0
            sum < 40 -> 20
            else -> sum
        } / 10 * 10

        return (nearestNumberThatCanBeGivenByTwentyAndFifty == sum)
    }

    override fun showDoneScreen() {
        alertDialog?.dismiss()
        sellContainerGroupView.hide()
        anotherAddressGroupView.show()
    }

    override fun showDoneScreenAnotherAddress(addressDestination: String?, cryptoAmount: Double) {
        alertDialog?.dismiss()
        sellContainerGroupView.hide()
        anotherAddressGroupView.show()
        qrCodeContainerView.show()
        amountView.show()

        addressView.text = addressDestination
        amountView.text =
            getString(R.string.transaction_crypto_balance, cryptoAmount.toStringCoin(), coinDataItem.code)
        imageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = imageView.layoutParams
                val imageSize = imageView.width
                params.height = imageSize
                imageView.layoutParams = params
                imageView.setImageBitmap(QRUtils.getSpacelessQR(addressDestination ?: "", imageSize, imageSize))
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        copyAddressButtonView.setOnClickListener {
            copyToClipboard(addressDestination ?: "", addressDestination ?: "")
            AlertHelper.showToastLong(applicationContext, R.string.alert_copy_to_clipboard)
        }
    }

    @SuppressLint("InflateParams")
    override fun openSmsCodeDialog(error: String?) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        val smsCode = view.findViewById<AppCompatEditText>(R.id.sms_code)

        this.alertDialog = AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next)
            { _, _ ->
                val code = smsCode.text.toString()
                if (code.length != 4) {
                    openSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    mPresenter.verifySmsCode(code)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setView(view)
            .create()


        alertDialog?.show()
        val tilSmsCode = view.findViewById<TextInputLayout>(R.id.til_sms_code)
        tilSmsCode.error = error
    }

}
