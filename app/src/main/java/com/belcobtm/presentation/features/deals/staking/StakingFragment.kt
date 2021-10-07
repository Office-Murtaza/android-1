package com.belcobtm.presentation.features.deals.staking

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.get
import com.belcobtm.R
import com.belcobtm.data.rest.transaction.response.StakeDetailsStatus
import com.belcobtm.databinding.FragmentStakingBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.watcher.DoubleTextWatcher
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class StakingFragment : BaseFragment<FragmentStakingBinding>() {
    private val viewModel: StakingViewModel by viewModel()
    private val currencyFormatter: Formatter<Double> by inject(
        named(DoubleCurrencyPriceFormatter.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val stakingDetails = viewModel.stakeDetailsLiveData.value
            if (stakingDetails !is LoadingData.Success) {
                // random crash which was caused by accessing viewmodel's lateinit properties
                // before their initialization
                return@DoubleTextWatcher
            }
            if (stakingDetails.data.status == StakeDetailsStatus.NOT_EXIST
                || stakingDetails.data.status == StakeDetailsStatus.WITHDRAWN
            ) {
                val cryptoAmount: Double = editable.getDouble()
                val annualRewardPercents = stakingDetails.data.rewardsPercentAnnual ?: 0.0
                val annualRewardData = (cryptoAmount * annualRewardPercents / 100).toStringCoin()
                // during stake creaation there should be some calculation like
                // converted values based on input
                binding.tvUsdConvertedValue.text =
                    currencyFormatter.format(cryptoAmount * viewModel.getUsdPrice())
                binding.tvAnualRewardAmountValue.text = "+".plus(
                    getString(
                        R.string.text_text,
                        annualRewardData,
                        LocalCoinType.CATM.name
                    )
                )
                binding.createButtonView.isEnabled =
                    binding.coinInputLayout.getEditText().text.isNotBlank() &&
                            binding.coinInputLayout.getEditText().text.getDouble() > 0
                if (cryptoAmount > 0) {
                    val text = cryptoAmount.toStringCoin()
                    binding.coinInputLayout.getEditText().setText(text)
                    binding.coinInputLayout.getEditText().setSelection(text.length)
                }
                // "0" should always be displayed for user
                // even through they try to clear the input
                if (editable.isEmpty()) {
                    editable.insert(0, "0")
                }
            }
        }
    )
    override var isMenuEnabled = true
    override val isHomeButtonEnabled = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        when (val loadingData = viewModel.transactionLiveData.value) {
            is LoadingData.Error -> when (loadingData.data) {
                StakingTransactionState.CREATE -> viewModel.stakeCreate(
                    binding.coinInputLayout.getEditText().text.getDouble()
                )
                StakingTransactionState.CANCEL -> viewModel.stakeCancel()
                StakingTransactionState.WITHDRAW -> viewModel.unstakeCreateTransaction()
            }
            else -> viewModel.loadData()
        }
    }

    override fun FragmentStakingBinding.initViews() {
        setToolbarTitle(R.string.staking_screen_title)
        binding.coinInputLayout.getEditText().setText("0")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.post {
            binding.coinInputLayout.getEditText().requestFocus()
            binding.coinInputLayout.getEditText().setSelection(
                binding.coinInputLayout.getEditText().length()
            )
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showSoftInput(binding.coinInputLayout.getEditText(), 0)
        }
    }

    override fun FragmentStakingBinding.initListeners() {
        binding.coinInputLayout.setOnMaxClickListener {
            binding.coinInputLayout.getEditText().setText(viewModel.getMaxValue().toStringCoin())
        }
        createButtonView.setOnClickListener {
            binding.coinInputLayout.setErrorText(null, false)
            if (isValid()) {
                viewModel.stakeCreate(binding.coinInputLayout.getEditText().text.getDouble())
            }
        }
        binding.coinInputLayout.getEditText().actionDoneListener {
            hideKeyboard()
        }
        cancelButtonView.setOnClickListener {
            if (isValid()) {
                viewModel.stakeCancel()
            }
        }
        withdrawButtonView.setOnClickListener {
            if (isValid()) {
                viewModel.unstakeCreateTransaction()
            }
        }
        binding.coinInputLayout.getEditText()
            .addTextChangedListener(doubleTextWatcher.firstTextWatcher)
    }

    override fun FragmentStakingBinding.initObservers() {
        viewModel.stakeDetailsLiveData.listen(success = {
            with(it) {
                when (status) {
                    // create
                    StakeDetailsStatus.NOT_EXIST,
                    StakeDetailsStatus.WITHDRAWN -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = true
                        coinInputLayout.setMaxVisible(true)
                        coinInputLayout.setHelperText2(null)
                        // left row
                        tvAnualPercent.show()
                        tvAnualPercentValue.show()
                        tvCancelHoldPeriod.show()
                        tvCancelHoldPeriodValue.show()
                        tvRewards.hide()
                        tvRewardsValue.hide()
                        // right row
                        tvUsdConverted.show()
                        tvUsdConvertedValue.show()
                        tvAnualRewardAmount.show()
                        tvAnualRewardAmountValue.show()
                        tvCreated.hide()
                        tvCreatedValue.hide()
                        tvCanceled.hide()
                        tvCanceledValue.hide()
                        tvDuration.hide()
                        tvDurationValue.hide()
                        // others
                        thirdDivider.hide()
                        tvWithdraw.hide()
                        createButtonView.show()
                        cancelButtonView.hide()
                        withdrawButtonView.hide()
                    }
                    // cancel
                    StakeDetailsStatus.CREATE_PENDING -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxVisible(false)
                        coinInputLayout.setHelperText2(getString(R.string.staking_screen_staked))
                        // left row
                        tvAnualPercent.show()
                        tvAnualPercentValue.show()
                        tvCancelHoldPeriod.show()
                        tvCancelHoldPeriodValue.show()
                        tvRewards.show()
                        tvRewardsValue.show()
                        // right row
                        tvUsdConverted.hide()
                        tvUsdConvertedValue.hide()
                        tvAnualRewardAmount.hide()
                        tvAnualRewardAmountValue.hide()
                        tvCreated.show()
                        tvCreatedValue.show()
                        tvCanceled.hide()
                        tvCanceledValue.hide()
                        tvDuration.show()
                        tvDurationValue.show()
                        // others
                        thirdDivider.hide()
                        tvWithdraw.hide()
                        createButtonView.hide()
                        cancelButtonView.hide()
                        withdrawButtonView.hide()
                    }
                    StakeDetailsStatus.CREATED -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxVisible(false)
                        coinInputLayout.setHelperText2(getString(R.string.staking_screen_staked))
                        // left row
                        tvAnualPercent.show()
                        tvAnualPercentValue.show()
                        tvCancelHoldPeriod.show()
                        tvCancelHoldPeriodValue.show()
                        tvRewards.show()
                        tvRewardsValue.show()
                        // right row
                        tvUsdConverted.hide()
                        tvUsdConvertedValue.hide()
                        tvAnualRewardAmount.hide()
                        tvAnualRewardAmountValue.hide()
                        tvCreated.show()
                        tvCreatedValue.show()
                        tvCanceled.hide()
                        tvCanceledValue.hide()
                        tvDuration.show()
                        tvDurationValue.show()
                        // others
                        thirdDivider.hide()
                        tvWithdraw.hide()
                        createButtonView.hide()
                        cancelButtonView.show()
                        withdrawButtonView.hide()
                    }
                    StakeDetailsStatus.CANCEL_PENDING -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxVisible(false)
                        coinInputLayout.setHelperText2(getString(R.string.staking_screen_staked))
                        // left row
                        tvAnualPercent.show()
                        tvAnualPercentValue.show()
                        tvCancelHoldPeriod.show()
                        tvCancelHoldPeriodValue.show()
                        tvRewards.show()
                        tvRewardsValue.show()
                        // right row
                        tvUsdConverted.hide()
                        tvUsdConvertedValue.hide()
                        tvAnualRewardAmount.hide()
                        tvAnualRewardAmountValue.hide()
                        tvCreated.show()
                        tvCreatedValue.show()
                        tvCanceled.hide()
                        tvCanceledValue.hide()
                        tvDuration.show()
                        tvDurationValue.show()
                        // others
                        thirdDivider.hide()
                        tvWithdraw.hide()
                        createButtonView.hide()
                        cancelButtonView.hide()
                        withdrawButtonView.hide()
                    }
                    // WITHDRAW
                    StakeDetailsStatus.CANCEL -> {
                        val showWithdrawButton = untilWithdraw == 0
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxVisible(false)
                        coinInputLayout.setHelperText2(getString(R.string.staking_screen_staked))
                        // left row
                        tvAnualPercent.show()
                        tvAnualPercentValue.show()
                        tvCancelHoldPeriod.show()
                        tvCancelHoldPeriodValue.show()
                        tvRewards.show()
                        tvRewardsValue.show()
                        // right row
                        tvUsdConverted.hide()
                        tvUsdConvertedValue.hide()
                        tvAnualRewardAmount.hide()
                        tvAnualRewardAmountValue.hide()
                        tvCreated.show()
                        tvCreatedValue.show()
                        tvCanceled.show()
                        tvCanceledValue.show()
                        tvDuration.show()
                        tvDurationValue.show()
                        // others
                        thirdDivider.show()
                        tvWithdraw.toggle(!showWithdrawButton)
                        createButtonView.hide()
                        cancelButtonView.hide()
                        withdrawButtonView.toggle(showWithdrawButton)
                    }
                    StakeDetailsStatus.WITHDRAW_PENDING -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxVisible(false)
                        coinInputLayout.setHelperText2(getString(R.string.staking_screen_staked))
                        // left row
                        tvAnualPercent.show()
                        tvAnualPercentValue.show()
                        tvCancelHoldPeriod.show()
                        tvCancelHoldPeriodValue.show()
                        tvRewards.show()
                        tvRewardsValue.show()
                        // right row
                        tvUsdConverted.hide()
                        tvUsdConvertedValue.hide()
                        tvAnualRewardAmount.hide()
                        tvAnualRewardAmountValue.hide()
                        tvCreated.show()
                        tvCreatedValue.show()
                        tvCanceled.show()
                        tvCanceledValue.show()
                        tvDuration.show()
                        tvDurationValue.show()
                        // others
                        thirdDivider.show()
                        tvWithdraw.hide()
                        createButtonView.hide()
                        cancelButtonView.hide()
                        withdrawButtonView.hide()
                    }
                    else -> {
                        createButtonView.hide()
                        cancelButtonView.hide()
                        withdrawButtonView.hide()
                    }
                }
            }
        })
        viewModel.transactionLiveData.observe(viewLifecycleOwner, { data ->
            when (data) {
                is LoadingData.Loading -> showLoading()
                is LoadingData.Success -> {
                    val stringResource = when (data.data) {
                        StakingTransactionState.CREATE -> R.string.staking_screen_success_created
                        StakingTransactionState.CANCEL -> R.string.staking_screen_success_canceled
                        StakingTransactionState.WITHDRAW -> R.string.staking_screen_success_withdrawn
                    }
                    Toast.makeText(requireContext(), stringResource, Toast.LENGTH_LONG).show()
                }
                is LoadingData.Error -> {
                    val stringResource = when (data.data!!) {
                        StakingTransactionState.CREATE -> R.string.staking_screen_fail_create
                        StakingTransactionState.CANCEL -> R.string.staking_screen_fail_cancel
                        StakingTransactionState.WITHDRAW -> R.string.staking_screen_fail_withdraw
                    }
                    Toast.makeText(requireContext(), stringResource, Toast.LENGTH_LONG).show()
                    showContent()
                }
            }
        })
    }

    private fun StakingScreenItem.invalidateStakingDetails() {
        // Staking annual percent
        if (rewardsPercentAnnual != null) {
            binding.tvAnualPercentValue.text = getString(
                R.string.staking_screen_rewards_percent,
                rewardsPercentAnnual.toStringPercents()
            )
        }
        // Cancel hold period
        binding.tvCancelHoldPeriodValue.text = resources.getQuantityString(
            R.plurals.staking_screen_time_value,
            cancelHoldPeriod,
            cancelHoldPeriod
        )
        // Rewards
        if (rewardsAmount != null && rewardsPercent != null) {
            binding.tvRewardsValue.text = getString(
                R.string.staking_screen_rewards_amount,
                rewardsAmount.toStringCoin(),
                rewardsPercent.toStringPercents()
            )
        }
        // Usd converted value
        if (amount != null) {
            val usdValue = amount * price
            binding.tvUsdConvertedValue.text = currencyFormatter.format(usdValue)
        }
        // Anual reward
        if (rewardsAmountAnnual != null) {
            binding.tvAnualRewardAmountValue.text = getString(
                R.string.text_text,
                rewardsAmountAnnual.toStringCoin(),
                LocalCoinType.CATM.name
            )
        }
        // create data
        if (createDate != null) {
            binding.tvCreatedValue.text = createDate
        }
        // cancel data
        if (cancelDate != null) {
            binding.tvCanceledValue.text = cancelDate
        }
        if (duration != null) {
            binding.tvDurationValue.text = resources.getQuantityString(
                R.plurals.staking_screen_time_value,
                duration,
                duration
            )
        }
        if (untilWithdraw != null) {
            binding.tvWithdraw.text = resources.getQuantityString(
                R.plurals.staking_screen_time_withdraw,
                untilWithdraw,
                untilWithdraw
            )
        }
        // header
        binding.tvStakeRate.text = getString(
            R.string.staking_screen_catm_price_formatted,
            currencyFormatter.format(price)
        ).toHtmlSpan()
        binding.coinInputLayout.setHelperText(
            getString(
                R.string.staking_screen_balance_formatted,
                balanceCoin.toStringCoin(),
                LocalCoinType.CATM.name,
                ethFee.toStringCoin(),
                LocalCoinType.ETH.name
            )
        )
        binding.coinInputLayout.setCoinData(
            LocalCoinType.CATM.name, LocalCoinType.CATM.resIcon(), false
        )
        if (amount != null
            && status != StakeDetailsStatus.NOT_EXIST
            && status != StakeDetailsStatus.WITHDRAWN
        ) {
            binding.coinInputLayout.getEditText().setText(amount.toStringCoin())
        } else {
            binding.tvUsdConvertedValue.text = currencyFormatter.format(0.0)
            binding.coinInputLayout.getEditText().setText("0")
            binding.coinInputLayout.getEditText()
                .setSelection(binding.coinInputLayout.getEditText().length())
        }
    }

    private fun isValid(): Boolean = when {
        viewModel.isNotEnoughETHBalanceForCATM() -> {
            binding.coinInputLayout.setErrorText(
                getString(R.string.withdraw_screen_where_money_libovski), false
            )
            false
        }
        viewModel.getMaxValue() < binding.coinInputLayout.getEditText().text.getDouble() -> {
            binding.coinInputLayout.setErrorText(
                getString(R.string.balance_amount_exceeded), true
            )
            false
        }
        else -> {
            binding.coinInputLayout.setErrorText(null, false)
            true
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStakingBinding =
        FragmentStakingBinding.inflate(inflater, container, false)
}
