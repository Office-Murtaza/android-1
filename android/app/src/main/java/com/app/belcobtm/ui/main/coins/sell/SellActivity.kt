package com.app.belcobtm.ui.main.coins.sell

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.LimitsResponse
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.QRUtils
import com.app.belcobtm.presentation.core.extensions.*
import com.giphy.sdk.ui.GiphyCoreUI
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_sell.*
import org.parceler.Parcels
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class SellActivity : BaseMvpActivity<SellContract.View, SellContract.Presenter>(),
    SellContract.View {
    override fun showErrorAndHideDialogs(errorMessage: String?) {
        showError(errorMessage)
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

    override fun showLimits(resp: LimitsResponse?) {
        this.limits = resp
        dayLimitView.text = """${String.format("%.2f", resp?.dailyLimit?.USD)} USD"""
        txLimitView.text = """${String.format("%.2f", resp?.txLimit?.USD)} USD"""
    }

    companion object {
        private const val KEY_COIN = "KEY_COIN"

        @JvmStatic
        fun start(context: Context?, coin: CoinModel) {
            val intent = Intent(context, SellActivity::class.java)
            intent.putExtra(KEY_COIN, Parcels.wrap(coin))
            context?.startActivity(intent)
        }
    }

    private var alertDialog: AlertDialog? = null
    private var limits: LimitsResponse? = null
    private lateinit var gifsDialog: GiphyDialogFragment

    private lateinit var mCoin: CoinModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GiphyCoreUI.configure(this, GIPHY_API_KEY)

        setContentView(R.layout.activity_sell)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mCoin = Parcels.unwrap(intent.getParcelableExtra(KEY_COIN))
        supportActionBar?.title = "Sell" + " " + mCoin.coinId

        initListeners()
        initViews()

        mPresenter.bindData(mCoin)
        mPresenter.getDetails()
    }

    private fun initListeners() {
        amountUsdView?.editText?.addTextChangedListener(coinFromTextWatcher)
        maxUsdView.setOnClickListener { selectMaxPrice() }
        amountUsdView.actionDoneListener { validateAndSubmit() }
        nextButtonView.setOnClickListener { validateAndSubmit() }
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
                    amountCryptoView.clearText()
                }
                else -> {
                    val usdAmount: Int =
                        if (amountUsdView.getString().toInt() > mCoin.price.uSD) mCoin.price.uSD.toInt()
                        else amountUsdView.getString().toInt()

                    if (!checkNotesForATM(usdAmount)) {
                        amountUsdView.showError(R.string.sell_screen_atm_contains_only_count_banknotes)
                    } else {
                        amountUsdView.clearError()
                    }
                    val price = mCoin.price.uSD
                    val rate = limits?.sellProfitRate ?: Double.MIN_VALUE
                    var cryptoAmount = (usdAmount / price * rate)
                    cryptoAmount = round(cryptoAmount * 100000) / 100000

                    editable.clear()
                    editable.insert(0, usdAmount.toString())
                    amountCryptoView.setText(String.format("%.6f", cryptoAmount).trimEnd('0'))
                }
            }

            isRunning = false
        }
    }

    private fun initViews() {
        sellContainerGroupView.visibility = View.VISIBLE
        resultContainer.visibility = View.GONE
        amountCryptoView.hint = getString(R.string.sell_screen_crypto_amount, mCoin.coinId)
        initPrice()
        initBalance()
    }

    private fun initPrice() {
        val convertedPrice = if (mCoin.price.uSD > 0) String.format("%.2f", mCoin.price.uSD).trimEnd('0') else "0"
        priceUsdView.text = getString(R.string.transaction_price_usd, convertedPrice)
    }

    private fun initBalance() {
        val convertedBalance = if (mCoin.balance > 0) String.format("%.6f", mCoin.balance).trimEnd('0') else "0"
        balanceCryptoView.text = getString(R.string.transaction_crypto_balance, convertedBalance, mCoin.coinId)

        val amountUsd = mCoin.balance * mCoin.price.uSD
        balanceUsdView.text = "${String.format("%.2f", amountUsd)} USD"
    }

    private fun selectMaxPrice() {
        val price = mCoin.price.uSD
        val rate = limits?.sellProfitRate ?: Double.MIN_VALUE
        val balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)

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
                    limits?.dailyLimit?.USD?.toInt() ?: 0,
                    limits?.txLimit?.USD?.toInt() ?: 0
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

    private fun getTextFromClipboard(): String {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text.toString()
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

        val balance = mCoin.balance - mPresenter.getTransactionFee(mCoin.coinId)

        val price = mCoin.price.uSD

        val rate = limits?.sellProfitRate ?: Double.MIN_VALUE

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

        var nearestNumberThatCanBeGivenByTwentyAndFifty = sum

        if (sum >= 20)
        else {
            nearestNumberThatCanBeGivenByTwentyAndFifty = 0
        }

        if (sum >= 40)
        else {
            nearestNumberThatCanBeGivenByTwentyAndFifty = 20
        }

        nearestNumberThatCanBeGivenByTwentyAndFifty =
            (nearestNumberThatCanBeGivenByTwentyAndFifty / 10 * 10)

        return (nearestNumberThatCanBeGivenByTwentyAndFifty == sum)

    }

    override fun onTransactionDone(
        anotherAddress: Boolean,
        addressDestination: String?,
        cryptoResultAmount: Double
    ) {
        alertDialog?.dismiss()

        sellContainerGroupView.visibility = View.GONE
        resultContainer.visibility = View.VISIBLE

        doneBtn.setOnClickListener {

            finish()
        }

        if (anotherAddress) {
            resultOwnContainer.visibility = View.GONE
            resultAnotherAddressContainer.visibility = View.VISIBLE
            addressTv.text = addressDestination
            amountTv.text = "${String.format("%.6f", cryptoResultAmount)} ${mCoin.coinId}"

            copyBtn.setOnClickListener {
                copyToClipboard(addressDestination ?: "", addressDestination ?: "")
            }

            /*  val walletQrCode =
                  BarcodeEncoder().encodeBitmap(addressDestination, BarcodeFormat.QR_CODE, 200, 200)
              qrCodeIv?.setImageBitmap(walletQrCode)
              */
            qrCodeIv?.setImageBitmap(QRUtils.getSpacelessQR(addressDestination ?: "", 200, 200))


        } else {
            resultOwnContainer.visibility = View.VISIBLE
            resultAnotherAddressContainer.visibility = View.GONE

        }
    }

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
