package com.app.belcobtm.presentation.features.deals.staking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.data.rest.transaction.response.StakeDetailsStatus
import com.app.belcobtm.databinding.FragmentStakingBinding
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class StakingFragment : BaseFragment<FragmentStakingBinding>() {
    private val viewModel: StakingViewModel by viewModel()
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val cryptoAmount: Double = editable.getDouble()
            binding.coinInputLayout.getEditText().setText(
                if (cryptoAmount > 0) {
                    (cryptoAmount * viewModel.getUsdPrice()).toStringUsd()
                } else {
                    "0.0"
                }
            )
            binding.createButtonView.isEnabled =
                binding.coinInputLayout.getEditText().text.isNotBlank() &&
                        binding.coinInputLayout.getEditText().text.getDouble() > 0
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
    }

    override fun FragmentStakingBinding.initListeners() {
        binding.coinInputLayout.setOnMaxClickListener(View.OnClickListener {
            binding.coinInputLayout.getEditText().setText(viewModel.getMaxValue().toStringCoin())
        })
        createButtonView.setOnClickListener {
            binding.coinInputLayout.setErrorText(null)
            if (isValid()) {
                viewModel.stakeCreate(binding.coinInputLayout.getEditText().text.getDouble())
            }
        }
        binding.coinInputLayout.getEditText().actionDoneListener {
            binding.coinInputLayout.setErrorText(null)
            if (isValid()) {
                viewModel.stakeCreate(binding.coinInputLayout.getEditText().text.getDouble())
            }
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
                    StakeDetailsStatus.CREATE_PENDING,
                    StakeDetailsStatus.CREATED,
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
                        cancelButtonView.show()
                        withdrawButtonView.hide()
                    }
                    // WITHDRAW
                    StakeDetailsStatus.CANCEL,
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
                        tvWithdraw.show()
                        createButtonView.hide()
                        cancelButtonView.hide()
                        withdrawButtonView.show()
                    }
                    else -> {
                        createButtonView.hide()
                        cancelButtonView.hide()
                        withdrawButtonView.hide()
                    }
                }
            }
        })
        viewModel.transactionLiveData.listen(success = {
            popBackStack()
        })
    }

    private fun StakingScreenItem.invalidateStakingDetails() {
        // Staking annual percent
        if (rewardsPercentAnnual != null) {
            binding.tvAnualPercentValue.text = getString(
                R.string.staking_screen_rewards_percent,
                rewardsPercentAnnual
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
                rewardsPercent
            )
        }
        // Usd converted value
        if (amount != null) {
            val usdValue = amount * price
            binding.tvAnualRewardAmountValue.text = getString(
                R.string.staking_screen_usd_formatted,
                usdValue.toStringUsd()
            )
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
            val days = TimeUnit.SECONDS.toDays(duration.toLong()).toInt()
            binding.tvDurationValue.text = resources.getQuantityString(
                R.plurals.staking_screen_time_value,
                days,
                days
            )
        }
        if (untilWithdraw != null) {
            binding.tvWithdraw.text = resources.getQuantityString(
                R.plurals.staking_screen_time_value,
                untilWithdraw,
                untilWithdraw
            )
        }
        // header
        binding.tvStakeRate.text = getString(
            R.string.staking_screen_catm_price_formatted,
            price.toStringUsd()
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
        if (amount != null) {
            binding.coinInputLayout.getEditText().setText(amount.toStringCoin())
        }
    }

    private fun isValid(): Boolean = when {
        viewModel.isNotEnoughETHBalanceForCATM() -> {
            showError(R.string.withdraw_screen_where_money_libovski)
            false
        }
        viewModel.getMaxValue() < binding.coinInputLayout.getEditText().text.getDouble() -> {
            showError(R.string.balance_amount_exceeded)
            false
        }
        else -> true
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStakingBinding =
        FragmentStakingBinding.inflate(inflater, container, false)
}