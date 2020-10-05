package com.app.belcobtm.presentation.features.wallet.staking

import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.domain.transaction.type.StakeStatus
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import kotlinx.android.synthetic.main.fragment_staking.*
import org.koin.android.viewmodel.ext.android.viewModel

class StakingFragment : BaseFragment() {
    private val viewModel: StakingViewModel by viewModel()
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val fromMaxValue = viewModel.getMaxValue()
            val fromCoinAmountTemporary = editable.getDouble()
            val cryptoAmount: Double

            if (fromCoinAmountTemporary >= fromMaxValue) {
                cryptoAmount = fromMaxValue
                editable.clear()
                editable.insert(0, cryptoAmount.toStringCoin())
            } else {
                cryptoAmount = fromCoinAmountTemporary
            }

            amountUsdView.text = if (cryptoAmount > 0) {
                getString(R.string.text_usd, (cryptoAmount * viewModel.getUsdPrice()).toStringUsd())
            } else {
                getString(R.string.text_usd, "0.0")
            }

            stakeButtonView.isEnabled = amountCryptoView.isNotBlank()
                    && amountCryptoView.getDouble() > 0
                    && amountCryptoView.getDouble() <= viewModel.getMaxValue()
        }
    )
    override val resourceLayout = R.layout.fragment_staking
    override var isMenuEnabled = true
    override val isHomeButtonEnabled = true

    override fun initViews() {
        super.initViews()
        setToolbarTitle(R.string.staking_screen_title)
    }

    override fun initListeners() {
        maxView.setOnClickListener { amountCryptoView.setText(viewModel.getMaxValue().toStringCoin()) }
        stakeButtonView.setOnClickListener {
            amountCryptoView.clearError()
            when {
                viewModel.isNotEnoughETHBalanceForCATM() -> amountCryptoView.showError(R.string.withdraw_screen_where_money_libovski)
                viewModel.getMaxValue() < amountCryptoView.getDouble() -> amountCryptoView.showError(R.string.withdraw_screen_max_exceeded)
                else -> viewModel.stakeCreate(amountCryptoView.getDouble())
            }
        }
        cancelButtonView.setOnClickListener { viewModel.stakeCancel() }
        unstakeButtonView.setOnClickListener { viewModel.unstakeCreateTransaction() }
        amountCryptoView.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
    }


    override fun initObservers() {
        viewModel.stakeDetailsLiveData.listen(success = {
            with(it) {
                priceUsdView.text = price.toStringUsd()
                balanceCryptoView.text = getString(
                    R.string.text_text,
                    balanceCoin.toStringCoin(),
                    LocalCoinType.CATM.name
                )
                balanceUsdView.text = getString(R.string.text_usd, balanceUsd.toStringUsd())
                cancelPeriodView.text =
                    resources.getQuantityString(R.plurals.staking_screen_time_value, cancelPeriod, cancelPeriod)
                rewardAnnualView.text = getString(
                    R.string.staking_screen_rewards_amount,
                    it.rewardsAmountAnnual.toStringCoin(),
                    it.rewardsPercent
                )

                when (status) {
                    StakeStatus.CREATED,
                    StakeStatus.CANCELED -> {
                        if (status == StakeStatus.CREATED) {
                            updateStatusView(
                                R.color.colorStatusCreated,
                                R.drawable.bg_status_created,
                                R.string.staking_screen_created
                            )
                            cancelDateGroupView.hide()
                            untilWithdrawGroupView.hide()
                            cancelButtonView.show()
                            unstakeButtonView.hide()
                        } else {
                            updateStatusView(
                                R.color.colorStatusCanceled,
                                R.drawable.bg_status_canceled,
                                R.string.staking_screen_canceled
                            )
                            cancelDate?.let { cancelDate ->
                                cancelDateView.text = cancelDate
                                cancelDateGroupView.show()
                            }
                            untilWithdraw?.let {
                                untilWithdrawView.text =
                                    resources.getQuantityString(
                                        R.plurals.staking_screen_time_value,
                                        untilWithdraw,
                                        untilWithdraw
                                    )
                                unstakeButtonView.isEnabled = untilWithdraw <= 0
                                untilWithdrawGroupView.show()
                            }
                            cancelButtonView.hide()
                            unstakeButtonView.toggle(it.untilWithdraw ?: 0 <= 0)
                        }

                        statusGroupView.show()
                        editStakeGroupView.hide()
                        amount?.let { amount ->
                            amountView.text = getString(R.string.staking_screen_staked_amount, amount.toStringCoin())
                            amountGroupView.show()
                        }
                        createDate?.let { createDate ->
                            createDateView.text = createDate
                            createDateGroupView.show()
                        }
                        duration?.let {
                            durationView.text =
                                resources.getQuantityString(R.plurals.staking_screen_time_value, duration, duration)
                            durationGroupView.show()
                        }
                        if (rewardsAmount != null && rewardsPercent != null) {
                            rewardsView.text = getString(
                                R.string.staking_screen_rewards_amount,
                                rewardsAmount.toStringCoin(),
                                rewardsPercent
                            )
                            rewardsGroupView.show()
                        }
                    }
                    StakeStatus.WITHDRAWN -> {
                        statusGroupView.hide()
                        editStakeGroupView.show()
                        amountGroupView.hide()
                        createDateGroupView.hide()
                        durationGroupView.hide()
                        rewardsGroupView.hide()
                        cancelDateGroupView.hide()
                        untilWithdrawGroupView.hide()
                        cancelButtonView.hide()
                        unstakeButtonView.hide()
                    }
                    else -> {
                        //empty?
                    }
                }

            }

            showContent()
        })
        viewModel.transactionLiveData.listen({
            popBackStack()
        })
    }

    private fun updateStatusView(textColor: Int, backgroundColor: Int, resText: Int) {
        statusView.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        statusView.setBackgroundResource(backgroundColor)
        statusView.setText(resText)
    }
}