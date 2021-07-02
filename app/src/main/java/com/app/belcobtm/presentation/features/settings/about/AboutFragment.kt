package com.app.belcobtm.presentation.features.settings.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentAboutBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel

class AboutFragment : BaseFragment<FragmentAboutBinding>() {

    private val viewModel by viewModel<AboutViewModel>()


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
            binding.versionItem.setValue(version)
        })
    }

    private fun setClickListeners() {
        binding.termsItem.setOnClickListener { viewModel.handleItemClick(AboutItem.TERMS) }
        binding.privacyItem.setOnClickListener { viewModel.handleItemClick(AboutItem.PRIVACY) }
        binding.complaintItem.setOnClickListener { viewModel.handleItemClick(AboutItem.COMPLAINT) }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAboutBinding =
        FragmentAboutBinding.inflate(inflater, container, false)
}
