package com.belcobtm.presentation.features.settings.verification.details.country

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationCountryPageBinding
import com.belcobtm.presentation.core.extensions.setText
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class VerificationCountryPageFragment : Fragment() {
    lateinit var binding: FragmentVerificationCountryPageBinding
    val viewModel by sharedViewModel<VerificationDetailsViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerificationCountryPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {

    }

    private fun initListeners() {
        binding.countryView.editText?.keyListener = null
        binding.countryView.editText?.setOnClickListener {
            viewModel.item?.let { item ->
                val countryList = item.supportedCountries
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.verification_alert_country_title)
                    .setItems(countryList.map { it.name }.toTypedArray()) { dialog, which ->
                        binding.countryView.setText(countryList[which].name)
                        viewModel.selectedCountry = countryList[which]
                    }
                    .create().show()
            }
        }
    }

    private fun initObservers() {

    }


}