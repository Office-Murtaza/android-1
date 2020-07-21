package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.ui.SmsDialogFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.app.belcobtm.presentation.features.HostActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_exchange_coin_to_coin.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ExchangeCoinToCoinActivity : BaseActivity() {
    private val viewModel: ExchangeCoinToCoinViewModel by viewModel {
        parametersOf(
            intent.getParcelableExtra(TAG_COIN_ITEM),
            intent.getParcelableArrayListExtra<CoinDataItem>(TAG_COIN_ITEM_LIST)
        )
    }

    private fun getExchangeValue(): Double {
        val fromCoinPrice = viewModel.fromCoinItem.priceUsd
        val fromCoinProfitC2c = viewModel.fromCoinFeeItem.profitExchange
        val toCoinRefPrice = viewModel.toCoinItem?.priceUsd ?: 0.0
        return fromCoinPrice / toCoinRefPrice * (100 - fromCoinProfitC2c) / 100
    }

    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        firstTextWatcher = { editable ->
            val fromCoinAmountTemporary = editable.getDouble()
            val fromCoinAmount: Double =
                if (fromCoinAmountTemporary > viewModel.fromCoinItem.balanceCoin) viewModel.fromCoinItem.balanceCoin
                else fromCoinAmountTemporary
            val toCoinAmount = fromCoinAmount * getExchangeValue()
            val fromMaxValue = getMaxValueFromCoin()

            if (fromCoinAmountTemporary > fromMaxValue) {
                editable.clear()
                editable.insert(0, fromMaxValue.toStringCoin())
            }

            if (fromCoinAmountTemporary > 0) {
                amountCoinToView.setText(toCoinAmount.toStringCoin())
                nextButtonView.isEnabled = true
            } else {
                amountCoinToView.clearText()
                nextButtonView.isEnabled = false
            }
        },
        secondTextWatcher = { editable ->
            val toCoinAmountMaxValue = viewModel.fromCoinItem.balanceCoin * getExchangeValue()
            val toCoinAmountTemporary = editable.getDouble()
            val toCoinAmount =
                if (toCoinAmountTemporary > toCoinAmountMaxValue) viewModel.fromCoinItem.balanceCoin
                else toCoinAmountTemporary
            val fromCoinAmount =
                if (toCoinAmountTemporary > toCoinAmountMaxValue) getMaxValueFromCoin()
                else toCoinAmount / getExchangeValue()
            val toCoinMaxValue = getMaxValueToCoin()

            if (toCoinAmountTemporary > toCoinMaxValue) {
                editable.clear()
                editable.insert(0, toCoinMaxValue.toStringCoin())
            }

            if (toCoinAmountTemporary > 0) {
                amountCoinFromView.setText(fromCoinAmount.toStringCoin())
                nextButtonView.isEnabled = true
            } else {
                amountCoinFromView.clearText()
                nextButtonView.isEnabled = false
            }
        }
    )

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

    private fun getMaxValueFromCoin(): Double = viewModel.fromCoinItem.balanceCoin - viewModel.fromCoinFeeItem.txFee

    private fun getMaxValueToCoin(): Double = getMaxValueFromCoin() * getExchangeValue()

    private fun initListeners() {
        pickCoinButtonView.editText?.keyListener = null
        pickCoinButtonView.editText?.setOnClickListener {
            val coinAdapter = CoinDialogAdapter(pickCoinButtonView.context)
            MaterialAlertDialogBuilder(pickCoinButtonView.context)
                .setTitle(R.string.exchange_coin_to_coin_screen_select_coin)
                .setAdapter(coinAdapter) { _, which ->
                    pickCoinButtonView.setText(LocalCoinType.values()[which].fullName)
                    pickCoinButtonView.setDrawableStartEnd(
                        LocalCoinType.values()[which].resIcon(),
                        R.drawable.ic_arrow_drop_down
                    )
                    amountCoinToView.hint = getString(
                        R.string.exchange_coin_to_coin_screen_crypto_amount,
                        LocalCoinType.values()[which].name
                    )
                    viewModel.toCoinItem = viewModel.coinItemList.find { it.code == LocalCoinType.values()[which].name }
                    viewModel.toCoinFeeItem = viewModel.coinFeeItemList[LocalCoinType.values()[which].name]
                    amountCoinFromView?.editText?.setText(amountCoinFromView.getString())
                }
                .create()
                .show()
        }
        amountCoinFromView?.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountCoinToView?.editText?.addTextChangedListener(doubleTextWatcher.secondTextWatcher)
        maxCoinFromView.setOnClickListener { amountCoinFromView.setText(getMaxValueFromCoin().toStringCoin()) }
        maxCoinToView.setOnClickListener { amountCoinToView.setText(getMaxValueToCoin().toStringCoin()) }
        nextButtonView.setOnClickListener {
            val amount = amountCoinFromView.getString().toDouble() + viewModel.fromCoinFeeItem.txFee
            viewModel.createTransaction(amount)
        }
    }

    private fun initObservers() {
        viewModel.exchangeLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    when (it.data) {
                        ExchangeCoinToCoinViewModel.TRANSACTION_CREATED -> showSmsDialog()
                        ExchangeCoinToCoinViewModel.TRANSACTION_EXCHANGED -> finish()
                    }
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.TokenError -> {
                            val intent = Intent(this, HostActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        is Failure.MessageError -> if (it.data == ExchangeCoinToCoinViewModel.TRANSACTION_CREATED) {
                            showError(it.errorType.message)
                        } else {
                            showSmsDialog(it.errorType.message)
                        }
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
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
            viewModel.fromCoinItem.code
        )
        balanceUsdView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_usd,
            viewModel.fromCoinItem.balanceUsd.toStringUsd()
        )
        amountCoinFromView.hint = getString(
            R.string.exchange_coin_to_coin_screen_crypto_amount,
            viewModel.fromCoinItem.code
        )
        amountCoinToView.hint = getString(
            R.string.exchange_coin_to_coin_screen_crypto_amount,
            viewModel.toCoinItem?.code ?: ""
        )
        LocalCoinType.values().find { it.name == viewModel.toCoinItem?.code }?.let { coinType ->
            pickCoinButtonView.setText(coinType.fullName)
            pickCoinButtonView.setDrawableStartEnd(coinType.resIcon(), R.drawable.ic_arrow_drop_down)
        }
    }

    private fun showSmsDialog(errorMessage: String? = null) {
        val fragment = SmsDialogFragment()
        fragment.arguments = bundleOf(SmsDialogFragment.TAG_ERROR to errorMessage)
        fragment.show(supportFragmentManager, SmsDialogFragment::class.simpleName)
        fragment.setDialogListener { smsCode ->
            viewModel.exchangeTransaction(smsCode, amountCoinFromView.getString().toDouble())
        }
    }

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
        const val TAG_COIN_ITEM_LIST = "tag_coin_item_list"
    }
}