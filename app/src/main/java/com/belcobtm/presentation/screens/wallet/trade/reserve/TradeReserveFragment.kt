package com.belcobtm.presentation.screens.wallet.trade.reserve

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeReserveBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.clearError
import com.belcobtm.presentation.tools.extensions.getDouble
import com.belcobtm.presentation.tools.extensions.setTextSilently
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.formatter.DoubleCurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class TradeReserveFragment : BaseFragment<FragmentTradeReserveBinding>() {

    private val viewModel: TradeReserveViewModel by viewModel {
        parametersOf(args.coinCode)
    }

    override val isBackButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        val initValue = viewModel.initialLoadLiveData.value
        if (initValue == null || initValue is LoadingData.Success) {
            viewModel.createTransaction()
        } else {
            viewModel.loadInitialData()
        }
    }
    private val args by navArgs<TradeReserveFragmentArgs>()
    private val currencyFormatter: Formatter<Double> by inject(
        named(DoubleCurrencyPriceFormatter.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )

    private val cryptoAmountTextWatcher by lazy {
        SafeDecimalEditTextWatcher { editable ->
            val cryptoAmount = editable.getDouble()
            viewModel.setAmount(cryptoAmount)
        }
    }

    override fun FragmentTradeReserveBinding.initListeners() {
        maxCryptoView.setOnClickListener {
            viewModel.setMaxAmount()
        }
        amountCryptoView.editText?.actionDoneListener {
            hideKeyboard()
        }
        amountCryptoEditText.addTextChangedListener {
            viewModel.checkAmountInput(it)
        }
        amountCryptoView.editText?.addTextChangedListener(cryptoAmountTextWatcher)
        submitButton.setOnClickListener { viewModel.createTransaction() }
    }

    override fun FragmentTradeReserveBinding.initObservers() {
        with(viewModel) {
            initialLoadLiveData.listen(success = {
                initScreen()
            })
            fee.observe(viewLifecycleOwner) { fee ->
                amountCryptoView.helperText = getString(
                    R.string.transaction_helper_text_commission,
                    fee.toStringCoin(),
                    when {
                        viewModel.getCoinCode().isEthRelatedCoinCode() -> LocalCoinType.ETH.name
                        viewModel.getCoinCode() == LocalCoinType.XRP.name -> getString(
                            R.string.xrp_additional_transaction_comission, LocalCoinType.XRP.name
                        )
                        else -> viewModel.getCoinCode()
                    }
                )
            }
            cryptoAmountError.observe(viewLifecycleOwner, amountCryptoView::setError)
            amount.observe(viewLifecycleOwner) {
                binding.amountUsdView.text = if (it.amount > 0) {
                    currencyFormatter.format(it.amount * viewModel.coinItem.priceUsd)
                } else {
                    currencyFormatter.format(0.0)
                }
                if (it.amount == amountCryptoView.editText?.text?.getDouble()) {
                    return@observe
                }
                val formattedCoin = it.amount.toStringCoin()
                amountCryptoView.editText?.setTextSilently(
                    cryptoAmountTextWatcher,
                    formattedCoin, formattedCoin.length
                )
            }
            createTransactionLiveData.listen(
                success = {
                    AlertHelper.showToastShort(
                        requireContext(), R.string.trade_reserve_screen_success_message
                    )
                    popBackStack()
                }
            )
            cryptoFieldState.observe(viewLifecycleOwner) { fieldState ->
                when (fieldState) {
                    InputFieldState.Valid -> amountCryptoView.clearError()
                    InputFieldState.LessThanNeedError -> amountCryptoView.error =
                        getString(R.string.trade_reserve_screen_min_error)
                    InputFieldState.MoreThanNeedError -> amountCryptoView.error =
                        getString(R.string.trade_reserve_screen_max_error)
                    InputFieldState.NotEnoughETHError -> amountCryptoView.error =
                        getString(R.string.trade_reserve_screen_not_enough_eth)
                }
            }
            isSubmitButtonEnabled.observe(viewLifecycleOwner) {
                binding.submitButton.isEnabled = it
            }
        }
    }

    private fun FragmentTradeReserveBinding.initScreen() {
        priceUsdView.text = currencyFormatter.format(viewModel.coinItem.priceUsd)
        balanceCryptoView.text = getString(
            R.string.text_text,
            viewModel.coinItem.balanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        balanceUsdView.text = currencyFormatter.format(viewModel.coinItem.balanceUsd)
        reservedCryptoView.text = getString(
            R.string.text_text,
            viewModel.coinItem.reservedBalanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        reservedUsdView.text = currencyFormatter.format(viewModel.coinItem.reservedBalanceUsd)
    }

    override fun FragmentTradeReserveBinding.initViews() {
        setToolbarTitle(getString(R.string.trade_reserve_screen_title, args.coinCode))
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTradeReserveBinding =
        FragmentTradeReserveBinding.inflate(inflater, container, false)

}
