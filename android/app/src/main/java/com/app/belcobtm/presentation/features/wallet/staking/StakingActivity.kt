package com.app.belcobtm.presentation.features.wallet.staking

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.core.ui.SmsDialogFragment
import com.app.belcobtm.presentation.features.HostActivity
import kotlinx.android.synthetic.main.activity_staking.*
import org.koin.android.viewmodel.ext.android.viewModel


class StakingActivity : BaseActivity() {
    private val viewModel: StakingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staking)
        initListeners()
        initObservers()
        initViews()
    }

    private fun initListeners() {
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
            stakeButtonView.isEnabled = if (loadingData is LoadingData.Success) {
                amountCryptoView.getDouble() > 0 && amountCryptoView.getDouble() <= loadingData.data.balanceCoin
            } else {
                false
            }
        }
    }

    private fun initObservers() {
        viewModel.stakeDetailsLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> with(loadingData.data) {
                    priceUsdView.text = getString(R.string.transaction_price_usd, price.toStringUsd())
                    balanceCryptoView.text = getString(
                        R.string.transaction_crypto_balance,
                        balanceCoin.toStringCoin(),
                        LocalCoinType.CATM.name
                    )
                    balanceUsdView.text = getString(R.string.transaction_price_usd, balanceUsd.toStringUsd())
                    stakedView.text = getString(R.string.staking_screen_staked_amount, staked.toStringCoin())
                    rewardsView.text =
                        getString(R.string.staking_screen_rewards_amount, rewardsAmount.toStringCoin(), rewardsPercent)
                    timeView.text = resources.getQuantityString(R.plurals.staking_screen_time_value, time, time)
                    stackingMinDays.text = stakingMinDays.toString()

                    editStakeGroupView.toggle(!isExist)
                    unstakeButtonView.toggle(isExist)
                    unstakeButtonView.isEnabled = isUnStakeAvailable
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.TokenError -> {
                            val intent = Intent(this, HostActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
        viewModel.transactionLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    when (loadingData.data) {
                        StakingViewModel.TransactionState.STAKE_CREATE,
                        StakingViewModel.TransactionState.UNSTAKE_CREATE -> showSmsDialog()
                        StakingViewModel.TransactionState.STAKE_COMPLETE,
                        StakingViewModel.TransactionState.UNSTAKE_COMPLETE -> finish()
                    }
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.TokenError -> {
                            val intent = Intent(this, HostActivity::class.java)
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
                            showError(loadingData.errorType.message)
                        }
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
    }

    private fun initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showSmsDialog(errorMessage: String? = null) {
        val fragment = SmsDialogFragment()
        fragment.arguments = bundleOf(SmsDialogFragment.TAG_ERROR to errorMessage)
        fragment.show(supportFragmentManager, SmsDialogFragment::class.simpleName)
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