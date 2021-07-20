package com.belcobtm.presentation.features.wallet.trade.reserve

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeReserveBinding
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class TradeReserveFragment : BaseFragment<FragmentTradeReserveBinding>() {
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
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
    private val viewModel: TradeReserveViewModel by viewModel {
        parametersOf(args.coinCode)
    }
    private val currencyFormatter: Formatter<Double> by inject(
        named(DoubleCurrencyPriceFormatter.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )

    private val cryptoAmountTextWatcher by lazy {
        SafeDecimalEditTextWatcher { editable ->
            val cryptoAmount = editable.getDouble()
            binding.amountUsdView.text = if (cryptoAmount > 0) {
                currencyFormatter.format(cryptoAmount * viewModel.coinItem.priceUsd)
            } else {
                currencyFormatter.format(0.0)
            }
            viewModel.selectedAmount = cryptoAmount
        }
    }

    override fun FragmentTradeReserveBinding.initListeners() {
        maxCryptoView.setOnClickListener {
            amountCryptoView.setText(
                viewModel.getMaxValue().toStringCoin()
            )
        }
        amountCryptoView.editText?.actionDoneListener {
            hideKeyboard()
        }
        amountCryptoView.editText?.addTextChangedListener(cryptoAmountTextWatcher)
        reserveButtonView.setOnClickListener { viewModel.createTransaction() }
    }

    override fun FragmentTradeReserveBinding.initObservers() {
        viewModel.initialLoadLiveData.listen(success = {
            priceUsdView.text = currencyFormatter.format(viewModel.coinItem.priceUsd)
            balanceCryptoView.text = getString(
                R.string.text_text,
                viewModel.coinItem.balanceCoin.toStringCoin(),
                viewModel.coinItem.code
            )
            balanceUsdView.text = currencyFormatter.format(viewModel.coinItem.balanceUsd)
            amountCryptoView.helperText = getString(
                R.string.transaction_helper_text_commission,
                viewModel.getTransactionFee().toStringCoin(),
                viewModel.getCoinCode()
            )
            reservedCryptoView.text = getString(
                R.string.text_text,
                viewModel.coinItem.reservedBalanceCoin.toStringCoin(),
                viewModel.coinItem.code
            )
            reservedUsdView.text = currencyFormatter.format(viewModel.coinItem.reservedBalanceUsd)
            amountCryptoView.hint = getString(R.string.text_amount, viewModel.coinItem.code)
        })
        viewModel.createTransactionLiveData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(), R.string.trade_reserve_screen_success_message
                )
                popBackStack()
            }
        )
        viewModel.cryptoFieldState.observe(viewLifecycleOwner) { fieldState ->
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
