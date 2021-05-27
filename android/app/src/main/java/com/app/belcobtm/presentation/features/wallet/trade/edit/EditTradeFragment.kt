package com.app.belcobtm.presentation.features.wallet.trade.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.FragmentEditTradeBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.app.belcobtm.presentation.features.wallet.trade.create.delegate.TradePaymentOptionDelegate
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import org.koin.android.viewmodel.ext.android.viewModel

class EditTradeFragment : BaseFragment<FragmentEditTradeBinding>() {

    override var isHomeButtonEnabled: Boolean = true
    override val isToolbarEnabled: Boolean = true

    private val args by navArgs<EditTradeFragmentArgs>()
    private val viewModel by viewModel<EditTradeViewModel>()

    override val retryListener: View.OnClickListener?
        get() = View.OnClickListener {
            if (viewModel.initialLoadingData.value is LoadingData.Error<Unit>) {
                viewModel.fetchTradeDetails(args.tradeId)
            } else {
                viewModel.editTrade(
                    args.tradeId, binding.termsInput.editText?.text.toString(),
                    minAmountValue, maxAmountValue
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        viewModel.fetchTradeDetails(args.tradeId)
        return root
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentEditTradeBinding =
        FragmentEditTradeBinding.inflate(inflater, container, false)

    override fun FragmentEditTradeBinding.initViews() {
        setToolbarTitle(R.string.edit_trade_screen_title)
        coinDetailsView.setMaxButtonEnabled(false)
        coinDetailsView.setErrorEnabled(false)
        coinDetailsView.getEditText().setText(0.0.toString())
        coinDetailsView.setHint(requireContext().getString(R.string.create_trade_price_input_hint))
        coinDetailsView.setPadding(0, 0, 0, 0)
        paymentOptions.adapter = adapter
        paymentOptions.setHasFixedSize(true)
        paymentOptions.overScrollMode = View.OVER_SCROLL_NEVER
        viewModel.updateMinAmount(minAmountValue)
        viewModel.updateMaxAmount(maxAmountValue)
        binding.amountMinLimitEditText.setText(minAmountValue.toString())
        binding.amountMaxLimitEditText.setText(maxAmountValue.toString())
    }

    override fun FragmentEditTradeBinding.initObservers() {
        viewModel.selectedCoin.observe(viewLifecycleOwner, ::setCoinData)
        viewModel.initialTerms.observe(viewLifecycleOwner) {
            termsInput.editText?.setText(it)
        }
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
        }
        viewModel.initialLoadingData.listen(error = {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.create_trade_get_coin_list_error_title)
                .setMessage(R.string.create_trade_get_coin_list_error_message)
                .setPositiveButton(R.string.create_trade_get_coin_list_error_button_title) { _, _ ->
                    popBackStack()
                }
                .create()
                .show()
        })
        viewModel.cryptoAmountFormatted.observe(viewLifecycleOwner, cryptoAmountValue::setText)
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
        }
        viewModel.priceError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
        }
        viewModel.priceRangeError.observe(viewLifecycleOwner) {
            it?.let(amountRangeError::setText)
            amountRangeError.toggle(it != null)
        }
        viewModel.snackbarMessage.observe(viewLifecycleOwner) {
            Snackbar.make(root, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel.amountMinLimit.observe(viewLifecycleOwner) { amount ->
            binding.amountMinLimitEditText.setTextSilently(
                minAmountTextWatcher, amount.toString(), amount.toString().length
            )
        }
        viewModel.amountMaxLimit.observe(viewLifecycleOwner) { amount ->
            binding.amountMaxLimitEditText.setTextSilently(
                maxAmountTextWatcher, amount.toString(), amount.toString().length
            )
        }
        viewModel.initialPrice.observe(viewLifecycleOwner) { amount ->
            binding.coinDetailsView.getEditText().setTextSilently(
                priceTextWatcher, amount.toString(), amount.toString().length
            )
        }
        viewModel.availablePaymentOptions.observe(viewLifecycleOwner, adapter::update)
        viewModel.tradeType.observe(viewLifecycleOwner) {
            if (it == TradeType.SELL) {
                tradeTypeBuyChip.isEnabled = false
                tradeTypeSellChip.isChecked = true
            } else {
                tradeTypeBuyChip.isChecked = true
                tradeTypeSellChip.isEnabled = false
            }
        }
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
        }
        viewModel.editTradeLoadingData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(),
                    R.string.transactions_screen_transaction_created
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

    override fun FragmentEditTradeBinding.initListeners() {
        setupTradeTypeCheckChangeListener(tradeTypeBuyChip)
        setupTradeTypeCheckChangeListener(tradeTypeSellChip)
        coinDetailsView.getEditText().addTextChangedListener(priceTextWatcher)
        amountMinLimitEditText.addTextChangedListener(minAmountTextWatcher)
        amountMaxLimitEditText.addTextChangedListener(maxAmountTextWatcher)
        editTradeButton.setOnClickListener {
            viewModel.editTrade(
                args.tradeId, binding.termsInput.editText?.text.toString(),
                minAmountValue, maxAmountValue
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
        binding.coinDetailsView.setCoinData(coinCode, localType.resIcon(), showCoinArrow = false)
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