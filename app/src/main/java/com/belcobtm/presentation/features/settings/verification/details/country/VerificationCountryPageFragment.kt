package com.belcobtm.presentation.features.settings.verification.details.country

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationCountryPageBinding
import com.belcobtm.presentation.core.extensions.getString
import com.belcobtm.presentation.core.extensions.setText
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class VerificationCountryPageFragment : Fragment() {
    lateinit var binding: FragmentVerificationCountryPageBinding
    val viewModel by sharedViewModel<VerificationDetailsViewModel>()
    private var validated = false
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
        with(binding) {
            nextButton.setOnClickListener {
                validated = true
                if (isValidFields()) {
                    viewModel.onCountryVerificationNext()
                }
            }
            countryView.editText?.keyListener = null
            countryView.editText?.setOnClickListener {
                viewModel.item?.let { item ->
                    val countryList = item.supportedCountries
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.verification_alert_country_title)
                        .setItems(countryList.map { it.name }.toTypedArray()) { dialog, which ->
                            countryView.setText(countryList[which].name)
                            viewModel.selectedCountry = countryList[which]
                        }
                        .create().show()
                }
            }
            countryView.editText?.addTextChangedListener {
                if (validated) {
                    validateCountry()
                }
            }
        }
    }

    private fun initObservers() {

    }

    private fun isValidFields(): Boolean {
        val country = binding.validateCountry()
        return country
    }

    private fun FragmentVerificationCountryPageBinding.validateCountry(): Boolean {
        return if (countryView.getString().isEmpty()) {
            countryView.isErrorEnabled = true
            countryView.error = getString(R.string.country_validation_text)
            false
        } else {
            countryView.isErrorEnabled = false
            true
        }
    }


}