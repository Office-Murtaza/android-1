package com.app.belcobtm.presentation.features.wallet.trade.details

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import kotlinx.android.synthetic.main.activity_trade_details.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeDetailsBuyActivity : BaseActivity() {
    private lateinit var tradeDetailsItem: TradeDetailsItem.Buy
    private val viewModel: TradeDetailsBuyViewModel by viewModel {
        parametersOf(intent.getParcelableExtra(TAG_COIN_ITEM))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_details)
        initListeners()
        initObservers()
        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initListeners() {
        maxUsdView.setOnClickListener {
            val balance = tradeDetailsItem.maxLimit
            amountUsdView.setText(balance.toString())
        }
        sendButtonView.setOnClickListener {
            viewModel.buy(
                tradeDetailsItem.id,
                tradeDetailsItem.price,
                amountUsdView.getString().toInt(),
                amountCryptoView.getString().toDouble(),
                tradeDetailsView.getString()
            )
        }
        amountUsdView.editText?.addTextChangedListener(coinFromTextWatcher)
        amountCryptoView?.editText?.keyListener = null
    }

    private fun initObservers() {
        viewModel.buyLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    finish()
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.TokenError -> startActivity(Intent(this, PinActivity::class.java))
                        is Failure.MessageError -> showError(it.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
    }

    private fun initViews() {
        val intentCoinItem = intent.getParcelableExtra<IntentCoinItem>(TAG_COIN_ITEM)!!
        tradeDetailsItem = intent.getParcelableExtra(TAG_TRADE_DETAILS_ITEM)!!

        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.trade_screen_title_buy, intentCoinItem.coinCode)

        priceUsdView.text = getString(R.string.unit_usd_dynamic, tradeDetailsItem.price.toStringUsd())
        userView.text = getString(
            R.string.trade_screen_user_field,
            tradeDetailsItem.userName,
            tradeDetailsItem.tradeCount,
            tradeDetailsItem.rate,
            tradeDetailsItem.distance
        )
        paymentMethodView.text = tradeDetailsItem.paymentMethod
        limitsView.text =
            getString(R.string.unit_usd_dynamic, "${tradeDetailsItem.minLimit} - ${tradeDetailsItem.maxLimit}")
        termsView.setText(tradeDetailsItem.terms)
        amountUsdView.setText(tradeDetailsItem.minLimit.toString())
        amountCryptoView.hint = getString(R.string.crypto_amount, intentCoinItem.coinCode)
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
                editable.isNotEmpty() && editable.first() == DOT_CHAR -> editable.insert(0, "0")
                editable.isNotEmpty() && editable.last() == DOT_CHAR && editable.count { it == DOT_CHAR } > 1 ->
                    editable.delete(editable.lastIndex, editable.length)
                editable.contains(DOT_CHAR) && (editable.lastIndex - editable.indexOf(DOT_CHAR)) > MAX_CHARS_AFTER_DOT ->
                    editable.delete(editable.lastIndex - 1, editable.lastIndex)
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
                    val maxLimit = tradeDetailsItem.maxLimit
                    val minLimit = tradeDetailsItem.minLimit
                    val usdAmountTemporary = amountUsdView.getString().toInt()
                    when {
                        usdAmountTemporary > maxLimit -> {
                            editable.clear()
                            editable.insert(0, maxLimit.toString())
                            amountCryptoView.setText((maxLimit / viewModel.fromCoinItem.priceUsd).toString())
                            sendButtonView.isEnabled = true
                        }
                        usdAmountTemporary < minLimit -> {
                            amountCryptoView.setText(0.0.toStringCoin())
                            sendButtonView.isEnabled = false
                        }
                        else -> {
                            amountCryptoView.setText((usdAmountTemporary / viewModel.fromCoinItem.priceUsd).toStringUsd())
                            sendButtonView.isEnabled = true
                        }
                    }
                }
            }

            isRunning = false
        }
    }

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
        const val TAG_TRADE_DETAILS_ITEM = "tag_trade_details_item"
        const val MAX_CHARS_AFTER_DOT = 6
        const val DOT_CHAR: Char = '.'
    }
}