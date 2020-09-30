package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
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
            val fromMaxValue = viewModel.getMaxValue()
            val cryptoAmount: Double

            if (fromCoinAmountTemporary >= fromMaxValue) {
                cryptoAmount = fromMaxValue
                editable.clear()
                editable.insert(0, fromMaxValue.toStringCoin())
            } else {
                cryptoAmount = fromCoinAmountTemporary
            }

            nextButtonView.isEnabled = fromCoinAmountTemporary > 0
            amountCoinToView.text = getString(
                R.string.text_usd,
                (cryptoAmount * viewModel.fromCoinItem.priceUsd).toStringCoin()
            )
            balanceCoinToView.text = getString(
                R.string.text_text,
                viewModel.getCoinToAmount(cryptoAmount).toStringCoin(),
                viewModel.toCoinItem?.code ?: ""
            )
        }
    )

    override val resourceLayout: Int = R.layout.fragment_exchange
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = false
    override val retryListener: View.OnClickListener = View.OnClickListener {
        viewModel.exchange(amountCoinFromView.getString().toDouble())
    }

    override fun initViews() {
        setToolbarTitle(
            getString(
                R.string.exchange_coin_to_coin_screen_title,
                viewModel.fromCoinItem.code
            )
        )
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
        amountCoinFromView.helperText = getString(
            R.string.transaction_helper_text_commission,
            viewModel.fromCoinFeeItem.txFee.toStringCoin(),
            if (viewModel.fromCoinItem.code == LocalCoinType.CATM.name) LocalCoinType.ETH.name else viewModel.fromCoinItem.code
        )

        val toCoinCode: String? = viewModel
            .coinItemList
            .firstOrNull { it.code == viewModel.toCoinItem?.code }
            ?.code

        if (toCoinCode == null) {
            pickCoinButtonView.isEnabled = false
        } else {
            pickCoinButtonView.isEnabled = true
            val coinType = LocalCoinType.valueOf(toCoinCode)
            pickCoinButtonView.setText(coinType.fullName)
            pickCoinButtonView.setResizedDrawableStart(
                coinType.resIcon(),
                R.drawable.ic_arrow_drop_down
            )
            balanceCoinToView.text = getString(R.string.text_text, "0.0", coinType.name)
            amountCoinToView.text = getString(R.string.text_usd, "0.0")
        }
    }

    override fun initListeners() {
        pickCoinButtonView.editText?.keyListener = null
        pickCoinButtonView.editText?.setOnClickListener {
            val itemList: List<LocalCoinType> = viewModel
                .coinItemList
                .filter { it.code != viewModel.fromCoinItem.code }
                .map { LocalCoinType.valueOf(it.code) }
            val coinAdapter = CoinDialogAdapter(pickCoinButtonView.context, itemList)
            MaterialAlertDialogBuilder(pickCoinButtonView.context)
                .setTitle(R.string.exchange_coin_to_coin_screen_select_coin)
                .setAdapter(coinAdapter) { _, which ->
                    val selectedItem: LocalCoinType = itemList[which]
                    pickCoinButtonView.setText(selectedItem.fullName)
                    pickCoinButtonView.setResizedDrawableStart(
                        selectedItem.resIcon(),
                        R.drawable.ic_arrow_drop_down
                    )
                    amountCoinToView.hint = getString(
                        R.string.text_amount,
                        selectedItem.name
                    )
                    viewModel.toCoinItem =
                        viewModel.coinItemList.find { it.code == selectedItem.name }
                    amountCoinFromView?.editText?.setText(amountCoinFromView.getString())
                }
                .create()
                .show()
        }
        amountCoinFromView?.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        maxCoinFromView.setOnClickListener { amountCoinFromView.setText(viewModel.getMaxValue().toStringCoin()) }
        nextButtonView.setOnClickListener {
            amountCoinFromView.clearError()
            viewModel.exchange(amountCoinFromView.getString().toDouble())
        }
    }

    override fun initObservers() {
        viewModel.exchangeLiveData.listen(
            success = {
                AlertHelper.showToastShort(requireContext(), R.string.transactions_screen_transaction_created)
                popBackStack()
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showSnackBar(it.message ?: "")
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    is Failure.XRPLowAmountToSend -> {
                        amountCoinFromView.showError(R.string.error_xrp_amount_is_not_enough)
                        showContent()
                    }
                    else -> showErrorSomethingWrong()
                }
            }
        )
    }
}