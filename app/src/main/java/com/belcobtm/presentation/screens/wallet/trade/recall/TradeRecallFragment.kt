package com.belcobtm.presentation.screens.wallet.trade.recall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeRecallBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.screens.wallet.trade.reserve.InputFieldState
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.clearError
import com.belcobtm.presentation.tools.extensions.getDouble
import com.belcobtm.presentation.tools.extensions.setText
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.formatter.DoubleCurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class TradeRecallFragment : BaseFragment<FragmentTradeRecallBinding>() {

    private val viewModel: TradeRecallViewModel by viewModel {
        parametersOf(args.coinCode)
    }

    override var isMenuEnabled: Boolean = true
    override val isBackButtonEnabled: Boolean = true
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
        amountCryptoEditText.addTextChangedListener {
            viewModel.checkAmountInput(it)
        }
        amountCryptoView.editText?.addTextChangedListener(cryptoAmountTextWatcher)
        submitButton.setOnClickListener { viewModel.performTransaction() }
    }

    override fun FragmentTradeRecallBinding.initObservers() {
        with(viewModel) {
            initialLoadLiveData.listen(success = {
                initScreen()
            })
            fee.observe(viewLifecycleOwner) { fee ->
                amountCryptoView.helperText = getString(
                    R.string.transaction_helper_text_commission,
                    fee.toStringCoin(),
                    viewModel.getCoinCode()
                )
            }
            transactionLiveData.listen(
                success = {
                    AlertHelper.showToastShort(
                        requireContext(), R.string.trade_recall_screen_success_message
                    )
                    popBackStack()
                }
            )
            cryptoFieldState.observe(viewLifecycleOwner) { fieldState ->
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
            isSubmitButtonEnabled.observe(viewLifecycleOwner) {
                binding.submitButton.isEnabled = it
            }
        }
    }

    private fun FragmentTradeRecallBinding.initScreen() {
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
