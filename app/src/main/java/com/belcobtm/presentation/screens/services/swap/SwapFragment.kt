package com.belcobtm.presentation.screens.services.swap

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.databinding.FragmentSwapBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.coin.model.ValidationResult
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.belcobtm.presentation.tools.extensions.getDouble
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.tools.extensions.setTextSilently
import com.belcobtm.presentation.tools.extensions.toHtmlSpan
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class SwapFragment : BaseFragment<FragmentSwapBinding>() {

    companion object {

        const val MIN_COINS_TO_ENABLE_DIALOG_PICKER = 2
    }

    private val viewModel: SwapViewModel by viewModel()
    private val currencyFormatter: Formatter<Double> by inject(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )
    private val textWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        firstTextWatcher = { editable ->
            val parsedCoinAmount = editable.getDouble()
            if (parsedCoinAmount != viewModel.sendCoinAmount.value?.amount) {
                viewModel.setSendAmount(parsedCoinAmount)
            }
        },
        secondTextWatcher = { editable ->
            val parsedCoinAmount = editable.getDouble()
            if (parsedCoinAmount != viewModel.receiveCoinAmount.value?.amount) {
                viewModel.setReceiveAmount(parsedCoinAmount)
            }
        }
    )

    override val isBackButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        val initialLoading = viewModel.initLoadingData.value
        when {
            initialLoading is LoadingData.Error -> {
                if (initialLoading.errorType is Failure.OperationCannotBePerformed) {
                    popBackStack()
                } else {
                    // data not yet initialized
                    viewModel.fetchInitialData()
                }
            }
            viewModel.transactionPlanLiveData.value is LoadingData.Error ->
                viewModel.reFetchTransactionPlans()
            else -> {
                // re submit swap
                initSwapWithPermissionCheck()
            }
        }
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

    override fun FragmentSwapBinding.initViews() {
        setToolbarTitle(R.string.swap_screen_title)
        sendCoinInputLayout.setHint(getString(R.string.text_amount))
        receiveCoinInputLayout.setHint(getString(R.string.text_amount))
        sendCoinInputLayout.getEditText().setTextSilently(textWatcher.firstTextWatcher, "0")
        receiveCoinInputLayout.getEditText().setTextSilently(textWatcher.secondTextWatcher, "0")
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSwapBinding =
        FragmentSwapBinding.inflate(inflater, container, false)

    override fun FragmentSwapBinding.initListeners() {
        limitDetails.setOnClickListener {
            navigate(SwapFragmentDirections.toServiceInfoDialog(ServiceType.SWAP))
        }
        nextButtonView.setOnClickListener {
            initSwapWithPermissionCheck()
        }
        viewCircle.setOnClickListener {
            viewModel.changeCoins()
        }
        sendCoinInputLayout.setOnMaxClickListener {
            viewModel.setMaxSendAmount()
        }
        receiveCoinInputLayout.setOnMaxClickListener {
            viewModel.setMaxReceiveAmount()
        }
        sendCoinInputLayout.setOnCoinButtonClickListener(View.OnClickListener {
            val coinToSend = viewModel.coinToSend.value ?: return@OnClickListener
            val coinToReceive = viewModel.coinToReceive.value ?: return@OnClickListener
            val coinsToExclude = listOf(coinToSend, coinToReceive)
            val coinsList = viewModel.originCoinsData.toMutableList().apply {
                removeAll(coinsToExclude)
            }
            AlertHelper.showSelectCoinDialog(requireContext(), coinsList) {
                viewModel.setCoinToSend(it)
            }
        })
        receiveCoinInputLayout.setOnCoinButtonClickListener(View.OnClickListener {
            val coinToSend = viewModel.coinToSend.value ?: return@OnClickListener
            val coinToReceive = viewModel.coinToReceive.value ?: return@OnClickListener
            val coinsToExclude = listOf(coinToSend, coinToReceive)
            val coinsList = viewModel.originCoinsData.toMutableList().apply {
                removeAll(coinsToExclude)
            }
            AlertHelper.showSelectCoinDialog(requireContext(), coinsList) {
                viewModel.setCoinToReceive(it)
            }
        })
        sendCoinInputLayout.getEditText().addTextChangedListener(textWatcher.firstTextWatcher)
        receiveCoinInputLayout.getEditText().addTextChangedListener(textWatcher.secondTextWatcher)
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun initSwap() {
        viewModel.executeSwap()
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun showLocationError() {
        viewModel.showLocationError()
    }

    override fun FragmentSwapBinding.initObservers() {
        viewModel.swapLoadingData.listen(success = {
            AlertHelper.showToastShort(requireContext(), R.string.swap_screen_success_message)
            popBackStack()
        }, error = {
            when (it) {
                is Failure.LocationError -> showError(it.message.orEmpty())
                else -> showErrorSomethingWrong()
            }
        })
        viewModel.transactionPlanLiveData.listen()
        viewModel.initLoadingData.listen(error = {
            when (it) {
                is Failure.OperationCannotBePerformed -> {
                    updateContentContainer(isErrorVisible = true)
                    baseBinding.errorView.errorImageView.setImageResource(R.drawable.ic_screen_state_something_wrong)
                    baseBinding.errorView.errorTitleView.setText(R.string.cannot_perform_swap_error)
                    baseBinding.errorView.errorDescriptionView.setText(R.string.cannot_perform_swap_error_message)
                    baseBinding.errorView.errorRetryButtonView.setText(R.string.cannot_perform_swap_error_button_title)
                }
                else -> {
                    hideKeyboard()
                    baseErrorHandler(it)
                    baseBinding.errorView.errorRetryButtonView.setText(R.string.retry)
                }
            }
        })
        viewModel.coinsDetailsLoadingState.listen()
        viewModel.coinToSendModel.observe(viewLifecycleOwner) { coin ->
            val coinCode = coin.coinCode
            val coinFee = coin.coinFee.toStringCoin()
            val coinBalance = coin.coinBalance.toStringCoin()
            val localType = LocalCoinType.valueOf(coinCode)
            val coinCodeFee = when {
                coinCode.isEthRelatedCoinCode() -> LocalCoinType.ETH.name
                coinCode == LocalCoinType.XRP.name -> getString(
                    R.string.xrp_additional_transaction_comission, LocalCoinType.XRP.name
                )
                else -> coinCode
            }
            sendCoinInputLayout.setCoinData(
                coinCode,
                localType.resIcon(),
                viewModel.originCoinsData.size > MIN_COINS_TO_ENABLE_DIALOG_PICKER
            )
            sendCoinInputLayout.setHelperText(
                getString(
                    R.string.swap_screen_balance_formatted,
                    coinBalance,
                    coinCode,
                    coinFee,
                    coinCodeFee
                )
            )
        }
        viewModel.coinToReceiveModel.observe(viewLifecycleOwner) { coin ->
            val coinCode = coin.coinCode
            val coinFee = coin.coinFee.toStringCoin()
            val coinBalance = coin.coinBalance.toStringCoin()
            val localType = LocalCoinType.valueOf(coinCode)
            val coinCodeFee = when (coinCode.isEthRelatedCoinCode()) {
                true -> LocalCoinType.ETH.name
                false -> coinCode
            }
            receiveCoinInputLayout.setCoinData(
                coinCode,
                localType.resIcon(),
                viewModel.originCoinsData.size > MIN_COINS_TO_ENABLE_DIALOG_PICKER
            )
            receiveCoinInputLayout.setHelperText(
                getString(
                    R.string.swap_screen_balance_formatted,
                    coinBalance,
                    coinCode,
                    coinFee,
                    coinCodeFee
                )
            )
        }
        viewModel.swapRate.observe(viewLifecycleOwner) { rate ->
            rateTextView.text = getString(
                R.string.swap_screen_rate_formatted,
                rate.fromCoinAmount.toString(),
                rate.fromCoinCode,
                rate.swapAmount.toStringCoin(),
                rate.swapCoinCode
            ).toHtmlSpan()
        }
        viewModel.swapFee.observe(viewLifecycleOwner) { fee ->
            platformFeeTextView.text = getString(
                R.string.swap_screen_fee_formatted,
                fee.platformFeePercents.toStringCoin(),
                fee.platformFeeCoinAmount.toStringCoin(),
                fee.swapCoinCode
            ).toHtmlSpan()
        }
        viewModel.coinToSendError.observe(viewLifecycleOwner) { error ->
            when (error) {
                is ValidationResult.Valid -> sendCoinInputLayout.setErrorText(null, false)
                is ValidationResult.InValid ->
                    sendCoinInputLayout.setErrorText(getString(error.error), true)
            }
        }
        viewModel.coinToReceiveError.observe(viewLifecycleOwner) { error ->
            when (error) {
                is ValidationResult.Valid -> receiveCoinInputLayout.setErrorText(null, false)
                is ValidationResult.InValid ->
                    receiveCoinInputLayout.setErrorText(getString(error.error), true)
            }
        }
        viewModel.sendCoinAmount.observe(viewLifecycleOwner) { sendAmount ->
            val targetEditText = sendCoinInputLayout.getEditText()
            if (targetEditText.text.getDouble() == 0.0 && sendAmount.amount == 0.0) {
                return@observe
            }
            val coinAmountString = sendAmount.amount.toStringCoin()
            val watcher = textWatcher.firstTextWatcher
            targetEditText.setTextSilently(watcher, coinAmountString)
        }
        viewModel.receiveCoinAmount.observe(viewLifecycleOwner) { receiveAmount ->
            val targetEditText = receiveCoinInputLayout.getEditText()
            if (targetEditText.text.getDouble() == 0.0 && receiveAmount.amount == 0.0) {
                return@observe
            }
            val coinAmountString = receiveAmount.amount.toStringCoin()
            val watcher = textWatcher.secondTextWatcher
            targetEditText.setTextSilently(watcher, coinAmountString)
        }
        viewModel.submitEnabled.observe(viewLifecycleOwner) { enabled ->
            nextButtonView.isEnabled = enabled
        }
        viewModel.usdReceiveAmount.observe(viewLifecycleOwner) { usdAmount ->
            tvUSDConvertedValue.text = currencyFormatter.format(usdAmount)
        }
    }

}
