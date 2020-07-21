package com.app.belcobtm.presentation.features.wallet.trade.details

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.app.belcobtm.presentation.features.HostActivity
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import kotlinx.android.synthetic.main.activity_trade_details.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

class TradeDetailsBuyActivity : BaseActivity() {
    private lateinit var tradeDetailsItem: TradeDetailsItem.BuySell
    private val viewModel: TradeDetailsBuyViewModel by viewModel {
        parametersOf(intent.getStringExtra(TAG_COIN_CODE))
    }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val maxLimit: Int = tradeDetailsItem.maxLimit
            val minLimit: Int = tradeDetailsItem.minLimit
            val cryptoAmount = it.getDouble()
            val usdAmount = (viewModel.fromCoinItem.priceUsd * it.getDouble()).roundToInt()

            when {
                cryptoAmount == 0.0 || usdAmount < minLimit -> {
                    amountUsdView.clearText()
                    sendButtonView.isEnabled = false
                }
                usdAmount > maxLimit -> {
                    it.clear()
                    it.insert(0, (maxLimit / viewModel.fromCoinItem.priceUsd).toStringCoin())
                    amountUsdView.setText(maxLimit.toString())
                    sendButtonView.isEnabled = true
                }
                else -> {
                    amountUsdView.setText(usdAmount.toString())
                    sendButtonView.isEnabled = true
                }
            }
        },
        secondTextWatcher = {
            val maxLimit = tradeDetailsItem.maxLimit
            val minLimit = tradeDetailsItem.minLimit
            val usdAmountText = it.getInt()
            when {
                usdAmountText == 0 || usdAmountText < minLimit -> {
                    amountCryptoView.clearText()
                    sendButtonView.isEnabled = false
                }
                usdAmountText > maxLimit -> {
                    it.clear()
                    it.insert(0, maxLimit.toString())
                    amountCryptoView.setText((maxLimit / viewModel.fromCoinItem.priceUsd).toStringCoin())
                    sendButtonView.isEnabled = true
                }
                else -> {
                    amountCryptoView.setText((usdAmountText / viewModel.fromCoinItem.priceUsd).toStringCoin())
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
                        is Failure.TokenError -> {
                            val intent = Intent(this, HostActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
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
        tradeDetailsItem = intent.getParcelableExtra(TAG_TRADE_DETAILS_ITEM)!!

        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (tradeDetailsItem.isBuyType) {
            getString(R.string.trade_screen_title_buy, intent.getStringExtra(TAG_COIN_CODE))
        } else {
            getString(R.string.trade_screen_title_sell, intent.getStringExtra(TAG_COIN_CODE))
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
        amountCryptoView.hint = getString(R.string.crypto_amount, intent.getStringExtra(TAG_COIN_CODE))
    }

    companion object {
        const val TAG_COIN_CODE = "tag_coin_code"
        const val TAG_TRADE_DETAILS_ITEM = "tag_trade_details_item"
    }
}