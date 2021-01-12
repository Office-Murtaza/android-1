package com.app.belcobtm.presentation.features.wallet.staking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.data.rest.transaction.response.StakeDetailsStatus
import com.app.belcobtm.databinding.FragmentStakingBinding
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import org.koin.android.viewmodel.ext.android.viewModel

class StakingFragment : BaseFragment<FragmentStakingBinding>() {
    private val viewModel: StakingViewModel by viewModel()
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val cryptoAmount: Double = editable.getDouble()
            binding.amountUsdView.text = if (cryptoAmount > 0) {
                getString(R.string.text_usd, (cryptoAmount * viewModel.getUsdPrice()).toStringUsd())
            } else {
                getString(R.string.text_usd, "0.0")
            }

            binding.stakeButtonView.isEnabled = binding.amountCryptoView.isNotBlank()
                    && binding.amountCryptoView.getDouble() > 0
        }
    )
    override var isMenuEnabled = true
    override val isHomeButtonEnabled = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        when (val loadingData = viewModel.transactionLiveData.value) {
            is LoadingData.Error -> when (loadingData.data) {
                StakingTransactionState.CREATE -> viewModel.stakeCreate(binding.amountCryptoView.getDouble())
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
        maxView.setOnClickListener {
            amountCryptoView.setText(viewModel.getMaxValue().toStringCoin())
        }
        stakeButtonView.setOnClickListener {
            amountCryptoView.clearError()
            if (isValid()) {
                viewModel.stakeCreate(amountCryptoView.getDouble())
            }
        }
        amountCryptoView.actionDoneListener {
            amountCryptoView.clearError()
            if (isValid()) {
                viewModel.stakeCreate(amountCryptoView.getDouble())
            }
        }
        cancelButtonView.setOnClickListener {
            if (isValid()) {
                viewModel.stakeCancel()
            }
        }
        unstakeButtonView.setOnClickListener {
            if (isValid()) {
                viewModel.unstakeCreateTransaction()
            }
        }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
    }


    override fun FragmentStakingBinding.initObservers() {
        viewModel.stakeDetailsLiveData.listen(success = {
            with(it) {
                priceUsdView.text = price.toStringUsd()
                balanceCryptoView.text = getString(
                    R.string.text_text,
                    balanceCoin.toStringCoin(),
                    LocalCoinType.CATM.name
                )
                balanceUsdView.text = getString(R.string.text_usd, balanceUsd.toStringUsd())
                reservedCryptoView.text = getString(
                    R.string.text_text,
                    reservedBalanceCoin.toStringCoin(),
                    reservedCode
                )
                reservedUsdView.text = getString(
                    R.string.text_usd,
                    reservedBalanceUsd.toStringUsd()
                )
                holdPeriodView.text = resources.getQuantityString(
                    R.plurals.staking_screen_time_value,
                    holdPeriod,
                    holdPeriod
                )
                when {
                    rewardsAmountAnnual != null -> rewardAnnualView.text = getString(
                        R.string.staking_screen_rewards_amount,
                        it.rewardsAmountAnnual?.toStringCoin(),
                        it.rewardsPercent
                    )
                    rewardsPercent != null -> rewardAnnualView.text = getString(
                        R.string.staking_screen_rewards_percent,
                        it.rewardsPercent
                    )
                    else -> {
                        rewardAnnualTitleView.hide()
                        rewardAnnualView.hide()
                    }
                }
                when(status) {
                    StakeDetailsStatus.NOT_EXIST, StakeDetailsStatus.WITHDRAWN -> {
                        editStakeGroupView.show()
                        stakeButtonView.show()
                        cancelButtonView.hide()
                        unstakeButtonView.hide()
                    }
                    StakeDetailsStatus.CREATE_PENDING -> {
                        cancelDateGroupView.hide()
                        untilWithdrawGroupView.hide()
                        updateStakingDetails()

                        stakeButtonView.hide()
                        cancelButtonView.show()
                        unstakeButtonView.hide()
                    }
                    StakeDetailsStatus.CANCEL -> {
                        cancelDate?.let { cancelDate ->
                            cancelDateView.text = cancelDate
                            cancelDateGroupView.show()
                        }
                        untilWithdraw?.let { untilWithdraw ->
                            untilWithdrawView.text =
                                    resources.getQuantityString(
                                            R.plurals.staking_screen_time_value,
                                            untilWithdraw,
                                            untilWithdraw
                                    )
                            untilWithdrawGroupView.toggle(untilWithdraw > 0)
                        }
                        updateStakingDetails()

                        stakeButtonView.hide()
                        cancelButtonView.hide()
                        unstakeButtonView.toggle(it.untilWithdraw ?: 0 == 0)
                    }
                    else -> {
                        editStakeGroupView.show()
                        amountGroupView.hide()
                        createDateGroupView.hide()
                        durationGroupView.hide()
                        rewardsGroupView.hide()
                        cancelDateGroupView.hide()
                        untilWithdrawGroupView.hide()
                        stakeButtonView.hide()
                        cancelButtonView.hide()
                        unstakeButtonView.hide()
                    }
                }
                when(status) {
                    StakeDetailsStatus.NOT_EXIST ->
                        updateStatusView(
                                R.color.colorStatusUnknown,
                                R.drawable.bg_status_unknown,
                                R.string.staking_screen_not_exist
                        )
                    StakeDetailsStatus.CREATE_PENDING ->
                        updateStatusView(
                                R.color.colorStatusCreated,
                                R.drawable.bg_status_created,
                                R.string.staking_screen_create_pending
                        )
                    StakeDetailsStatus.CREATED ->
                        updateStatusView(
                                R.color.colorStatusComplete,
                                R.drawable.bg_status_complete,
                                R.string.staking_screen_created
                        )
                    StakeDetailsStatus.CANCEL_PENDING ->
                        updateStatusView(
                                R.color.colorStatusCanceled,
                                R.drawable.bg_status_canceled,
                                R.string.staking_screen_cancel_pending
                        )
                    StakeDetailsStatus.CANCEL ->
                        updateStatusView(
                                R.color.colorStatusFail,
                                R.drawable.bg_status_fail,
                                R.string.staking_screen_canceled
                        )
                    StakeDetailsStatus.WITHDRAW_PENDING ->
                        updateStatusView(
                                R.color.colorStatusWithdrawPending,
                                R.drawable.bg_status_withdraw_pending,
                                R.string.staking_screen_withdraw_pending
                        )
                    StakeDetailsStatus.WITHDRAWN ->
                        updateStatusView(
                                R.color.colorStatusWithdrawn,
                                R.drawable.bg_status_withdrawn,
                                R.string.staking_screen_withdrawn
                        )
                }
            }
        })
        viewModel.transactionLiveData.listen(success = {
            popBackStack()
        })
    }

    private fun StakingScreenItem.updateStakingDetails() {
        binding.statusGroupView.show()
        binding.editStakeGroupView.hide()
        amount?.let { amount ->
            binding.amountView.text = getString(
                R.string.staking_screen_staked_amount,
                amount.toStringCoin()
            )
            binding.amountGroupView.show()
        }
        createDate?.let { createDate ->
            binding.createDateView.text = createDate
            binding.createDateGroupView.show()
        }
        duration?.let {
            binding.durationView.text =
                resources.getQuantityString(
                    R.plurals.staking_screen_time_value,
                    duration,
                    duration
                )
            binding.durationGroupView.show()
        }
        if (rewardsAmount != null && rewardsPercent != null) {
            binding.rewardsView.text = getString(
                R.string.staking_screen_rewards_amount,
                rewardsAmount.toStringCoin(),
                rewardsPercent
            )
            binding.rewardsGroupView.show()
        }
    }

    private fun isValid(): Boolean = when {
        viewModel.isNotEnoughETHBalanceForCATM() -> {
            showError(R.string.withdraw_screen_where_money_libovski)
            false
        }
        viewModel.getMaxValue() < binding.amountCryptoView.getDouble() -> {
            showError(R.string.balance_amount_exceeded)
            false
        }
        else -> true
    }

    private fun updateStatusView(textColor: Int, backgroundColor: Int, resText: Int) {
        binding.statusView.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        binding.statusView.setBackgroundResource(backgroundColor)
        binding.statusView.setText(resText)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentStakingBinding =
        FragmentStakingBinding.inflate(inflater, container, false)
}