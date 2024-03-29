package com.belcobtm.presentation.screens.services.staking

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.belcobtm.R
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.data.rest.transaction.response.StakeDetailsStatus
import com.belcobtm.databinding.FragmentStakingBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.getDouble
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.tools.extensions.toHtmlSpan
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.toStringPercents
import com.belcobtm.presentation.tools.extensions.toggle
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class StakingFragment : BaseFragment<FragmentStakingBinding>() {

    private val viewModel: StakingViewModel by viewModel()
    private val currencyFormatter: Formatter<Double> by inject(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
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
            }
        }
    )
    override var isMenuEnabled = true
    override val isBackButtonEnabled = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        when (val loadingData = viewModel.transactionLiveData.value) {
            is LoadingData.Error -> when (loadingData.data) {
                StakingTransactionState.CREATE -> createStakeWithPermissionCheck(
                    binding.coinInputLayout.getEditText().text.getDouble()
                )
                StakingTransactionState.CANCEL -> cancelStakeWithPermissionCheck()
                StakingTransactionState.WITHDRAW -> unStakeWithPermissionCheck()
            }
            else -> viewModel.loadData()
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

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun createStake(amount: Double) {
        viewModel.stakeCreate(amount)
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun cancelStake() {
        viewModel.stakeCancel()
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun unStake() {
        viewModel.unstakeCreateTransaction()
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun showLocationError() {
        viewModel.showLocationError()
    }

    override fun FragmentStakingBinding.initViews() {
        setToolbarTitle(R.string.staking_screen_title)
        coinInputLayout.setHint(getString(R.string.text_amount))
    }

    override fun FragmentStakingBinding.initListeners() {
        coinInputLayout.setOnMaxClickListener {
            coinInputLayout.getEditText().setText(viewModel.getMaxValue().toStringCoin())
        }
        createButtonView.setOnClickListener {
            coinInputLayout.setErrorText(null, false)
            if (isValid()) {
                createStakeWithPermissionCheck(coinInputLayout.getEditText().text.getDouble())
            }
        }
        coinInputLayout.getEditText().actionDoneListener {
            hideKeyboard()
        }
        cancelButtonView.setOnClickListener {
            if (isValid()) {
                cancelStakeWithPermissionCheck()
            }
        }
        withdrawButtonView.setOnClickListener {
            if (isValid()) {
                unStakeWithPermissionCheck()
            }
        }
        limitDetailsButton.setOnClickListener {
            navigate(StakingFragmentDirections.toServiceInfoDialog(ServiceType.STAKING))
        }
        coinInputLayout.getEditText()
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
                        coinInputLayout.setMaxButtonVisible(true)
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
                        limitDetailsButton.show()
                        cancelButtonView.hide()
                        withdrawButtonView.hide()
                    }
                    // cancel
                    StakeDetailsStatus.CREATE_PENDING -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxButtonVisible(false)
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
                        limitDetailsButton.hide()
                    }
                    StakeDetailsStatus.CREATED -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxButtonVisible(false)
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
                        limitDetailsButton.hide()
                    }
                    StakeDetailsStatus.CANCEL_PENDING -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxButtonVisible(false)
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
                        limitDetailsButton.hide()
                    }
                    // WITHDRAW
                    StakeDetailsStatus.CANCEL -> {
                        val showWithdrawButton = untilWithdraw == 0
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxButtonVisible(false)
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
                        limitDetailsButton.toggle(showWithdrawButton)
                    }
                    StakeDetailsStatus.WITHDRAW_PENDING -> {
                        invalidateStakingDetails()
                        // header
                        coinInputLayout.getEditText().isEnabled = false
                        coinInputLayout.setMaxButtonVisible(false)
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
                        limitDetailsButton.hide()
                    }
                    else -> {
                        createButtonView.hide()
                        cancelButtonView.hide()
                        withdrawButtonView.hide()
                        limitDetailsButton.hide()
                    }
                }
            }
        })
        viewModel.transactionLiveData.observe(viewLifecycleOwner) { data ->
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
                    if (data.errorType is Failure.LocationError) {
                        showError(data.errorType.message.orEmpty())
                    } else {
                        val stringResource = when (data.data!!) {
                            StakingTransactionState.CREATE -> R.string.staking_screen_fail_create
                            StakingTransactionState.CANCEL -> R.string.staking_screen_fail_cancel
                            StakingTransactionState.WITHDRAW -> R.string.staking_screen_fail_withdraw
                        }
                        Toast.makeText(requireContext(), stringResource, Toast.LENGTH_LONG).show()
                        showContent()
                    }
                }
            }
        }
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
