package com.app.belcobtm.presentation.features.wallet.trade.recall

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_trade_recall.*
import kotlinx.android.synthetic.main.view_material_sms_code_dialog.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeRecallActivity : BaseActivity() {
    private val viewModel: TradeRecallViewModel by viewModel { parametersOf(intent.getStringExtra(TAG_COIN_CODE)) }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val cryptoBalance = viewModel.getMaxValue()
            val cryptoAmountTemporary = it.getDouble()
            val cryptoAmount: Double

            if (cryptoAmountTemporary > cryptoBalance) {
                viewModel.selectedAmount = viewModel.coinItem.balanceCoin
                cryptoAmount = cryptoBalance
                it.clear()
                it.insert(0, cryptoAmount.toStringCoin())
            } else {
                viewModel.selectedAmount = cryptoAmountTemporary
                cryptoAmount = cryptoAmountTemporary
            }

            if (cryptoAmountTemporary > 0) {
                amountUsdView.setText((cryptoAmount * viewModel.coinItem.priceUsd).toStringUsd())
                recallButtonView.isEnabled = true
            } else {
                amountUsdView.clearText()
                recallButtonView.isEnabled = false
            }
        },
        secondTextWatcher = {
            val maxCryptoAmount = viewModel.getMaxValue()
            val maxUsdAmount = maxCryptoAmount * viewModel.coinItem.priceUsd
            val usdAmountTemporary = it.getDouble()
            val usdAmount: Double

            if (usdAmountTemporary > maxUsdAmount) {
                viewModel.selectedAmount = viewModel.coinItem.balanceCoin
                usdAmount = maxUsdAmount
                it.clear()
                it.insert(0, usdAmount.toStringUsd())
            } else {
                viewModel.selectedAmount = usdAmountTemporary / viewModel.coinItem.priceUsd
                usdAmount = usdAmountTemporary
            }

            if (usdAmountTemporary > 0) {
                amountCryptoView.setText((usdAmount / viewModel.coinItem.priceUsd).toStringCoin())
                recallButtonView.isEnabled = true
            } else {
                amountCryptoView.clearText()
                recallButtonView.isEnabled = false
            }
        }
    )

    private val smsDialog: AlertDialog by lazy {
        val view = layoutInflater.inflate(R.layout.view_material_sms_code_dialog, null)
        val smsCodeView = view.smsCodeView
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next, null)
            .setNegativeButton(R.string.cancel) { _, _ -> showProgress(false) }
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
                    viewModel.completeTransaction(smsCodeView.getString())
                    dialog.dismiss()
                }
            }
        }
        return@lazy dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_recall)

        initListeners()
        initObservers()
        initViews()
    }

    private fun initListeners() {
        maxCryptoView.setOnClickListener { amountCryptoView.setText(viewModel.getMaxValue().toStringCoin()) }
        maxUsdView.setOnClickListener { amountCryptoView.setText(viewModel.getMaxValue().toStringCoin()) }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountUsdView.editText?.addTextChangedListener(doubleTextWatcher.secondTextWatcher)
        recallButtonView.setOnClickListener { viewModel.createTransaction() }
    }

    private fun initObservers() {
        viewModel.createTransactionLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    smsDialog.show()
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.TokenError -> startActivity(Intent(this, PinActivity::class.java))
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })

        viewModel.completeTransactionLiveData.observe(this, Observer
        { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> finish()
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.TokenError -> startActivity(Intent(this, PinActivity::class.java))
                        is Failure.MessageError -> showError(loadingData.errorType.message)
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
        priceUsdView.text = getString(R.string.transaction_price_usd, viewModel.coinItem.priceUsd.toStringUsd())
        balanceCryptoView.text = getString(
            R.string.transaction_crypto_balance,
            viewModel.coinItem.balanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        balanceUsdView.text = getString(R.string.transaction_price_usd, viewModel.coinItem.balanceUsd.toStringUsd())
        reservedCryptoView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_crypto,
            viewModel.coinItem.reservedBalanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        reservedUsdView.text = getString(
            R.string.exchange_coin_to_coin_screen_balance_usd,
            viewModel.coinItem.reservedBalanceUsd.toStringUsd()
        )
        amountCryptoView.hint = getString(R.string.withdraw_screen_crypto_amount, viewModel.coinItem.code)
    }

    companion object {
        const val TAG_COIN_CODE = "tag_coin_code"
    }
}