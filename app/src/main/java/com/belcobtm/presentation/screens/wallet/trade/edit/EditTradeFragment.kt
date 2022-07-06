package com.belcobtm.presentation.screens.wallet.trade.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.databinding.FragmentEditTradeBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.screens.wallet.trade.create.delegate.TradePaymentOptionDelegate
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.getDouble
import com.belcobtm.presentation.tools.extensions.getInt
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.tools.extensions.setTextSilently
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.toggle
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class EditTradeFragment : BaseFragment<FragmentEditTradeBinding>() {

    override var isBackButtonEnabled: Boolean = true

    private val args by navArgs<EditTradeFragmentArgs>()
    private val viewModel by viewModel<EditTradeViewModel>()
    private val priceFormatter by inject<Formatter<Double>>(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )

    override val retryListener: View.OnClickListener
        get() = View.OnClickListener {
            if (viewModel.initialLoadingData.value is LoadingData.Error<Unit>) {
                viewModel.fetchTradeDetails(args.tradeId)
            } else {
                viewModel.editTrade(args.tradeId, binding.termsInput.editText?.text.toString())
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

    private val minAmountTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = editable.getInt()
        viewModel.updateMinAmount(parsedAmount)
    }

    private val maxAmountTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = editable.getInt()
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
        coinDetailsView.getEditText().setText("0")
        coinDetailsView.setHint(requireContext().getString(R.string.create_trade_price_input_hint))
        coinDetailsView.setPadding(0, 0, 0, 0)
        paymentOptions.adapter = adapter
        paymentOptions.setHasFixedSize(true)
        paymentOptions.overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun FragmentEditTradeBinding.initObservers() {
        viewModel.selectedCoin.observe(viewLifecycleOwner, ::setCoinData)
        viewModel.initialTerms.observe(viewLifecycleOwner) {
            termsInput.editText?.setText(it)
        }
        viewModel.initialLoadingData.listen()
        viewModel.cryptoAmountFormatted.observe(viewLifecycleOwner, cryptoAmountValue::setText)
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
            coinDetailsView.setErrorEnabled(!it.isNullOrEmpty())
        }
        viewModel.priceError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it, true)
            coinDetailsView.setErrorEnabled(!it.isNullOrEmpty())
        }
        viewModel.termsError.observe(viewLifecycleOwner) {
            binding.termsInput.error = it
        }
        viewModel.priceRangeError.observe(viewLifecycleOwner) {
            it?.let(amountRangeError::setText)
            amountRangeError.toggle(it != null)
        }
        viewModel.snackbarMessage.observe(viewLifecycleOwner) {
            Snackbar.make(root, it, Snackbar.LENGTH_SHORT).show()
        }
        viewModel.paymentOptionsError.observe(viewLifecycleOwner) {
            it?.let(paymentOptionsError::setText)
            paymentOptionsError.toggle(it != null)
        }
        viewModel.initialAmountMinLimit.observe(viewLifecycleOwner) { amount ->
            binding.amountMinLimitEditText.setTextSilently(
                minAmountTextWatcher, amount.toString(), amount.toString().length
            )
        }
        viewModel.initialAmountMaxLimit.observe(viewLifecycleOwner) { amount ->
            binding.amountMaxLimitEditText.setTextSilently(
                maxAmountTextWatcher, amount.toString(), amount.toString().length
            )
        }
        viewModel.initialPrice.observe(viewLifecycleOwner) { amount ->
            binding.coinDetailsView.getEditText().setTextSilently(
                priceTextWatcher, amount.toStringCoin(), amount.toStringCoin().length
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
        viewModel.editTradeLoadingData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(),
                    R.string.trade_updated_message
                )
                popBackStack()
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showToast(it.message.orEmpty())
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
            viewModel.editTrade(args.tradeId, termsInput.editText?.text.toString())
        }
        amountMinLimitEditText.actionDoneListener {
            hideKeyboard()
            amountMinLimitEditText.clearFocus()
        }
        amountMaxLimitEditText.actionDoneListener {
            hideKeyboard()
            amountMaxLimitEditText.clearFocus()
        }
        limitDetails.setOnClickListener {
            navigate(EditTradeFragmentDirections.toServiceInfoDialog(ServiceType.TRADE))
        }
    }

    private fun setCoinData(coin: CoinDataItem) {
        val coinCode = coin.code
        val coinBalance = coin.reservedBalanceCoin.toStringCoin()
        val localType = LocalCoinType.valueOf(coinCode)
        binding.coinDetailsView.setCoinData(coinCode, localType.resIcon(), showCoinArrow = false)
        val balancePart = getString(R.string.sell_screen_balance)
        val coinPart = getString(
            R.string.coin_balance_format,
            coinBalance,
            coinCode,
            priceFormatter.format(coin.reservedBalanceUsd)
        )
        val balanceFormatted = getString(R.string.sell_screen_balance_formatted, balancePart, coinPart)
        binding.coinDetailsView.setHelperTextWithLink(balanceFormatted, coinPart) {
            val uri = getString(R.string.reserved_deeplink_format, coinCode).toUri()
            findNavController().navigate(uri)
        }
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
