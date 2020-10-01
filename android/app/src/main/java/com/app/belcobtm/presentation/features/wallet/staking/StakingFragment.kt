package com.app.belcobtm.presentation.features.wallet.staking

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.HostActivity
import kotlinx.android.synthetic.main.fragment_staking.*
import org.koin.android.viewmodel.ext.android.viewModel


class StakingFragment : BaseFragment() {
    private val viewModel: StakingViewModel by viewModel()
    private var coef = 0.0

    override val resourceLayout = R.layout.fragment_staking
    override var isMenuEnabled = true
    override val isHomeButtonEnabled = true

    override fun initViews() {
        super.initViews()
        setToolbarTitle(R.string.staking_screen_title)
    }

    override fun initListeners() {
        maxView.setOnClickListener {
            amountCryptoView.setText(viewModel.getMaxValue().toStringCoin())
        }
        stakeButtonView.setOnClickListener {
            val amount = amountCryptoView.getDouble()
            when {
                viewModel.isNotEnoughETHBalanceForCATM() -> {
                    showError(R.string.withdraw_screen_where_money_libovski)
                }
                viewModel.getMaxValue() < amount -> {
                    showError(R.string.withdraw_screen_max_exceeded)
                }
                else -> {
                    viewModel.stakeCreateTransaction(amount)
                }
            }
        }
        cancelButtonView.setOnClickListener {
            viewModel.stakeCancelCreateTransaction()
        }
        unstakeButtonView.setOnClickListener { viewModel.unstakeCreateTransaction() }
        amountCryptoView.editText?.afterTextChanged {
            val loadingData = viewModel.stakeDetailsLiveData.value
            val amount = try {
                it.toString().toDouble()
            } catch (e: Exception) {
                0.0
            }
            fiatView.text = if (amount > 0) {
                "${(amount * coef).toStringUsd()}$"
            } else {
                getText(R.string.staking_screen_null_price)
            }
            stakeButtonView.isEnabled = if (loadingData is LoadingData.Success) {
                try {
                    amountCryptoView.getDouble() > 0 && amountCryptoView.getDouble() <= loadingData.data.balanceCoin
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        }
    }

    override fun initObservers() {
        viewModel.stakeDetailsLiveData.listen(success = {
            with(it) {
                coef = price
                priceUsdView.text = price.toStringUsd()
                balanceCryptoView.text = getString(
                    R.string.text_text,
                    balanceCoin.toStringCoin(),
                    LocalCoinType.CATM.name
                )
                balanceUsdView.text = getString(R.string.text_usd, balanceUsd.toStringUsd())

                when (status) {
                    StakeStatus.CREATED -> {
                        statusTitleView.toggle(true)
                        statusView.toggle(true)
                        statusView.text = getString(R.string.staking_screen_created)
                        setStatusColors(R.color.pending_border, R.color.pending_background)
                        editStakeGroupView.toggle(false)
                        cancelButtonView.toggle(true)
                        unstakeButtonView.toggle(false)
                    }
                    StakeStatus.CANCELED ->  {
                        statusTitleView.toggle(true)
                        statusView.toggle(true)
                        statusView.text = getString(R.string.staking_screen_canceled)
                        setStatusColors(R.color.staking_canceled_border, R.color.staking_canceled_background)
                        editStakeGroupView.toggle(false)
                        cancelButtonView.toggle(false)
                        unstakeButtonView.toggle(it.untilWithdraw?: 0 <= 0)
                    }
                    else -> {
                        statusTitleView.toggle(false)
                        statusView.toggle(false)
                        cancelButtonView.toggle(false)
                        unstakeButtonView.toggle(false)
                        editStakeGroupView.toggle(true)
                    }
                }

                val created = status == StakeStatus.CREATED || status == StakeStatus.CANCELED
                val canceled = status == StakeStatus.CANCELED
                if (created && amount != null) {
                    amountView.text = getString(R.string.staking_screen_staked_amount, amount.toStringCoin())
                }
                amountTitleView.toggle(created)
                amountView.toggle(created)

                if (created && rewardsAmount != null && rewardsPercent != null) {
                    rewardsView.text =
                        getString(
                            R.string.staking_screen_rewards_amount,
                            rewardsAmount.toStringCoin(),
                            rewardsPercent
                        )
                }
                rewardsView.toggle(created)
                rewardsTitleView.toggle(created)

                rewardAnnualView.text = getString(
                    R.string.staking_screen_rewards_amount,
                    it.rewardsAmountAnnual.toStringCoin(),
                    it.rewardsPercent)

                if (created && createDate != null) {
                    createDateView.text = createDate
                }
                createDateView.toggle(createDate != null)
                createDateTitleView.toggle(createDate != null)

                if (canceled && cancelDate != null) {
                    cancelDateView.text = cancelDate
                }
                cancelDateView.toggle(canceled)
                cancelDateTitleView.toggle(canceled)

                if (created && duration != null) {
                    durationView.text = resources.getQuantityString(R.plurals.staking_screen_time_value, duration, duration)
                }
                durationView.toggle(created)
                durationTitleView.toggle(created)

                cancelPeriodView.text = resources.getQuantityString(R.plurals.staking_screen_time_value, cancelPeriod, cancelPeriod)

                if (canceled && untilWithdraw != null) {
                    untilWithdrawView.text = resources.getQuantityString(R.plurals.staking_screen_time_value, untilWithdraw, untilWithdraw)
                    unstakeButtonView.isEnabled = untilWithdraw <= 0
                }
                untilWithdrawView.toggle(canceled)
                untilWithdrawTitleView.toggle(canceled)
            }

            showContent()
        })
        viewModel.transactionLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> showLoading()
                is LoadingData.Success -> {
                    showContent()
                    when (loadingData.data) {
                        StakingViewModel.TransactionState.STAKE_COMPLETE,
                        StakingViewModel.TransactionState.STAKE_CANCEL_COMPLETE,
                        StakingViewModel.TransactionState.UNSTAKE_COMPLETE -> popBackStack()
                    }
                }
                is LoadingData.Error -> {
                    showContent()
                    when (loadingData.errorType) {
                        is Failure.TokenError -> {
                            val intent = Intent(requireContext(), HostActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        is Failure.MessageError -> showError(loadingData.errorType.message.orEmpty())
                        else -> showError(R.string.error_something_went_wrong)
                    }
                }
            }
        })
    }

    private fun setStatusColors(strokeColor: Int, backgroundColor: Int) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
        shape.setStroke(3, ContextCompat.getColor(requireContext(), strokeColor))
        shape.setColor(ContextCompat.getColor(requireContext(), backgroundColor))
        statusView.setTextColor(ContextCompat.getColor(requireContext(), strokeColor))
        statusView.background = shape
    }
}