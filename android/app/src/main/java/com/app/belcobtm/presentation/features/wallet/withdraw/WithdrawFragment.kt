package com.app.belcobtm.presentation.features.wallet.withdraw

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
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
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val cryptoAmount: Double = editable.getDouble()
            amountUsdView.text = if (cryptoAmount > 0) {
                getString(R.string.text_usd, (cryptoAmount * viewModel.getUsdPrice()).toStringUsd())
            } else {
                getString(R.string.text_usd, "0.0")
            }
            updateNextButton()
        }
    )

    override val resourceLayout: Int = R.layout.fragment_withdraw
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { validateAndSubmit() }

    override fun initViews() {
        setToolbarTitle(getString(R.string.withdraw_screen_screen_title, viewModel.getCoinCode()))
        priceUsdView.text = getString(R.string.text_usd, viewModel.getUsdPrice().toStringUsd())
        balanceCryptoView.text =
            getString(
                R.string.text_text,
                viewModel.getCoinBalance().toStringCoin(),
                viewModel.getCoinCode()
            )
        balanceUsdView.text = getString(R.string.text_usd, viewModel.getUsdBalance().toStringUsd())
        amountCryptoView.hint = getString(R.string.text_amount, viewModel.getCoinCode())
        amountCryptoView.actionDoneListener { validateAndSubmit() }
        nextButtonView.setOnClickListener { validateAndSubmit() }
        amountCryptoView.helperText = getString(
            R.string.transaction_helper_text_commission,
            viewModel.getTransactionFee().toStringCoin(),
            if (viewModel.getCoinCode() == LocalCoinType.CATM.name) LocalCoinType.ETH.name else viewModel.getCoinCode()
        )
        reservedCryptoView.text = getString(
            R.string.text_text,
            viewModel.getReservedBalanceCoin().toStringCoin(),
            viewModel.getCoinCode()
        )
        reservedUsdView.text = getString(
            R.string.text_usd,
            viewModel.getReservedBalanceUsd().toStringUsd()
        )
    }

    override fun initListeners() {
        addressScanView.setOnClickListener { IntentIntegrator.forSupportFragment(this).initiateScan() }
        addressPasteView.setOnClickListener {
            addressView.setText(getTextFromClipboard())
            updateNextButton()
        }
        maxCryptoView.setOnClickListener {
            amountCryptoView.setText(
                viewModel.getMaxValue().toStringCoin()
            )
        }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
    }

    override fun initObservers() {
        viewModel.transactionLiveData.listen(
            success = {
                AlertHelper.showToastShort(requireContext(), R.string.transactions_screen_transaction_created)
                popBackStack()
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showSnackBar(it.message ?: "")
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    is Failure.XRPLowAmountToSend -> {
                        amountCryptoView.showError(R.string.error_xrp_amount_is_not_enough)
                        showContent()
                    }
                    else -> showErrorSomethingWrong()
                }
            }
        )
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
            amountCryptoView.showError(R.string.withdraw_screen_where_money_libovski)
        }

        if (!isCatm && amountCryptoView.getDouble() > (viewModel.getCoinBalance() - viewModel.getTransactionFee())) {
            errors++
            amountCryptoView.showError(R.string.insufficient_balance)
        }

        if(amountCryptoView.getDouble() < viewModel.getMinValue()) {
            errors++
            amountCryptoView.showError(R.string.balance_amount_too_small)
        }

        if(amountCryptoView.getDouble() >= viewModel.getMaxValue()) {
            errors++
            amountCryptoView.showError(R.string.balance_amount_exceeded)
        }

        if (errors == 0) {
            viewModel.withdraw(addressView.getString(), amountCryptoView.getDouble())
        }
    }

    private fun isValidAddress(): Boolean = CoinTypeExtension.getTypeByCode(
        if (LocalCoinType.CATM.name == viewModel.getCoinCode()) LocalCoinType.ETH.name else viewModel.getCoinCode()
    )?.validate(addressView.getString()) ?: false

    private fun updateNextButton() {
        nextButtonView.isEnabled = amountCryptoView.isNotBlank()
                && amountCryptoView.getDouble() > 0
                && isValidAddress()
    }
}
