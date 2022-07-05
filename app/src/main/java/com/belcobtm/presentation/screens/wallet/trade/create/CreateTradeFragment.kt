package com.belcobtm.presentation.screens.wallet.trade.create

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.belcobtm.R
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.databinding.FragmentCreateTradeBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.screens.wallet.trade.container.TradeContainerFragment
import com.belcobtm.presentation.screens.wallet.trade.create.delegate.TradePaymentOptionDelegate
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.getDouble
import com.belcobtm.presentation.tools.extensions.getInt
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.toggle
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class CreateTradeFragment : BaseFragment<FragmentCreateTradeBinding>() {

    override val isBackButtonEnabled: Boolean
        get() = true
    override val isToolbarEnabled: Boolean
        get() = true

    private val viewModel by viewModel<CreateTradeViewModel>()
    private val priceFormatter by inject<Formatter<Double>>(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )

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

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateTradeBinding =
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
                findNavController().previousBackStackEntry?.savedStateHandle
                    ?.set(TradeContainerFragment.CREATE_TRADE_KEY, true)
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
                    is Failure.LocationError -> showError(it.message.orEmpty())
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
            createTradeWithPermissionCheck()
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
            navigate(CreateTradeFragmentDirections.toServiceInfoDialog(ServiceType.TRADE))
        }
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun createTrade() {
        viewModel.createTrade(
            when {
                binding.tradeTypeBuyChip.isChecked -> TradeType.BUY
                binding.tradeTypeSellChip.isChecked -> TradeType.SELL
                else -> -1
            },
            binding.termsInput.editText?.text.toString()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun showLocationError() {
        viewModel.showLocationError()
    }

    private fun setCoinData(coin: CoinDataItem) {
        val coinCode = coin.code
        val coinBalance = coin.reservedBalanceCoin.toStringCoin()
        val localType = LocalCoinType.valueOf(coinCode)
        binding.coinDetailsView.setCoinData(coinCode, localType.resIcon())
        val balancePart = getString(R.string.sell_screen_balance)
        val coinPart = getString(
            R.string.coin_balance_format,
            coinBalance,
            coinCode,
            priceFormatter.format(coin.reservedBalanceUsd)
        )
        val balanceFormatted =
            getString(R.string.sell_screen_balance_formatted, balancePart, coinPart)
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
