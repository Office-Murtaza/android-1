package com.app.belcobtm.presentation.features.settings.security

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.layout_settings_security.*
import org.koin.android.viewmodel.ext.android.viewModel

class SecurityFragment : BaseFragment() {

    private val viewModel by viewModel<SecurityViewModel>()

    override val resourceLayout: Int = R.layout.layout_settings_security
    override var isBackButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.security_label)
        setClickListeners()
        observeData()
    }

    private fun observeData() {
        viewModel.actionData.observe(viewLifecycleOwner, Observer { action ->
            if (action is SecurityAction.NavigateAction) {
                navigate(action.navDirections)
            }
        })
    }

    private fun setClickListeners() {
        updatePhoneItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.PHONE) }
        updatePassItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.PASS) }
        updatePinItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.PIN) }
        seedPhraseItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.SEED) }
        unlinkItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.UNLINK) }
    }
}
