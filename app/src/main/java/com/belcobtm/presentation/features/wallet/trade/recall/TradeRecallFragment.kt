package com.belcobtm.presentation.features.wallet.trade.recall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeRecallBinding
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.features.wallet.trade.reserve.InputFieldState
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

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
    private val viewModel: TradeRecallViewModel by viewModel {
        parametersOf(args.coinCode)
    }
    private val cryptoAmountTextWatcher by lazy {
        SafeDecimalEditTextWatcher { editable ->
            val cryptoAmount = editable.getDouble()
            binding.amountUsdView.text = if (cryptoAmount > 0) {
                getString(R.string.text_usd, (cryptoAmount * viewModel.coinItem.priceUsd).toStringUsd())
            } else {
                getString(R.string.text_usd, "0.0")
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
            priceUsdView.text = getString(R.string.text_usd, viewModel.coinItem.priceUsd.toStringUsd())
            balanceCryptoView.text = getString(
                R.string.text_text,
                viewModel.coinItem.balanceCoin.toStringCoin(),
                viewModel.coinItem.code
            )
            balanceUsdView.text =
                getString(R.string.text_usd, viewModel.coinItem.balanceUsd.toStringUsd())
            reservedCryptoView.text = getString(
                R.string.text_text,
                viewModel.coinItem.reservedBalanceCoin.toStringCoin(),
                viewModel.coinItem.code
            )
            reservedUsdView.text = getString(
                R.string.text_usd,
                viewModel.coinItem.reservedBalanceUsd.toStringUsd()
            )
            amountCryptoView.helperText = getString(
                R.string.transaction_helper_text_commission,
                viewModel.getTransactionFee().toStringCoin(),
                viewModel.getCoinCode()
            )
            amountCryptoView.hint = getString(R.string.text_amount, viewModel.coinItem.code)
        })
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

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeRecallBinding =
        FragmentTradeRecallBinding.inflate(inflater, container, false)
}
