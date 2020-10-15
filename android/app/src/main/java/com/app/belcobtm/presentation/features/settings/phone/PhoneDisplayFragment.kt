package com.app.belcobtm.presentation.features.settings.phone

import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_display_phone.*
import org.koin.android.viewmodel.ext.android.viewModel

class PhoneDisplayFragment : BaseFragment() {
    val viewModel by viewModel<PhoneDisplayViewModel>()
    private var appliedState: LoadingData<PhoneDisplayState>? = null
    override val resourceLayout = R.layout.fragment_display_phone
    override val retryListener = View.OnClickListener {
        viewModel.getPhone()
    }

    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true

    override fun initViews() {
        appliedState = null
        setToolbarTitle(R.string.update_phone_label)
    }

    override fun initListeners() {
        nextButton.setOnClickListener {
            navigate(
                PhoneDisplayFragmentDirections.phoneDisplayToPasswordFragment(
                    R.id.password_to_change_phone_fragment,
                    R.string.update_phone_label
                )
            )
        }
    }

    override fun initObservers() {
        viewModel.stateData.listen(
            success = { state ->
                state.doIfChanged(appliedState) {
                    showContent()
                }
                state.phone.doIfChanged((appliedState)?.commonData?.phone) {
                    phoneNumber.text = it
                }
                state.isNextButtonEnabled.doIfChanged((appliedState)?.commonData?.isNextButtonEnabled) {
                    nextButton.isEnabled = it
                }
            },
            onUpdate = {
                appliedState = it
            }
        )
    }
}