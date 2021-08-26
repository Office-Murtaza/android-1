package com.belcobtm.presentation.features.deals.atm.sell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.belcobtm.R
import com.belcobtm.databinding.FragmentAtmSellBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.features.deals.swap.SwapFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class AtmSellFragment : BaseFragment<FragmentAtmSellBinding>() {

    override var isHomeButtonEnabled: Boolean = true

    override val retryListener: View.OnClickListener = View.OnClickListener {
        val initialLoading = viewModel.initLoadingData.value
        if (initialLoading is LoadingData.Error) {
            viewModel.loadInitialData()
        } else {
            viewModel.sell()
        }
    }

    private val viewModel by viewModel<AtmSellViewModel>()

    private val textWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = editable.getDouble()
        if (parsedAmount != viewModel.usdAmount.value) {
            viewModel.setAmount(parsedAmount)
        }
        // "0" should always be displayed for user
        // even through they try to clear the input
        if (editable.isEmpty()) {
            editable.insert(0, "0")
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAtmSellBinding =
        FragmentAtmSellBinding.inflate(inflater, container, false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.loadInitialData()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.post {
            binding.coinInputLayout.getEditText().requestFocus()
            binding.coinInputLayout.getEditText().setSelection(
                binding.coinInputLayout.getEditText().length()
            )
        }
    }

    override fun FragmentAtmSellBinding.initViews() {
        setToolbarTitle(R.string.atm_sell_title)
        coinInputLayout.getEditText().setTextSilently(textWatcher, "0")
    }

    override fun FragmentAtmSellBinding.initObservers() {
        viewModel.initLoadingData.listen()
        viewModel.usdAmount.observe(viewLifecycleOwner) { sendAmount ->
            val targetEditText = coinInputLayout.getEditText()
            if (targetEditText.text.getDouble() == 0.0 && sendAmount == 0.0) {
                return@observe
            }
            val coinAmountString = sendAmount.toStringPercents()
            targetEditText.setTextSilently(textWatcher, coinAmountString)
        }
        viewModel.selectedCoinModel.observe(viewLifecycleOwner) { coin ->
            val coinCode = coin.coinCode
            val coinFee = coin.coinFee.toStringCoin()
            val coinBalance = coin.coinBalance.toStringCoin()
            val localType = LocalCoinType.valueOf(coinCode)
            val coinCodeFee = when (coinCode.isEthRelatedCoinCode()) {
                true -> LocalCoinType.ETH.name
                false -> coinCode
            }
            coinInputLayout.setCoinData(
                coinCode,
                localType.resIcon(),
                viewModel.originCoinsData.size > SwapFragment.MIN_COINS_TO_ENABLE_DIALOG_PICKER
            )
            coinInputLayout.setHelperText(
                getString(
                    R.string.swap_screen_balance_formatted,
                    coinBalance,
                    coinCode,
                    coinFee,
                    coinCodeFee
                )
            )
        }
        viewModel.todayLimitFormatted.observe(viewLifecycleOwner, txLimitValue::setText)
        viewModel.dailyLimitFormatted.observe(viewLifecycleOwner, dayLimitValue::setText)
        viewModel.txLimitFormatted.observe(viewLifecycleOwner, todayLimitValue::setText)
        viewModel.formattedCoinAmount.observe(viewLifecycleOwner, convertedValueValue::setText)
        viewModel.fee.observe(viewLifecycleOwner) { fee ->
            platformFeeTextView.text = getString(
                R.string.swap_screen_fee_formatted,
                fee.platformFeePercent.toStringCoin(),
                fee.platformFeeCoinAmount.toStringCoin(),
                fee.swapCoinCode
            ).toHtmlSpan()
        }
        viewModel.usdAmountError.observe(viewLifecycleOwner) { errorMessage ->
            binding.coinInputLayout.setErrorText(errorMessage, highlightAmount = true)
        }
        viewModel.sellLoadingData.listen(success = {
            val coin = viewModel.selectedCoin.value ?: return@listen
            findNavController().navigate(
                getString(
                    R.string.transactions_deeplink_format,
                    coin.code
                ).toUri()
            )
        })
        viewModel.rate.observe(viewLifecycleOwner) {
            rateTextView.text = getString(
                R.string.atm_sell_coin_rate,
                it.coinAmount.toStringCoin(),
                it.coinCode, it.usdAmount
            ).toHtmlSpan()
        }
    }

    override fun FragmentAtmSellBinding.initListeners() {
        updateLimitsButton.setOnClickListener {
            findNavController().navigate(getString(R.string.verification_info_deeplink).toUri())
        }
        coinInputLayout.setOnMaxClickListener { viewModel.setMaxSendAmount() }
        coinInputLayout.setOnCoinButtonClickListener(View.OnClickListener {
            val selectedCoin = viewModel.selectedCoin.value ?: return@OnClickListener
            val coinsToExclude = listOf(selectedCoin)
            val coinsList = viewModel.originCoinsData.toMutableList().apply {
                removeAll(coinsToExclude)
            }
            AlertHelper.showSelectCoinDialog(requireContext(), coinsList) {
                viewModel.setCoin(it)
            }
        })
        coinInputLayout.getEditText().addTextChangedListener(textWatcher)
        sellButton.setOnClickListener { viewModel.sell() }
    }
}