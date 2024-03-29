package com.belcobtm.presentation.screens.wallet.withdraw

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.belcobtm.R
import com.belcobtm.databinding.FragmentWithdrawBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.isTransactionValidationError
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.helper.ClipBoardHelper
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.clearError
import com.belcobtm.presentation.tools.extensions.getDouble
import com.belcobtm.presentation.tools.extensions.getString
import com.belcobtm.presentation.tools.extensions.setText
import com.belcobtm.presentation.tools.extensions.setTextSilently
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.formatter.CryptoPriceFormatter
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import com.google.zxing.integration.android.IntentIntegrator
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class WithdrawFragment : BaseFragment<FragmentWithdrawBinding>() {

    private val viewModel: WithdrawViewModel by viewModel {
        parametersOf(WithdrawFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    private val clipBoardHelper: ClipBoardHelper by inject()
    private val currencyFormatter: Formatter<Double> by inject(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )
    private val cryptoPriceFormatter: Formatter<Double> by inject(
        named(CryptoPriceFormatter.CRYPTO_PRICE_FORMATTER_QUALIFIER)
    )

    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val cryptoAmount: Double = editable.getDouble()
            viewModel.setAmount(cryptoAmount)
        }
    )

    override val isBackButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true

    override val retryListener: View.OnClickListener =
        View.OnClickListener { binding.validateAndSubmit() }

    override fun FragmentWithdrawBinding.initViews() {
        setToolbarTitle(getString(R.string.withdraw_screen_screen_title, viewModel.getCoinCode()))
        // to prevent paste possibility through standard method
        addressView.isLongClickable = false
        addressView.editText?.isLongClickable = false
    }

    override fun FragmentWithdrawBinding.initListeners() {
        addressEditText.addTextChangedListener {
            viewModel.checkAddressInput(it)
        }
        addressScanView.setOnClickListener {
            IntentIntegrator.forSupportFragment(this@WithdrawFragment).initiateScan()
        }
        addressPasteView.setOnClickListener {
            clipBoardHelper.getTextFromClipboard()?.let {
                addressView.setText(it)
            }
        }
        maxCryptoView.setOnClickListener {
            viewModel.setMaxAmount()
        }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountEditText.addTextChangedListener {
            viewModel.checkAmountInput(it)
        }

        nextButtonView.setOnClickListener { validateAndSubmit() }
        amountCryptoView.actionDoneListener { hideKeyboard() }
    }

    override fun FragmentWithdrawBinding.initObservers() {
        with(viewModel) {
            loadingLiveData.listen({
                initScreen()
            })
            amount.observe(viewLifecycleOwner) {
                binding.amountUsdView.text = if (it.amount > 0) {
                    currencyFormatter.format(it.amount * getUsdPrice())
                } else {
                    currencyFormatter.format(0.0)
                }
                if (it.amount == amountCryptoView.editText?.text?.getDouble()) {
                    return@observe
                }
                val formattedCoin = it.amount.toStringCoin()
                amountCryptoView.editText?.setTextSilently(
                    doubleTextWatcher.firstTextWatcher,
                    formattedCoin, formattedCoin.length
                )
            }
            fee.observe(viewLifecycleOwner) { fee ->
                amountCryptoView.helperText = getString(
                    R.string.transaction_helper_text_commission,
                    fee.toStringCoin(),
                    when {
                        getCoinCode().isEthRelatedCoinCode() -> LocalCoinType.ETH.name
                        getCoinCode() == LocalCoinType.XRP.name -> getString(
                            R.string.xrp_additional_transaction_comission, LocalCoinType.XRP.name
                        )
                        else -> getCoinCode()
                    }
                )
            }
            cryptoAmountError.observe(viewLifecycleOwner, amountCryptoView::setError)
            addressError.observe(viewLifecycleOwner, addressView::setError)
            transactionLiveData.listen(
                success = {
                    binding.errorTextView.isVisible = false
                    AlertHelper.showToastShort(
                        requireContext(),
                        R.string.transactions_screen_transaction_created
                    )
                    popBackStack()
                },
                error = {
                    binding.errorTextView.isVisible = false
                    when (it) {
                        is Failure.NetworkConnection -> showErrorNoInternetConnection()
                        is Failure.MessageError -> {
                            if (it.isTransactionValidationError()) {
                                binding.errorTextView.text = it.message.orEmpty()
                                binding.errorTextView.isVisible = true
                            } else showToast(it.message.orEmpty())
                            showContent()
                        }
                        is Failure.ServerError -> showErrorServerError()
                        else -> showErrorSomethingWrong()
                    }
                }
            )
            isNextButtonEnabled.observe(viewLifecycleOwner) {
                binding.nextButtonView.isEnabled = it
            }
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWithdrawBinding =
        FragmentWithdrawBinding.inflate(inflater, container, false)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showError(R.string.cancelled)
            } else {
                binding.addressView.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun FragmentWithdrawBinding.initScreen() {
        priceUsdView.text = cryptoPriceFormatter.format(viewModel.getUsdPrice())
        balanceCryptoView.text =
            getString(
                R.string.text_text,
                viewModel.getCoinBalance().toStringCoin(),
                viewModel.getCoinCode()
            )
        balanceUsdView.text = currencyFormatter.format(viewModel.getUsdBalance())
        reservedCryptoView.text = getString(
            R.string.text_text,
            viewModel.getReservedBalanceCoin().toStringCoin(),
            viewModel.getCoinCode()
        )
        reservedUsdView.text = currencyFormatter.format(viewModel.getReservedBalanceUsd())
    }

    private fun FragmentWithdrawBinding.validateAndSubmit() {
        amountCryptoView.clearError()
        addressView.clearError()
        viewModel.withdraw(addressView.getString())
    }

}
