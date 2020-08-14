package com.app.belcobtm.presentation.features.settings.verification.info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.app.belcobtm.R
import com.app.belcobtm.domain.settings.interactor.GetVerificationInfoUseCase
import com.app.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.app.belcobtm.domain.settings.type.VerificationStatus
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class VerificationInfoViewModel(
    private val getVerificationInfoUseCase: GetVerificationInfoUseCase
) : ViewModel() {
    val stateData = MutableLiveData<LoadingData<VerificationInfoState>>()
    val actionData = SingleLiveData<VerificationInfoAction>()

    private var status: VerificationStatus? = null

    fun updateData() {
        stateData.value = LoadingData.Loading()
        getVerificationInfoUseCase.invoke(Unit,
            onSuccess = {
                status = it.status
                stateData.value = LoadingData.Success(
                    VerificationInfoState(
                        statusColor = getColorByStatus(it.status),
                        buttonText = getButtonTextByStatus(it.status),
                        statusTextCode = it.status.code,
                        txLimit = it.txLimit.toInt().toString(),
                        dailyLimit = it.dayLimit.toInt().toString(),
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

    fun onNextClick() {
        actionData.value = VerificationInfoAction.NavigateAction(
            when (status) {
                VerificationStatus.NOT_VERIFIED,
                VerificationStatus.VERIFICATION_REJECTED -> VerificationInfoFragmentDirections.verificationInfoToVerify()
                VerificationStatus.VERIFIED,
                VerificationStatus.VIP_VERIFICATION_REJECTED -> VerificationInfoFragmentDirections.verificationInfoToVipVerify()
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
    val statusTextCode: Int = 0,
    val txLimit: String = "",
    val dailyLimit: String = "",
    val isButtonEnabled: Boolean = false,
    val message: String = ""
)

sealed class VerificationInfoAction {
    data class NavigateAction(val navDirections: NavDirections) : VerificationInfoAction()
}