package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_exchange.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ExchangeFragment : BaseFragment() {
    private val viewModel: ExchangeViewModel by viewModel {
        parametersOf(ExchangeFragmentArgs.fromBundle(requireArguments()).coinCode)
    }

    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        firstTextWatcher = { editable ->
            val fromCoinAmountTemporary = editable.getDouble()
            val fromCoinAmount: Double =
                if (fromCoinAmountTemporary > viewModel.fromCoinItem.balanceCoin) viewModel.fromCoinItem.balanceCoin
                else fromCoinAmountTemporary
            val toCoinAmount = fromCoinAmount * getExchangeValue()
            val fromMaxValue = getMaxValueFromCoin()

            if (fromCoinAmountTemporary > fromMaxValue) {
                editable.clear()
                editable.insert(0, fromMaxValue.toStringCoin())
            }

            nextButtonView.isEnabled = fromCoinAmountTemporary > 0
            amountCoinToView.text = getString(R.string.text_usd, toCoinAmount.toStringCoin())
        }
    )

    override val resourceLayout: Int = R.layout.fragment_exchange
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = false
    override val retryListener: View.OnClickListener = View.OnClickListener { }

    override fun initViews() {
        setToolbarTitle(getString(R.string.exchange_coin_to_coin_screen_title, viewModel.fromCoinItem.code))
        priceUsdView.text = getString(
            R.string.text_usd,
            viewModel.fromCoinItem.priceUsd.toStringUsd()
        )
        balanceCryptoView.text = getString(
            R.string.text_text,
            viewModel.fromCoinItem.balanceCoin.toStringCoin(),
            viewModel.fromCoinItem.code
        )
        balanceUsdView.text =
            getString(R.string.text_usd, viewModel.fromCoinItem.balanceUsd.toStringUsd())
        amountCoinFromView.hint = getString(
            R.string.text_amount,
            viewModel.fromCoinItem.code
        )
        amountCoinToView.hint = getString(
            R.string.text_amount,
            viewModel.toCoinItem?.code ?: ""
        )
        LocalCoinType.values().find { it.name == viewModel.toCoinItem?.code }?.let { coinType ->
            pickCoinButtonView.setText(coinType.fullName)
            pickCoinButtonView.setResizedDrawableStart(coinType.resIcon(), R.drawable.ic_arrow_drop_down)
        }
        amountCoinToView.text = getString(R.string.text_usd, "0.0")
        updateBalanceToView()
    }

    override fun initListeners() {
        pickCoinButtonView.editText?.keyListener = null
        pickCoinButtonView.editText?.setOnClickListener {
            val coinAdapter = CoinDialogAdapter(pickCoinButtonView.context)
            MaterialAlertDialogBuilder(pickCoinButtonView.context)
                .setTitle(R.string.exchange_coin_to_coin_screen_select_coin)
                .setAdapter(coinAdapter) { _, which ->
                    pickCoinButtonView.setText(LocalCoinType.values()[which].fullName)
                    pickCoinButtonView.setResizedDrawableStart(
                        LocalCoinType.values()[which].resIcon(),
                        R.drawable.ic_arrow_drop_down
                    )
                    amountCoinToView.hint = getString(
                        R.string.text_amount,
                        LocalCoinType.values()[which].name
                    )
                    viewModel.toCoinItem = viewModel.coinItemList.find { it.code == LocalCoinType.values()[which].name }
                    viewModel.toCoinFeeItem = viewModel.coinFeeItemList[LocalCoinType.values()[which].name]
                    amountCoinFromView?.editText?.setText(amountCoinFromView.getString())
                    updateBalanceToView()
                }
                .create()
                .show()
        }
        amountCoinFromView?.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        maxCoinFromView.setOnClickListener { amountCoinFromView.setText(getMaxValueFromCoin().toStringCoin()) }
        nextButtonView.setOnClickListener {
            val amount = amountCoinFromView.getString().toDouble() + viewModel.fromCoinFeeItem.txFee
            viewModel.exchange(amount)
        }
    }

    override fun initObservers() {
        viewModel.exchangeLiveData.listen({ popBackStack() })
    }

    private fun getMaxValueFromCoin(): Double = viewModel.fromCoinItem.balanceCoin - viewModel.fromCoinFeeItem.txFee

    private fun updateBalanceToView() {
        balanceCoinToView.text = getString(
            R.string.text_text,
            viewModel.toCoinItem?.balanceCoin?.toStringCoin() ?: "",
            viewModel.toCoinItem?.code ?: ""
        )
    }

    private fun getExchangeValue(): Double {
        val fromCoinPrice = viewModel.fromCoinItem.priceUsd
        val fromCoinProfitC2c = viewModel.fromCoinFeeItem.profitExchange
        val toCoinRefPrice = viewModel.toCoinItem?.priceUsd ?: 0.0
        return fromCoinPrice / toCoinRefPrice * (100 - fromCoinProfitC2c) / 100
    }
}