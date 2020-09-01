package com.app.belcobtm.presentation.features.wallet.withdraw

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_withdraw.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class WithdrawFragment : BaseFragment() {
    private val viewModel: WithdrawViewModel by viewModel {
        parametersOf(WithdrawFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    private var cryptoBalanceToSend = 0.0
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val fromMaxValue = viewModel.getMaxValue()
            val fromCoinAmountTemporary = editable.getDouble()
            val cryptoAmount: Double

            if (fromCoinAmountTemporary >= fromMaxValue) {
                cryptoBalanceToSend = viewModel.getCoinBalance()
                cryptoAmount = fromMaxValue
                editable.clear()
                editable.insert(0, cryptoAmount.toStringCoin())
            } else {
                cryptoBalanceToSend = fromCoinAmountTemporary
                cryptoAmount = fromCoinAmountTemporary
            }

            amountUsdView.text = if (cryptoAmount > 0) {
                getString(R.string.unit_usd_dynamic_symbol, (cryptoAmount * viewModel.getUsdPrice()).toStringUsd())
            } else {
                getString(R.string.unit_usd_dynamic_symbol, "0.0")
            }
            updateNextButton()
        }
    )

    override val resourceLayout: Int = R.layout.fragment_withdraw
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = false
    override val retryListener: View.OnClickListener = View.OnClickListener { validateAndSubmit() }

    override fun initViews() {
        setToolbarTitle(getString(R.string.withdraw_screen_screen_title, viewModel.getCoinCode()))
        priceUsdView.text = getString(R.string.unit_usd_dynamic_symbol, viewModel.getUsdPrice().toStringUsd())
        balanceCryptoView.text =
            getString(
                R.string.transaction_crypto_balance,
                viewModel.getCoinBalance().toStringCoin(),
                viewModel.getCoinCode()
            )
        balanceUsdView.text = getString(R.string.transaction_price_usd, viewModel.getUsdBalance().toStringUsd())
        amountCryptoView.hint = getString(R.string.withdraw_screen_crypto_amount, viewModel.getCoinCode())
        amountCryptoView.actionDoneListener { validateAndSubmit() }
        nextButtonView.setOnClickListener { validateAndSubmit() }
    }

    override fun initListeners() {
        addressScanView.setOnClickListener { IntentIntegrator.forSupportFragment(this).initiateScan() }
        addressPasteView.setOnClickListener {
            addressView.setText(getTextFromClipboard())
            updateNextButton()
        }
        maxCryptoView.setOnClickListener { amountCryptoView.setText(viewModel.getCoinBalance().toStringCoin()) }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
    }

    override fun initObservers() {
        viewModel.transactionLiveData.listen({
            AlertHelper.showToastShort(requireContext(), R.string.transactions_screen_transaction_created)
            popBackStack()
        })
    }

    private fun getTextFromClipboard(): String {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showError(R.string.cancelled)
            } else {
                val walletCode = result.contents.replaceBefore(':', "")
                    .replaceBefore('=', "")
                    .removePrefix(":")
                addressView.setText(walletCode)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun validateAndSubmit() {
        amountCryptoView.clearError()
        addressView.clearError()

        var errors = 0

        val isCatm = viewModel.getCoinCode() == LocalCoinType.CATM.name
        //Validate CATM by ETH commission
        if (isCatm && viewModel.isNotEnoughBalanceETH()) {
            errors++
            showError(R.string.withdraw_screen_where_money_libovski)
        }

        if (!isCatm && cryptoBalanceToSend > (viewModel.getCoinBalance() - viewModel.getTransactionFee())) {
            errors++
            showError(R.string.not_enough_balance)
        }

        if (errors == 0) {
            viewModel.withdraw(addressView.getString(), cryptoBalanceToSend)
        }
    }

    private fun isValidAddress(): Boolean = CoinTypeExtension.getTypeByCode(
        if (LocalCoinType.CATM.name == viewModel.getCoinCode()) LocalCoinType.ETH.name else viewModel.getCoinCode()
    )?.validate(addressView.getString()) ?: false

    private fun updateNextButton() {
        nextButtonView.isEnabled = cryptoBalanceToSend > viewModel.getTransactionFee() && isValidAddress()
    }
}
