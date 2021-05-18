package com.app.belcobtm.presentation.features.wallet.trade.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.FragmentCreateTradeBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.app.belcobtm.presentation.features.deals.swap.adapter.CoinDialogAdapter
import com.app.belcobtm.presentation.features.wallet.trade.create.delegate.TradePaymentOptionDelegate
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import org.koin.android.viewmodel.ext.android.viewModel

class CreateTradeFragment : BaseFragment<FragmentCreateTradeBinding>() {

    override val isHomeButtonEnabled: Boolean
        get() = true
    override val isToolbarEnabled: Boolean
        get() = true

    private val viewModel by viewModel<CreateTradeViewModel>()

    override val retryListener: View.OnClickListener?
        get() = View.OnClickListener {
            if (viewModel.initialLoadingData.value is LoadingData.Error<Unit>) {
                viewModel.fetchInitialData()
            } else {
                viewModel.createTrade(
                    if (binding.tradeTypeBuyChip.isChecked) TradeType.BUY else TradeType.SELL,
                    binding.termsInput.editText?.text.toString(), minAmountValue, maxAmountValue
                )
            }
        }

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(TradePaymentOptionDelegate())
        }
    }

    private val priceTextWatcher = SafeDecimalEditTextWatcher { editable ->
        viewModel.updatePrice(editable.getDouble())
    }

    private val minAmountValue by lazy { resources.getInteger(R.integer.trade_amount_min) }
    private val maxAmountValue by lazy { resources.getInteger(R.integer.trade_amount_max) }

    private val minAmountTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val currentMax = binding.amountMaxLimitEditText.text?.getInt() ?: maxAmountValue
        val parsedAmount = editable.getInt().coerceAtMost(currentMax)
        viewModel.updateMinAmount(parsedAmount)
    }

    private val maxAmountTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = editable.getInt().coerceAtMost(maxAmountValue)
        viewModel.updateMaxAmount(parsedAmount)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateTradeBinding =
        FragmentCreateTradeBinding.inflate(inflater, container, false)

    override fun FragmentCreateTradeBinding.initViews() {
        setToolbarTitle(R.string.create_trade_screen_title)
        coinDetailsView.setMaxButtonEnabled(false)
        coinDetailsView.setErrorEnabled(false)
        coinDetailsView.getEditText().setText(0.0.toString())
        coinDetailsView.setHint(requireContext().getString(R.string.create_trade_price_input_hint))
        coinDetailsView.setPadding(0, 0, 0, 0)
        paymentOptions.adapter = adapter
        paymentOptions.setHasFixedSize(true)
        paymentOptions.overScrollMode = View.OVER_SCROLL_NEVER
        viewModel.updateMinAmount(amountRangeSlider.values[0].toInt())
        viewModel.updateMaxAmount(amountRangeSlider.values[1].toInt())
    }

    override fun FragmentCreateTradeBinding.initObservers() {
        val amountStepSize = resources.getInteger(R.integer.trade_amount_step)
        viewModel.selectedCoin.observe(viewLifecycleOwner, ::setCoinData)
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
        }
        viewModel.initialLoadingData.listen({})
        viewModel.amountMinLimit.observe(viewLifecycleOwner) { amount ->
            if (amount in minAmountValue..maxAmountValue) {
                amountRangeSlider.values = amountRangeSlider.values.apply {
                    set(0, (amount - amount % amountStepSize).toFloat())
                }
            }
            amountMinLimitEditText.setTextSilently(minAmountTextWatcher, amount.toString())
        }
        viewModel.amountMaxLimit.observe(viewLifecycleOwner) { amount ->
            if (amount in minAmountValue..maxAmountValue) {
                amountRangeSlider.values = amountRangeSlider.values.apply {
                    set(1, (amount - amount % amountStepSize).toFloat())
                }
            }
            amountMaxLimitEditText.setTextSilently(maxAmountTextWatcher, amount.toString())
        }
        viewModel.cryptoAmountFormatted.observe(viewLifecycleOwner, cryptoAmountValue::setText)
        viewModel.priceError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
        }
        viewModel.amountRangeError.observe(viewLifecycleOwner) {
            it?.let(amountRangeError::setText)
            amountRangeError.toggle(it != null)
        }
        viewModel.snackbarMessage.observe(viewLifecycleOwner) {
            Snackbar.make(root, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel.availablePaymentOptions.observe(viewLifecycleOwner, adapter::update)
        viewModel.createTradeLoadingData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(),
                    R.string.create_trade_success_message
                )
                popBackStack()
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showSnackBar(it.message.orEmpty())
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    is Failure.ValidationError -> showError(it.message.orEmpty())
                    is Failure.ClientValidationError -> showContent()
                    else -> showErrorSomethingWrong()
                }
            })
    }

    override fun FragmentCreateTradeBinding.initListeners() {
        setupTradeTypeCheckChangeListener(tradeTypeBuyChip)
        setupTradeTypeCheckChangeListener(tradeTypeSellChip)
        coinDetailsView.getEditText().addTextChangedListener(priceTextWatcher)
        amountMinLimitEditText.addTextChangedListener(minAmountTextWatcher)
        amountMaxLimitEditText.addTextChangedListener(maxAmountTextWatcher)
        amountRangeSlider.addOnChangeListener { slider, _, fromUser ->
            if (fromUser) {
                viewModel.updateMinAmount(slider.values[0].toInt())
                viewModel.updateMaxAmount(slider.values[1].toInt())
            }
        }
        coinDetailsView.setOnCoinButtonClickListener(View.OnClickListener {
            showSelectCoinDialog {
                viewModel.selectCoin(it)
            }
        })
        createTradeButton.setOnClickListener {
            viewModel.createTrade(
                if (binding.tradeTypeBuyChip.isChecked) TradeType.BUY else TradeType.SELL,
                binding.termsInput.editText?.text.toString(), minAmountValue, maxAmountValue
            )
        }
    }

    private fun setCoinData(coin: CoinDataItem) {
        val coinCode = coin.code
        val coinBalance = coin.reservedBalanceCoin.toStringCoin()
        val localType = LocalCoinType.valueOf(coinCode)
        binding.coinDetailsView.setCoinData(coinCode, localType.resIcon())
        binding.coinDetailsView.setHelperText(
            getString(
                R.string.trade_create_reserved_balance_formatted,
                coinBalance,
                coinCode
            )
        )
    }

    private fun showSelectCoinDialog(action: (CoinDataItem) -> Unit) {
        val safeContext = context ?: return
        val coinsList = viewModel.getCoinsToSelect()
        val adapter = CoinDialogAdapter(safeContext, coinsList)
        AlertDialog.Builder(safeContext)
            .setAdapter(adapter) { _, position -> action.invoke(coinsList[position]) }
            .create()
            .show()
    }

    private fun setupTradeTypeCheckChangeListener(chip: Chip) {
        chip.setOnCheckedChangeListener { _, isChecked ->
            chip.chipStrokeWidth = if (isChecked) {
                resources.getDimensionPixelSize(R.dimen.divider_size).toFloat()
            } else {
                0.0f
            }
        }
    }
}