package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_exchange_coin_to_coin.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ExchangeCoinToCoinActivity : BaseActivity() {
    private val viewModel: ExchangeCoinToCoinViewModel by viewModel {
        parametersOf(
            intent.getParcelableExtra(TAG_COIN_ITEM),
            intent.getParcelableArrayListExtra<IntentCoinItem>(TAG_COIN_ITEM_LIST)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_coin_to_coin)
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
        pickCoinButtonView.editText?.keyListener = null
        pickCoinButtonView.editText?.setOnClickListener {
            val coinList = viewModel.getCoinTypeList()
            MaterialAlertDialogBuilder(pickCoinButtonView.context)
                .setTitle(R.string.exchange_coin_to_coin_screen_select_coin)
                .setItems(coinList.map { it.verboseValue() }.toTypedArray()) { _, which ->
                    pickCoinButtonView.setText(coinList[which].verboseValue())
                    amountCoinToView.hint = getString(
                        R.string.exchange_coin_to_coin_screen_crypto_amount,
                        coinList[which].code()
                    )
                    viewModel.toCoinItem = viewModel.coinItemList.find { it.coinCode == coinList[which].code() }
                    amountCoinFromView?.editText?.setText(amountCoinFromView.getString())
                }
                .create()
                .show()
        }
        amountCoinFromView?.editText?.addTextChangedListener(coinFromTextWatcher)
        amountCoinToView?.editText?.afterTextChanged {
            nextButtonView.isEnabled = it.isNotEmpty() && it.toString().toDouble() > 0
        }
        maxCoinFromView.setOnClickListener {
            amountCoinFromView.setText(viewModel.fromCoinItem.balanceCoin.toStringCoin())
        }
        nextButtonView.setOnClickListener {
            viewModel.exchange(amountCoinFromView.getString().toDouble())
        }
    }

    private fun initObservers() {
        viewModel.exchangeLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    progressView.hide()
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        priceUsdView.text = getString(
            R.string.exchange_coin_to_coin_screen_price_value,
            viewModel.fromCoinItem.priceUsd.toStringUsd()
        )
        balanceCryptoView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_crypto,
            viewModel.fromCoinItem.balanceCoin.toStringCoin(),
            viewModel.fromCoinItem.coinCode
        )
        balanceUsdView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_usd,
            viewModel.fromCoinItem.balanceUsd.toStringUsd()
        )
        amountCoinFromView.hint = getString(
            R.string.exchange_coin_to_coin_screen_crypto_amount,
            viewModel.fromCoinItem.coinCode
        )
        amountCoinToView.hint = getString(
            R.string.exchange_coin_to_coin_screen_crypto_amount,
            viewModel.toCoinItem?.coinCode ?: ""
        )
        pickCoinButtonView.setText(
            viewModel.getCoinTypeList().find {
                it.code() == viewModel.toCoinItem?.coinCode
            }?.verboseValue() ?: ""
        )
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
                editable.isEmpty() || editable.toString().replace(
                    dotChar.toString(),
                    ""
                ).toInt() <= 0 -> {
                    when {
                        editable.contains(dotChar) && editable.indexOf(dotChar, 0, false) > 1 ->
                            editable.delete(0, editable.indexOf(dotChar, 0, false) - 1)
                        !editable.contains(dotChar) && editable.length > 1 -> editable.delete(0, editable.length - 1)
                    }

                    amountCoinToView.clearText()
                }
                editable.first() == dotChar -> editable.insert(0, "0")
                editable.last() == dotChar && editable.count { it == dotChar } > 1 -> editable.delete(
                    editable.lastIndex,
                    editable.length
                )
                else -> {
                    val fromCoinTemporaryValue = amountCoinFromView.getString().toDouble()
                    val fromCoinAmount: Double =
                        if (fromCoinTemporaryValue > viewModel.fromCoinItem.balanceCoin) viewModel.fromCoinItem.balanceCoin
                        else fromCoinTemporaryValue
                    val toCoinRefPrice = viewModel.toCoinItem?.priceUsd ?: 0.0
                    editable.clear()
                    if (toCoinRefPrice <= 0) {
                        editable.insert(0, toCoinRefPrice.toStringCoin())
                        amountCoinToView.setText(toCoinRefPrice.toStringCoin())
                    } else {
                        val fromCoinPrice = viewModel.fromCoinItem.priceUsd
                        val fromCoinProfitC2c = viewModel.coinFeeItem.profitC2C
                        val toCoinAmount =
                            (fromCoinAmount * fromCoinPrice) / toCoinRefPrice * (100 - fromCoinProfitC2c) / 100
                        editable.insert(0, fromCoinAmount.toStringCoin())
                        amountCoinToView.setText(toCoinAmount.toStringCoin())
                    }
                }
            }

            isRunning = false
        }
    }

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
        const val TAG_COIN_ITEM_LIST = "tag_coin_item_list"
    }
}