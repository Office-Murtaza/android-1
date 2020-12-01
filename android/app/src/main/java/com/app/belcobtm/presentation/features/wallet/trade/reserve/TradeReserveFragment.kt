package com.app.belcobtm.presentation.features.wallet.trade.reserve

import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import kotlinx.android.synthetic.main.fragment_trade_reserve.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TradeReserveFragment : BaseFragment() {
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val resourceLayout: Int = R.layout.fragment_trade_reserve
    private val viewModel: TradeReserveViewModel by viewModel {
        parametersOf(TradeReserveFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_USD,
        firstTextWatcher = {
            val cryptoAmount = it.getDouble()
            if (cryptoAmount > 0) {
                amountUsdView.setText((cryptoAmount * viewModel.coinItem.priceUsd).toStringUsd())
            } else {
                amountUsdView.clearText()
            }
            viewModel.validateCryptoAmount(cryptoAmount)
        },
        secondTextWatcher = {
            val usdAmount = it.getDouble()
            val cryptoAmount = usdAmount / viewModel.coinItem.priceUsd
            if (usdAmount > 0) {
                amountCryptoView.setText(cryptoAmount.toStringCoin())
            } else {
                amountCryptoView.clearText()
            }
            viewModel.validateCryptoAmount(cryptoAmount)
        }
    )

    override fun initListeners() {
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
        reserveButtonView.setOnClickListener { viewModel.createTransaction() }
    }

    override fun initObservers() {
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
        viewModel.usdFieldState.observe(viewLifecycleOwner, Observer { fieldState ->
            when (fieldState) {
                InputFieldState.Valid -> amountUsdView.clearError()
                InputFieldState.LessThanNeedError -> amountUsdView.error =
                    getString(R.string.trade_reserve_screen_min_error)
                InputFieldState.MoreThanNeedError -> amountUsdView.error =
                    getString(R.string.trade_reserve_screen_max_error)
                InputFieldState.NotEnoughETHError -> amountUsdView.error =
                    getString(R.string.trade_reserve_screen_not_enough_eth)
            }
        })
        viewModel.submitButtonEnable.observe(this, Observer { enable ->
            reserveButtonView.isEnabled = enable
        })
    }

    override fun initViews() {
        setToolbarTitle(R.string.trade_reserve_screen_title)
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
}