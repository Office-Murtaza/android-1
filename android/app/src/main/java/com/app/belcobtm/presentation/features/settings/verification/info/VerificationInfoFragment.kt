package com.app.belcobtm.presentation.features.settings.verification.info

import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_MAIN
import kotlinx.android.synthetic.main.activity_verification_info.*
import org.koin.android.viewmodel.ext.android.viewModel


class VerificationInfoFragment: BaseFragment() {
    val viewModel by viewModel<VerificationInfoViewModel>()
    override val resourceLayout = R.layout.activity_verification_info
    override val isHomeButtonEnabled = true
    private var appliedState: LoadingData<VerificationInfoState>? = null

    override fun initViews() {
        super.initViews()
        setToolbarTitle(R.string.kyc_label)
        viewModel.updateData()
    }

    override fun initListeners() {
        nextButton.setOnClickListener {
            viewModel.onNextClick()
        }
    }

    override fun initObservers() {
        viewModel.actionData.observe(this) { action ->
            when (action) {
                is VerificationInfoAction.NavigateAction -> navigate(action.navDirections)
            }
        }
        viewModel.stateData.observe(this) { state ->
            when (state) {
                is LoadingData.Success -> {
                    state.doIfChanged(appliedState) {
                        showContent()
                    }
                    state.data.isButtonEnabled.doIfChanged((appliedState as? LoadingData.Success<VerificationInfoState>)?.data?.isButtonEnabled) {
                        nextButton.isEnabled = it
                    }
                    state.data.buttonText.doIfChanged((appliedState as? LoadingData.Success<VerificationInfoState>)?.data?.buttonText) {
                        nextButton.setText(it)
                    }
                    state.data.statusTextCode.doIfChanged((appliedState as? LoadingData.Success<VerificationInfoState>)?.data?.statusTextCode) {
                        statusValueView.text = resources.getStringArray(R.array.verification_status_array)[it]
                    }
                    state.data.txLimit.doIfChanged((appliedState as? LoadingData.Success<VerificationInfoState>)?.data?.txLimit) {
                        txLimitValueView.text = it
                    }
                    state.data.dailyLimit.doIfChanged((appliedState as? LoadingData.Success<VerificationInfoState>)?.data?.dailyLimit) {
                        dailyLimitValueView.text = it
                    }
                    state.data.statusColor.doIfChanged((appliedState as? LoadingData.Success<VerificationInfoState>)?.data?.statusColor) {
                        val shape = GradientDrawable()
                        shape.shape = GradientDrawable.RECTANGLE
                        shape.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
                        shape.setStroke(3, ContextCompat.getColor(requireContext(), it.first))
                        shape.setColor(ContextCompat.getColor(requireContext(), it.second))
                        statusValueView.setTextColor(ContextCompat.getColor(requireContext(), it.first))
                        statusValueView.background = shape
                    }
                }
                is LoadingData.Loading -> {
                    showLoading()
                }
                is LoadingData.Error -> {
                    state.doIfChanged(appliedState) {
                        when (state.errorType) {
                            is Failure.MessageError -> showError(state.errorType.message?: getString(R.string.error_something_went_wrong))
                            is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                            else -> showError(R.string.error_something_went_wrong)
                        }
                    }
                }
            }
            appliedState = state
        }
    }

    override fun popBackStack(): Boolean {
        navigate(VerificationInfoFragmentDirections.verificationToSettingsFragment(SETTINGS_MAIN))
        return true
    }
}