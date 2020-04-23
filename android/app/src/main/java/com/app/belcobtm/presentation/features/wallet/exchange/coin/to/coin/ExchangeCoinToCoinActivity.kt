package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_exchange_coin_to_coin.*
import kotlinx.android.synthetic.main.view_material_sms_code_dialog.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ExchangeCoinToCoinActivity : BaseActivity() {
    private val viewModel: ExchangeCoinToCoinViewModel by viewModel {
        parametersOf(
            intent.getParcelableExtra(TAG_COIN_ITEM),
            intent.getParcelableArrayListExtra<IntentCoinItem>(TAG_COIN_ITEM_LIST)
        )
    }
    private val smsDialog: AlertDialog by lazy {
        val view = layoutInflater.inflate(R.layout.view_material_sms_code_dialog, null)
        val smsCodeView = view.smsCodeView
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next, null)
            .setNegativeButton(R.string.cancel, null)
            .setView(view)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener {
            smsCodeView.clearText()
            smsCodeView.clearError()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (smsCodeView.getString().length != 4) {
                    smsCodeView.showError(R.string.error_sms_code_4_digits)
                } else {
                    smsCodeView.clearError()
                    viewModel.exchangeTransaction(
                        smsCodeView.getString(),
                        amountCoinFromView.getString().toDouble()
                    )
                    dialog.dismiss()
                }
            }
        }
        return@lazy dialog
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
            val coinAdapter = CoinDialogAdapter(pickCoinButtonView.context, coinList)
            MaterialAlertDialogBuilder(pickCoinButtonView.context)
                .setTitle(R.string.exchange_coin_to_coin_screen_select_coin)
                .setAdapter(coinAdapter) { _, which ->
                    pickCoinButtonView.setText(coinList[which].verboseValue())
                    pickCoinButtonView.setDrawableStartEnd(coinList[which].resIcon(), R.drawable.ic_arrow_drop_down)
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
        amountCoinToView?.editText?.keyListener = null
        amountCoinToView?.editText?.afterTextChanged {
            nextButtonView.isEnabled = it.isNotEmpty() && it.toString().toDouble() > 0
        }
        maxCoinFromView.setOnClickListener {
            amountCoinFromView.setText(viewModel.fromCoinItem.balanceCoin.toStringCoin())
        }
        nextButtonView.setOnClickListener {
            viewModel.createTransaction(amountCoinFromView.getString().toDouble())
        }
    }

    private fun initObservers() {
        viewModel.exchangeLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    when (it.data) {
                        ExchangeCoinToCoinViewModel.TRANSACTION_CREATED -> smsDialog.show()
                        ExchangeCoinToCoinViewModel.TRANSACTION_EXCHANGED -> finish()
                    }
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
        viewModel.getCoinTypeList().find { it.code() == viewModel.toCoinItem?.coinCode }?.let { coinType ->
            pickCoinButtonView.setText(coinType.verboseValue())
            pickCoinButtonView.setDrawableStartEnd(coinType.resIcon(), R.drawable.ic_arrow_drop_down)
        }
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
                    amountCoinToView.clearText()
                }
                else -> {
                    val fromCoinTemporaryValue = amountCoinFromView.getString().toDouble()
                    val fromCoinAmount: Double =
                        if (fromCoinTemporaryValue > viewModel.fromCoinItem.balanceCoin) viewModel.fromCoinItem.balanceCoin
                        else fromCoinTemporaryValue
                    val toCoinRefPrice = viewModel.toCoinItem?.priceUsd ?: 0.0
                    val fromCoinPrice = viewModel.fromCoinItem.priceUsd
                    val fromCoinProfitC2c = viewModel.coinFeeItem.profitC2C
                    val toCoinAmount =
                        (fromCoinAmount * fromCoinPrice) / toCoinRefPrice * (100 - fromCoinProfitC2c) / 100

                    if (fromCoinTemporaryValue > viewModel.fromCoinItem.balanceCoin) {
                        editable.clear()
                        editable.insert(0, fromCoinAmount.toStringCoin())
                    }
                    amountCoinToView.setText(toCoinAmount.toStringCoin())
                }
            }

            isRunning = false
        }
    }

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
        const val TAG_COIN_ITEM_LIST = "tag_coin_item_list"
        const val MAX_CHARS_AFTER_DOT = 6
        const val DOT_CHAR: Char = '.'
    }
}