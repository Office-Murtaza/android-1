package com.app.belcobtm.presentation.features.wallet.trade.reserve

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeReserveBinding
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeReserveFragment : BaseFragment<FragmentTradeReserveBinding>() {
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        val initValue = viewModel.initialLoadLiveData.value
        if (initValue == null || initValue is LoadingData.Success) {
            viewModel.createTransaction()
        }
    }
    private val viewModel: TradeReserveViewModel by viewModel {
        parametersOf(TradeReserveFragmentArgs.fromBundle(requireArguments()).coinCode)
    }

    private val cryptoAmountTextWatcher by lazy {
        SafeDecimalEditTextWatcher { editable ->
            val cryptoAmount = editable.getDouble()
            binding.amountUsdView.text = if (cryptoAmount > 0) {
                getString(R.string.text_usd, (cryptoAmount * viewModel.coinItem.priceUsd).toStringUsd())
            } else {
                getString(R.string.text_usd, "0.0")
            }
            viewModel.validateCryptoAmount(cryptoAmount)
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
        viewModel.initialLoadLiveData.listen(success = {})
        viewModel.createTransactionLiveData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(), R.string.trade_reserve_screen_success_message
                )
                popBackStack()
            }
        )
        viewModel.cryptoFieldState.observe(viewLifecycleOwner, Observer { fieldState ->
            when (fieldState) {
                InputFieldState.Valid -> amountCryptoView.clearError()
                InputFieldState.LessThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_reserve_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_reserve_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountUsdView.error =
                    getString(R.string.trade_reserve_screen_not_enough_eth)
            }
        })
        viewModel.submitButtonEnable.observe(viewLifecycleOwner, Observer { enable ->
            reserveButtonView.isEnabled = enable
        })
    }

    override fun FragmentTradeReserveBinding.initViews() {
        setToolbarTitle(R.string.trade_reserve_screen_title)
        priceUsdView.text = getString(R.string.text_usd, viewModel.coinItem.priceUsd.toStringUsd())
        balanceCryptoView.text = getString(
            R.string.text_text,
            viewModel.coinItem.balanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        balanceUsdView.text =
            getString(R.string.text_usd, viewModel.coinItem.balanceUsd.toStringUsd())
        amountCryptoView.helperText = getString(
            R.string.transaction_helper_text_commission,
            viewModel.getTransactionFee().toStringCoin(),
            viewModel.coinItem.code
        )
        reservedCryptoView.text = getString(
            R.string.text_text,
            viewModel.coinItem.reservedBalanceCoin.toStringCoin(),
            viewModel.coinItem.code
        )
        reservedUsdView.text = getString(
            R.string.text_usd,
            viewModel.coinItem.reservedBalanceUsd.toStringUsd()
        )
        amountCryptoView.hint = getString(R.string.text_amount, viewModel.coinItem.code)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeReserveBinding =
        FragmentTradeReserveBinding.inflate(inflater, container, false)
}
