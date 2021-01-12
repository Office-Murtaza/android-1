package com.app.belcobtm.presentation.features.wallet.trade.recall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeRecallBinding
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.app.belcobtm.presentation.features.wallet.trade.reserve.InputFieldState
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
        }
    }
    private val viewModel: TradeRecallViewModel by viewModel {
        parametersOf(TradeRecallFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val cryptoAmount = it.getDouble()
            val usdAmount = cryptoAmount * viewModel.coinItem.priceUsd
            if (cryptoAmount > 0) {
                binding.amountUsdView.setText(usdAmount.toStringUsd())
            } else {
                binding.amountUsdView.clearText()
            }
            viewModel.validateCryptoAmount(cryptoAmount)
        },
        secondTextWatcher = {
            val usdAmount = it.getDouble()
            val cryptoAmount = usdAmount / viewModel.coinItem.priceUsd
            if (usdAmount > 0) {
                binding.amountCryptoView.setText(cryptoAmount.toStringCoin())
            } else {
                binding.amountCryptoView.clearText()
            }
            viewModel.validateCryptoAmount(cryptoAmount)
        }
    )

    override fun FragmentTradeRecallBinding.initListeners() {
        maxCryptoView.setOnClickListener {
            amountCryptoView.setText(
                viewModel.getMaxValue().toStringCoin()
            )
        }
        maxUsdView.setOnClickListener {
            amountCryptoView.setText(
                viewModel.getMaxValue().toStringCoin()
            )
        }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountUsdView.editText?.addTextChangedListener(doubleTextWatcher.secondTextWatcher)
        recallButtonView.setOnClickListener { viewModel.performTransaction() }
    }

    override fun FragmentTradeRecallBinding.initObservers() {
        viewModel.initialLoadLiveData.listen(success = {})
        viewModel.transactionLiveData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(), R.string.trade_recall_screen_success_message
                )
                popBackStack()
            }
        )
        viewModel.cryptoFieldState.observe(viewLifecycleOwner, Observer { fieldState ->
            when (fieldState) {
                InputFieldState.Valid -> amountCryptoView.clearError()
                InputFieldState.LessThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_recall_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountCryptoView.error =
                    getString(R.string.trade_recall_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountUsdView.error =
                    getString(R.string.trade_recall_screen_not_enough_eth)
            }
        })
        viewModel.usdFieldState.observe(viewLifecycleOwner, Observer { fieldState ->
            when (fieldState) {
                InputFieldState.Valid -> amountUsdView.clearError()
                InputFieldState.LessThanNeedError -> amountUsdView.error =
                    getString(R.string.trade_recall_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountUsdView.error =
                    getString(R.string.trade_recall_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountUsdView.error =
                    getString(R.string.trade_recall_screen_not_enough_eth)
            }
        })
        viewModel.submitButtonEnable.observe(viewLifecycleOwner, Observer { enable ->
            recallButtonView.isEnabled = enable
        })
    }

    override fun FragmentTradeRecallBinding.initViews() {
        setToolbarTitle(R.string.trade_recall_screen_title)
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
        amountCryptoView.hint = getString(R.string.text_amount, viewModel.coinItem.code)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeRecallBinding =
        FragmentTradeRecallBinding.inflate(inflater, container, false)
}
