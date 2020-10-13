package com.app.belcobtm.presentation.features.settings.verification.info

import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_MAIN
import kotlinx.android.synthetic.main.fragment_verification_info.*
import org.koin.android.viewmodel.ext.android.viewModel


class VerificationInfoFragment: BaseFragment() {
    val viewModel by viewModel<VerificationInfoViewModel>()
    override val resourceLayout = R.layout.fragment_verification_info
    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true
    private var appliedState: LoadingData<VerificationInfoState>? = null

    override fun initViews() {
        appliedState = null
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
        viewModel.stateData.listen(
            success = { state ->
                state.doIfChanged(appliedState) {
                    showContent()
                }
                state.isButtonEnabled.doIfChanged(appliedState?.commonData?.isButtonEnabled) {
                    nextButton.toggle(it)
                }
                state.buttonText.doIfChanged(appliedState?.commonData?.buttonText) {
                    nextButton.setText(it)
                }
                state.statusTextCode.doIfChanged(appliedState?.commonData?.statusTextCode) {
                    statusValueView.text = resources.getStringArray(R.array.verification_status_array)[it]
                }
                state.txLimit.doIfChanged(appliedState?.commonData?.txLimit) {
                    txLimitValueView.text = getString(R.string.text_usd, it)
                }
                state.dailyLimit.doIfChanged(appliedState?.commonData?.dailyLimit) {
                    dailyLimitValueView.text = getString(R.string.text_usd, it)
                }
                state.statusColor.doIfChanged(appliedState?.commonData?.statusColor) {
                    val shape = GradientDrawable()
                    shape.shape = GradientDrawable.RECTANGLE
                    shape.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
                    shape.setStroke(3, ContextCompat.getColor(requireContext(), it.first))
                    shape.setColor(ContextCompat.getColor(requireContext(), it.second))
                    statusValueView.setTextColor(ContextCompat.getColor(requireContext(), it.first))
                    statusValueView.background = shape
                }
                state.message.doIfChanged(appliedState?.commonData?.message) {
                    messageView.toggle(it.isNotEmpty())
                    messageViewText.text = it
                }
            },
            onUpdate = {
                appliedState = it
            }
        )
    }
}