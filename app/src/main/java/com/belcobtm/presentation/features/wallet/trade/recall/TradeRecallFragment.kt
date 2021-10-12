package com.belcobtm.presentation.features.wallet.trade.recall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeRecallBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.features.wallet.trade.reserve.InputFieldState
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class TradeRecallFragment : BaseFragment<FragmentTradeRecallBinding>() {
    override var isMenuEnabled: Boolean = true
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        val initValue = viewModel.initialLoadLiveData.value
        if (initValue == null || initValue is LoadingData.Success) {
            viewModel.performTransaction()
        } else {
            viewModel.loadInitialData()
        }
    }
    private val args by navArgs<TradeRecallFragmentArgs>()
    private val currencyFormatter: Formatter<Double> by inject(
        named(DoubleCurrencyPriceFormatter.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )
    private val viewModel: TradeRecallViewModel by viewModel {
        parametersOf(args.coinCode)
    }
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

    override fun FragmentTradeRecallBinding.initListeners() {
        maxCryptoView.setOnClickListener {
            amountCryptoView.setText(
                viewModel.getMaxValue().toStringCoin()
            )
        }
        amountCryptoView.editText?.addTextChangedListener(cryptoAmountTextWatcher)
        recallButtonView.setOnClickListener { viewModel.performTransaction() }
    }

    override fun FragmentTradeRecallBinding.initObservers() {
        viewModel.initialLoadLiveData.listen(success = {
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
                when (viewModel.getCoinCode().isEthRelatedCoinCode()) {
                    true -> LocalCoinType.ETH.name
                    false -> viewModel.getCoinCode()
                }
            )
            reservedUsdView.text = currencyFormatter.format(viewModel.coinItem.reservedBalanceUsd)
            amountCryptoView.hint = getString(R.string.text_amount, viewModel.coinItem.code)
        })
        viewModel.fee.observe(viewLifecycleOwner) { fee ->
            amountCryptoView.helperText = getString(
                R.string.transaction_helper_text_commission,
                fee.toStringCoin(),
                viewModel.getCoinCode()
            )
        }
        viewModel.transactionLiveData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(), R.string.trade_recall_screen_success_message
                )
                popBackStack()
            }
        )
        viewModel.cryptoFieldState.observe(viewLifecycleOwner) { fieldState ->
            when (fieldState) {
                InputFieldState.Valid -> amountCryptoView.clearError()
                InputFieldState.LessThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_recall_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_recall_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountCryptoView.error =
                    getString(R.string.trade_recall_screen_not_enough_eth)
            }
        }
        amountCryptoView.editText?.actionDoneListener {
            hideKeyboard()
        }
    }

    override fun FragmentTradeRecallBinding.initViews() {
        setToolbarTitle(getString(R.string.trade_recall_screen_title, args.coinCode))
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTradeRecallBinding =
        FragmentTradeRecallBinding.inflate(inflater, container, false)
}
