package com.belcobtm.presentation.features.wallet.trade.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.databinding.FragmentCreateTradeBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.features.wallet.trade.container.TradeContainerFragment
import com.belcobtm.presentation.features.wallet.trade.create.delegate.TradePaymentOptionDelegate
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateTradeFragment : BaseFragment<FragmentCreateTradeBinding>() {

    override val isHomeButtonEnabled: Boolean
        get() = true
    override val isToolbarEnabled: Boolean
        get() = true

    private val viewModel by viewModel<CreateTradeViewModel>()

    override val retryListener: View.OnClickListener
        get() = View.OnClickListener {
            if (viewModel.initialLoadingData.value is LoadingData.Error<Unit>) {
                viewModel.fetchInitialData()
            } else {
                viewModel.createTrade(
                    when {
                        binding.tradeTypeBuyChip.isChecked -> TradeType.BUY
                        binding.tradeTypeSellChip.isChecked -> TradeType.SELL
                        else -> -1
                    }, binding.termsInput.editText?.text.toString()
                )
            }
        }

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(TradePaymentOptionDelegate(viewModel::changePaymentSelection))
        }
    }

    private val priceTextWatcher = SafeDecimalEditTextWatcher { editable ->
        viewModel.updatePrice(editable.getDouble())
    }

    private val initialMinAmountValue by lazy { resources.getInteger(R.integer.trade_amount_min) }
    private val initialMaxAmountValue by lazy { resources.getInteger(R.integer.trade_amount_max) }

    private val minAmountTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = editable.getInt()
        viewModel.updateMinAmount(parsedAmount)
    }

    private val maxAmountTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = editable.getInt()
        viewModel.updateMaxAmount(parsedAmount)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateTradeBinding =
        FragmentCreateTradeBinding.inflate(inflater, container, false)

    override fun FragmentCreateTradeBinding.initViews() {
        setToolbarTitle(R.string.create_trade_screen_title)
        coinDetailsView.setMaxButtonEnabled(false)
        coinDetailsView.setErrorEnabled(false)
        coinDetailsView.getEditText().setText("0")
        coinDetailsView.setHint(requireContext().getString(R.string.create_trade_price_input_hint))
        coinDetailsView.setPadding(0, 0, 0, 0)
        paymentOptions.adapter = adapter
        paymentOptions.setHasFixedSize(true)
        paymentOptions.overScrollMode = View.OVER_SCROLL_NEVER
        viewModel.updateMinAmount(initialMinAmountValue)
        viewModel.updateMaxAmount(initialMaxAmountValue)
        binding.amountMinLimitEditText.setText(initialMinAmountValue.toString())
        binding.amountMaxLimitEditText.setText(initialMaxAmountValue.toString())
    }

    override fun FragmentCreateTradeBinding.initObservers() {
        viewModel.selectedCoin.observe(viewLifecycleOwner, ::setCoinData)
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
            coinDetailsView.setErrorEnabled(!it.isNullOrEmpty())
        }
        viewModel.initialLoadingData.listen()
        viewModel.cryptoAmountFormatted.observe(viewLifecycleOwner, cryptoAmountValue::setText)
        viewModel.priceError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
            coinDetailsView.setErrorEnabled(!it.isNullOrEmpty())
        }
        viewModel.termsError.observe(viewLifecycleOwner) {
            binding.termsInput.error = it
        }
        viewModel.amountRangeError.observe(viewLifecycleOwner) {
            it?.let(amountRangeError::setText)
            amountRangeError.toggle(it != null)
        }
        viewModel.amountRangeError.observe(viewLifecycleOwner) {
            it?.let(amountRangeError::setText)
            amountRangeError.toggle(it != null)
        }
        viewModel.tradeTypeError.observe(viewLifecycleOwner) {
            it?.let(tradeTypeChipGroupError::setText)
            tradeTypeChipGroupError.toggle(it != null)
        }
        viewModel.paymentOptionsError.observe(viewLifecycleOwner) {
            it?.let(paymentOptionsError::setText)
            paymentOptionsError.toggle(it != null)
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
                getNavController()?.previousBackStackEntry?.savedStateHandle
                    ?.set(TradeContainerFragment.CREATE_TRADE_KEY, true)
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
        coinDetailsView.setOnCoinButtonClickListener {
            AlertHelper.showSelectCoinDialog(requireContext(), viewModel.getCoinsToSelect()) {
                viewModel.selectCoin(it)
            }
        }
        createTradeButton.setOnClickListener {
            viewModel.createTrade(
                when {
                    binding.tradeTypeBuyChip.isChecked -> TradeType.BUY
                    binding.tradeTypeSellChip.isChecked -> TradeType.SELL
                    else -> -1
                },
                binding.termsInput.editText?.text.toString()
            )
        }
        binding.amountMinLimitEditText.actionDoneListener {
            hideKeyboard()
            binding.amountMinLimitEditText.clearFocus()
        }
        binding.amountMaxLimitEditText.actionDoneListener {
            hideKeyboard()
            binding.amountMaxLimitEditText.clearFocus()
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