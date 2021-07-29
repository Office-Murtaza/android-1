package com.belcobtm.presentation.features.settings.verification.info

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationInfoBinding
import com.belcobtm.presentation.core.extensions.setDrawableEnd
import com.belcobtm.presentation.core.extensions.toHtmlSpan
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class VerificationInfoFragment : BaseFragment<FragmentVerificationInfoBinding>() {
    val viewModel by viewModel<VerificationInfoViewModel>()
    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true
    private var appliedState: LoadingData<VerificationInfoState>? = null

    override fun FragmentVerificationInfoBinding.initViews() {
        appliedState = null
        setToolbarTitle(R.string.kyc_label)
        viewModel.updateData()
    }

    override fun FragmentVerificationInfoBinding.initListeners() {
        nextButton.setOnClickListener {
            viewModel.onNextClick()
        }
    }

    override fun FragmentVerificationInfoBinding.initObservers() {
        viewModel.actionData.observe(viewLifecycleOwner) { action ->
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
                    txLimitValueView.text = it
                }
                state.dailyLimit.doIfChanged(appliedState?.commonData?.dailyLimit) {
                    dailyLimitValueView.text = it
                }
                state.statusIcon.doIfChanged(appliedState?.commonData?.statusIcon) {
                    statusValueView.setDrawableEnd(it)
                }
                state.message.doIfChanged(appliedState?.commonData?.message) {
                    messageView.toggle(it.isNotEmpty())
                    messageViewText.text = it.toHtmlSpan()
                }
                state.bannerIcon.doIfChanged(appliedState?.commonData?.bannerIcon) {
                    bannerIcon.setImageResource(it)
                }
            },
            onUpdate = {
                appliedState = it
            }
        )
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentVerificationInfoBinding =
        FragmentVerificationInfoBinding.inflate(inflater, container, false)
}