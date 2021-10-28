package com.belcobtm.presentation.features.settings.verification.info

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.R
import com.belcobtm.domain.settings.interactor.GetVerificationInfoUseCase
import com.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.belcobtm.domain.settings.type.VerificationStatus
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.mvvm.LoadingData

class VerificationInfoViewModel(
    private val getVerificationInfoUseCase: GetVerificationInfoUseCase,
    private val priceFormatter: Formatter<Double>
) : ViewModel() {
    val stateData = MutableLiveData<LoadingData<VerificationInfoState>>()
    val actionData = SingleLiveData<VerificationInfoAction>()

    private var item: VerificationInfoDataItem? = null

    fun updateData() {
        stateData.value = LoadingData.Loading()
        getVerificationInfoUseCase.invoke(Unit,
            onSuccess = {
                item = it
                stateData.value = LoadingData.Success(
                    VerificationInfoState(
                        statusColor = getColorByStatus(it.status),
                        statusIcon = getStatusIcon(it.status),
                        buttonText = getButtonTextByStatus(it.status),
                        bannerIcon = getBannerIcon(it.status),
                        statusTextCode = it.status.code,
                        txLimit = priceFormatter.format(it.txLimit),
                        dailyLimit = priceFormatter.format(it.dayLimit),
                        isButtonEnabled = isButtonEnabled(it),
                        message = it.message
                    )
                )
            },
            onError = {
                stateData.value = LoadingData.Error(it)
            }
        )
    }

    private fun getBannerIcon(status: VerificationStatus): Int =
        when (status) {
            VerificationStatus.VERIFICATION_PENDING,
            VerificationStatus.VIP_VERIFICATION_PENDING -> R.drawable.ic_time_outlined
            else -> R.drawable.ic_warning_outlined
        }

    private fun getStatusIcon(status: VerificationStatus): Int =
        when (status) {
            VerificationStatus.VERIFICATION_PENDING,
            VerificationStatus.VIP_VERIFICATION_PENDING -> R.drawable.ic_verification_status_pending
            VerificationStatus.VIP_VERIFICATION_REJECTED,
            VerificationStatus.VERIFICATION_REJECTED -> R.drawable.ic_verification_status_rejected
            VerificationStatus.VIP_VERIFIED -> R.drawable.ic_verification_status_vip_verified
            VerificationStatus.VERIFIED -> R.drawable.ic_verification_status_verified
            else -> R.drawable.ic_verification_status_not_verified
        }

    fun onNextClick() {
        actionData.value = VerificationInfoAction.NavigateAction(
            when (item?.status) {
                VerificationStatus.NOT_VERIFIED,
                VerificationStatus.VERIFICATION_REJECTED ->
                    VerificationInfoFragmentDirections.verificationInfoToVerify(item ?: return)
                VerificationStatus.VERIFIED,
                VerificationStatus.VIP_VERIFICATION_REJECTED ->
                    VerificationInfoFragmentDirections.verificationInfoToVipVerify(item ?: return)
                else -> throw IllegalStateException("Not available for verification for this state")
            }
        )
    }

    private fun isButtonEnabled(verificationInfoDataItem: VerificationInfoDataItem): Boolean {
        return when (verificationInfoDataItem.status) {
            VerificationStatus.NOT_VERIFIED,
            VerificationStatus.VERIFICATION_REJECTED,
            VerificationStatus.VERIFIED,
            VerificationStatus.VIP_VERIFICATION_REJECTED -> true
            else -> false
        }
    }

    private fun getColorByStatus(status: VerificationStatus): Pair<Int, Int> {
        return when (status) {
            VerificationStatus.VERIFICATION_PENDING,
            VerificationStatus.VIP_VERIFICATION_PENDING -> Pair(
                R.color.pending_border,
                R.color.pending_border
            )
            VerificationStatus.VIP_VERIFICATION_REJECTED,
            VerificationStatus.VERIFICATION_REJECTED -> Pair(
                R.color.rejected_border,
                R.color.rejected_background
            )
            VerificationStatus.VIP_VERIFIED -> Pair(
                R.color.vip_verified_border,
                R.color.vip_verified_background
            )
            VerificationStatus.VERIFIED -> Pair(
                R.color.verified_border,
                R.color.verified_background
            )
            else -> Pair(R.color.not_verified_border, R.color.not_verified_background)
        }
    }

    private fun getButtonTextByStatus(status: VerificationStatus): Int {
        return when (status) {
            VerificationStatus.VERIFIED,
            VerificationStatus.VIP_VERIFICATION_REJECTED -> {
                R.string.verification_vip_verify
            }
            else -> R.string.verification_verify
        }
    }
}

data class VerificationInfoState(
    val statusColor: Pair<Int, Int> = Pair(R.color.transaction_red, R.color.transaction_red),
    val buttonText: Int = R.string.verification_verify,
    @StringRes val statusTextCode: Int = 0,
    @DrawableRes val statusIcon: Int = 0,
    @DrawableRes val bannerIcon: Int = R.drawable.ic_warning_outlined,
    val txLimit: String = "",
    val dailyLimit: String = "",
    val isButtonEnabled: Boolean = false,
    val message: String = ""
)

sealed class VerificationInfoAction {
    data class NavigateAction(val navDirections: NavDirections) : VerificationInfoAction()
}