package com.app.belcobtm.presentation.features.settings.about

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.layout_settings_about.*
import org.koin.android.viewmodel.ext.android.viewModel

class AboutFragment : BaseFragment() {

    private val viewModel by viewModel<AboutViewModel>()

    override val resourceLayout: Int = R.layout.layout_settings_about
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.about_label)
        setClickListeners()
        observeData()
    }

    private fun observeData() {
        viewModel.appVersion.observe(viewLifecycleOwner, Observer { version ->
            versionItem.setValue(version)
        })
    }

    private fun setClickListeners() {
        termsItem.setOnClickListener { viewModel.handleItemClick(AboutItem.TERMS) }
        supportItem.setOnClickListener { viewModel.handleItemClick(AboutItem.SUPPORT) }
    }
}
