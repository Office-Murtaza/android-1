package com.app.belcobtm.presentation.features.wallet.staking

import android.content.Intent
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.SmsDialogFragment
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
            if (viewModel.isNotEnoughETHBalanceForCATM()) {
                showError(R.string.withdraw_screen_where_money_libovski)
            } else {
                viewModel.stakeCreateTransaction(amount)
            }
        }
        unstakeButtonView.setOnClickListener { viewModel.unstakeCreateTransaction() }
        amountCryptoView.editText?.afterTextChanged {
            val loadingData = viewModel.stakeDetailsLiveData.value
            fiatView.text = "${it.toString().toDouble() * coef}"
            stakeButtonView.isEnabled = if (loadingData is LoadingData.Success) {
                amountCryptoView.getDouble() > 0 && amountCryptoView.getDouble() <= loadingData.data.balanceCoin
            } else {
                false
            }
        }
    }

    override fun initObservers() {
        viewModel.stakeDetailsLiveData.listen(success = {
            coef = it.balanceUsd / it.balanceCoin
            priceUsdView.text = getString(R.string.text_usd, it.price.toStringUsd())
            balanceCryptoView.text = getString(
                R.string.text_text,
                it.balanceCoin.toStringCoin(),
                LocalCoinType.CATM.name
            )
            balanceUsdView.text = getString(R.string.text_usd, it.balanceUsd.toStringUsd())
            stakedView.text = getString(R.string.staking_screen_staked_amount, it.staked.toStringCoin())
            rewardsView.text =
                getString(R.string.staking_screen_rewards_amount, it.rewardsAmount.toStringCoin(), it.rewardsPercent)
            timeView.text = resources.getQuantityString(R.plurals.staking_screen_time_value, it.time, it.time)
            rewardAnnualView.text = getString(R.string.staking_screen_rewards_amount, it.rewardsAmount.toStringCoin(), it.rewardsPercent)
            stackingMinDays.text = it.stakingMinDays.toString()

            amountCryptoView.toggle(!it.isExist)
            maxView.toggle(!it.isExist)
            fiatView.toggle(!it.isExist)
            rewardsTitleView.toggle(it.isExist)
            rewardsView.toggle(it.isExist)
            editStakeGroupView.toggle(!it.isExist)
            unstakeButtonView.toggle(it.isExist && it.time >= it.stakingMinDays)
            unstakeButtonView.isEnabled = it.isUnStakeAvailable
            showContent()
        })
        viewModel.transactionLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> showLoading()
                is LoadingData.Success -> {
                    showContent()
                    when (loadingData.data) {
                        StakingViewModel.TransactionState.STAKE_CREATE,
                        StakingViewModel.TransactionState.UNSTAKE_CREATE -> showSmsDialog()
                        StakingViewModel.TransactionState.STAKE_COMPLETE,
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
                        is Failure.MessageError -> if (
                            loadingData.data == StakingViewModel.TransactionState.STAKE_COMPLETE ||
                            loadingData.data == StakingViewModel.TransactionState.UNSTAKE_COMPLETE
                        ) {
                            showSmsDialog(loadingData.errorType.message)
                        } else {
                            showError(loadingData.errorType.message.orEmpty())
                        }
                        else -> showError(R.string.error_something_went_wrong)
                    }
                }
            }
        })
    }

    private fun showSmsDialog(errorMessage: String? = null) {
        val fragment = SmsDialogFragment()
        fragment.arguments = bundleOf(SmsDialogFragment.TAG_ERROR to errorMessage)
        fragment.show(childFragmentManager, SmsDialogFragment::class.simpleName)
        fragment.setDialogListener { smsCode ->
            val loadingData = viewModel.transactionLiveData.value
            when {
                loadingData is LoadingData.Success && loadingData.data == StakingViewModel.TransactionState.STAKE_CREATE ->
                    viewModel.stakeCompleteTransaction(smsCode)
                loadingData is LoadingData.Success && loadingData.data == StakingViewModel.TransactionState.UNSTAKE_CREATE ->
                    viewModel.unstakeCompleteTransaction(smsCode, amountCryptoView.getDouble())
                else -> Unit
            }
        }
    }
}