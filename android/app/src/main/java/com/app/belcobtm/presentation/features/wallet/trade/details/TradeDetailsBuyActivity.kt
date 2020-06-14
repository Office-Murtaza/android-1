package com.app.belcobtm.presentation.features.wallet.trade.details

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import kotlinx.android.synthetic.main.activity_trade_details.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeDetailsBuyActivity : BaseActivity() {
    private lateinit var tradeDetailsItem: TradeDetailsItem.BuySell
    private val viewModel: TradeDetailsBuyViewModel by viewModel {
        parametersOf(intent.getParcelableExtra(TAG_COIN_ITEM))
    }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val maxLimit = tradeDetailsItem.maxLimit
            val minLimit = tradeDetailsItem.minLimit
            val cryptoAmount = if (it.toString().replace(".", "").isEmpty()) {
                0.0
            } else {
                it.toString().toDouble()
            }
            val usdAmountTemporary = (viewModel.fromCoinItem.priceUsd * cryptoAmount).toInt()

            when {
                usdAmountTemporary > maxLimit -> {
                    it.clear()
                    it.insert(0, (maxLimit / viewModel.fromCoinItem.priceUsd).toStringCoin())
                    amountUsdView.setText(maxLimit.toString())
                    sendButtonView.isEnabled = true
                }
                usdAmountTemporary < minLimit -> {
                    amountUsdView.clearText()
                    sendButtonView.isEnabled = false
                }
                else -> {
                    amountUsdView.setText(usdAmountTemporary.toString())
                    sendButtonView.isEnabled = true
                }
            }
        },
        secondTextWatcher = {
            val maxLimit = tradeDetailsItem.maxLimit
            val minLimit = tradeDetailsItem.minLimit
            val usdAmountText = it.toString().replace(".", "")
            when {
                usdAmountText.isEmpty() -> {
                    amountCryptoView.setText((usdAmountText.toInt() / viewModel.fromCoinItem.priceUsd).toStringCoin())
                    sendButtonView.isEnabled = false
                }
                usdAmountText.toInt() > maxLimit -> {
                    it.clear()
                    it.insert(0, maxLimit.toString())
                    amountCryptoView.setText((maxLimit / viewModel.fromCoinItem.priceUsd).toStringCoin())
                    sendButtonView.isEnabled = true
                }
                usdAmountText.toInt() < minLimit -> {
                    amountCryptoView.clearText()
                    sendButtonView.isEnabled = false
                }
                else -> {
                    amountCryptoView.setText((usdAmountText.toInt() / viewModel.fromCoinItem.priceUsd).toStringCoin())
                    sendButtonView.isEnabled = true
                }
            }
        }
    )

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
        maxCryptoView.setOnClickListener {
            val balance = tradeDetailsItem.maxLimit
            amountUsdView.setText(balance.toString())
        }
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
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountUsdView.editText?.addTextChangedListener(doubleTextWatcher.secondTextWatcher)
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
        val intentCoinItem = intent.getParcelableExtra<CoinDataItem>(TAG_COIN_ITEM)!!
        tradeDetailsItem = intent.getParcelableExtra(TAG_TRADE_DETAILS_ITEM)!!

        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (tradeDetailsItem.isBuyType) {
            getString(R.string.trade_screen_title_buy, intentCoinItem.code)
        } else {
            getString(R.string.trade_screen_title_sell, intentCoinItem.code)
        }

        priceUsdView.text = getString(R.string.unit_usd_dynamic, tradeDetailsItem.price.toStringUsd())
        userView.text = getString(
            R.string.trade_screen_user_field_ln,
            tradeDetailsItem.userName,
            tradeDetailsItem.tradeCount,
            tradeDetailsItem.rate.toStringUsd(),
            tradeDetailsItem.distance
        )
        paymentMethodView.text = tradeDetailsItem.paymentMethod
        limitsView.text =
            getString(R.string.unit_usd_dynamic, "${tradeDetailsItem.minLimit} - ${tradeDetailsItem.maxLimit}")
        termsView.text = tradeDetailsItem.terms
        amountCryptoView.hint = getString(R.string.crypto_amount, intentCoinItem.code)
    }

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
        const val TAG_TRADE_DETAILS_ITEM = "tag_trade_details_item"
    }
}