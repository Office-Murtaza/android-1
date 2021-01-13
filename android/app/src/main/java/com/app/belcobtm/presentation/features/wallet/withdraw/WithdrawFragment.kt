package com.app.belcobtm.presentation.features.wallet.withdraw

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentWithdrawBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.google.zxing.integration.android.IntentIntegrator
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class WithdrawFragment : BaseFragment<FragmentWithdrawBinding>() {
    private val viewModel: WithdrawViewModel by viewModel {
        parametersOf(WithdrawFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val cryptoAmount: Double = editable.getDouble()
            binding.amountUsdView.text = if (cryptoAmount > 0) {
                getString(R.string.text_usd, (cryptoAmount * viewModel.getUsdPrice()).toStringUsd())
            } else {
                getString(R.string.text_usd, "0.0")
            }
            binding.updateNextButton()
        }
    )

    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { binding.validateAndSubmit() }

    override fun FragmentWithdrawBinding.initListeners() {
        addressScanView.setOnClickListener {
            IntentIntegrator.forSupportFragment(this@WithdrawFragment).initiateScan()
        }
        addressPasteView.setOnClickListener {
            getTextFromClipboard()?.let {
                addressView.setText(it)
                updateNextButton()
            }
        }
        maxCryptoView.setOnClickListener {
            amountCryptoView.setText(
                viewModel.getMaxValue().toStringCoin()
            )
        }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
    }

    override fun FragmentWithdrawBinding.initObservers() {
        viewModel.loadingLiveData.listen({
            initScreen()
        })
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

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWithdrawBinding =
        FragmentWithdrawBinding.inflate(inflater, container, false)

    private fun getTextFromClipboard(): String? {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text?.toString()
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
                binding.addressView.setText(walletCode)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun FragmentWithdrawBinding.initScreen() {
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
            viewModel.getCoinCode()
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

    private fun FragmentWithdrawBinding.validateAndSubmit() {
        amountCryptoView.clearError()
        addressView.clearError()

        var errors = 0

        val validationResult = viewModel.validateAmount(amountCryptoView.getDouble())
        if (validationResult is ValidationResult.InValid) {
            errors++
            amountCryptoView.showError(validationResult.error)
        }

        if (amountCryptoView.getDouble() < viewModel.getMinValue()) {
            errors++
            amountCryptoView.showError(R.string.balance_amount_too_small)
        }

        if (amountCryptoView.getDouble() >= viewModel.getMaxValue()) {
            errors++
            amountCryptoView.showError(R.string.balance_amount_exceeded)
        }

        if (errors == 0) {
            viewModel.withdraw(addressView.getString(), amountCryptoView.getDouble())
        }
    }

    private fun FragmentWithdrawBinding.isValidAddress(): Boolean =
        CoinTypeExtension.getTypeByCode(viewModel.getCoinCode())
            ?.validate(addressView.getString()) ?: false

    private fun FragmentWithdrawBinding.updateNextButton() {
        nextButtonView.isEnabled = amountCryptoView.isNotBlank()
                && amountCryptoView.getDouble() > 0
                && isValidAddress()
    }
}
