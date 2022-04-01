package com.belcobtm.presentation.features.settings.verification.details.identity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationIdentityPageBinding
import com.belcobtm.domain.settings.item.VerificationIdentityDataItem
import com.belcobtm.domain.settings.type.RecordStatus
import com.belcobtm.presentation.core.extensions.getString
import com.belcobtm.presentation.core.extensions.setText
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import com.belcobtm.presentation.features.settings.verification.details.VerificationIdentityState
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class VerificationIdentityPageFragment : Fragment() {
    val viewModel by sharedViewModel<VerificationDetailsViewModel>()
    lateinit var binding: FragmentVerificationIdentityPageBinding
    private var selectedDayOfBirth: Int? = null
    private var selectedMonthOfBirth: Int? = null
    private var selectedYearOfBirth: Int? = null
    private var validated = false
    private var firstInvalidView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerificationIdentityPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        // viewModel.getVerificationFields()
    }

    private fun initListeners() {
        with(binding) {
            nextButton.setOnClickListener {
                validated = true
                if (isValidFields()) {
                    viewModel.onIdentityVerificationNext(
                        VerificationIdentityDataItem(
                            countryCode = viewModel.selectedCountry!!.code,
                            firstName = firstNameView.getString(),
                            lastName = lastNameView.getString(),
                            dayOfBirth = selectedDayOfBirth ?: 0,
                            monthOfBirth = selectedMonthOfBirth ?: 0,
                            yearOfBirth = selectedYearOfBirth ?: 0,
                            province = provinceView.getString(),
                            city = cityView.getString(),
                            streetName = streetNameView.getString(),
                            buildingNumber = buildingNumberView.getString(),
                            zipCode = zipCodeView.getString(),
                            ssn = ssnView.getString()
                        )
                    )
                } else {
                    firstInvalidView?.let {
                        identityFieldsContainer.smoothScrollTo(0, it.top)
                    }
                }
            }

            birthDateView.editText?.keyListener = null
            birthDateView.editText?.setOnClickListener {
                val c = Calendar.getInstance()
                val minimumYear = c.get(Calendar.YEAR) - 18
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    requireActivity(),
                    { view, year, monthOfYear, dayOfMonth ->
                        selectedDayOfBirth = dayOfMonth
                        selectedMonthOfBirth = monthOfYear + 1
                        selectedYearOfBirth = year
                        birthDateView.setText("$selectedDayOfBirth/$selectedMonthOfBirth/$selectedYearOfBirth")
                    },
                    minimumYear,
                    month,
                    day
                )
                dpd.show()
            }

            firstNameView.editText?.addTextChangedListener {
                if (validated) {
                    validateFirstName()
                }
            }
            lastNameView.editText?.addTextChangedListener {
                if (validated) {
                    validateLastName()
                }
            }
            birthDateView.editText?.addTextChangedListener {
                if (validated) {
                    validateBirthDate()
                }
            }
            provinceView.editText?.addTextChangedListener {
                if (validated) {
                    validateProvince()
                }
            }
            cityView.editText?.addTextChangedListener {
                if (validated) {
                    validateCity()
                }
            }
            streetNameView.editText?.addTextChangedListener {
                if (validated) {
                    validateStreetName()
                }
            }
            buildingNumberView.editText?.addTextChangedListener {
                if (validated) {
                    validateBuildingNumber()
                }
            }
            zipCodeView.editText?.addTextChangedListener {
                if (validated) {
                    validateZipCode()
                }
            }
            ssnView.editText?.addTextChangedListener {
                if (validated) {
                    validateSsn()
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.identityStateData.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<VerificationIdentityState> -> showLoading()
                is LoadingData.Success<VerificationIdentityState> -> {
                    hideLoading()
                    if (loadingData.data.recordStatus == RecordStatus.NO_MATCH) {
                       // populateUiWithIdentityStateData(loadingData.data)
                    }
                }
                is LoadingData.Error<VerificationIdentityState> -> {
                    hideLoading()
                }
                else -> {
                }
            }

        }
    }

    private fun populateUiWithIdentityStateData(identityState: VerificationIdentityState) {
        with(binding) {
            lastNameView.isErrorEnabled = true
            lastNameView.error = getString(R.string.last_name_validation_text)
        }

    }

    private fun isValidFields(): Boolean {
        //order this as opposed to the order they have on the scrollview ( for the firstInvalidView to work)
        val ssn = binding.validateSsn()
        val zipCode = binding.validateZipCode()
        val buildingNumber = binding.validateBuildingNumber()
        val streetName = binding.validateStreetName()
        val city = binding.validateCity()
        val province = binding.validateProvince()
        val birthDate = binding.validateBirthDate()
        val lastName = binding.validateLastName()
        val firstName = binding.validateFirstName()

        return firstName
                && lastName
                && birthDate
                && province
                && city
                && streetName
                && buildingNumber
                && zipCode
                && ssn
    }

    private fun FragmentVerificationIdentityPageBinding.validateFirstName(): Boolean {
        return if (firstNameView.getString().isEmpty()) {
            firstNameView.isErrorEnabled = true
            firstNameView.error = getString(R.string.first_name_validation_text)
            firstInvalidView = firstNameView
            false
        } else {
            firstNameView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateLastName(): Boolean {
        return if (lastNameView.getString().isEmpty()) {
            lastNameView.isErrorEnabled = true
            lastNameView.error = getString(R.string.last_name_validation_text)
            firstInvalidView = lastNameView
            false
        } else {
            lastNameView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateBirthDate(): Boolean {
        return if (birthDateView.getString().isEmpty()) {
            birthDateView.isErrorEnabled = true
            birthDateView.error = getString(R.string.birth_date_validation_text)
            firstInvalidView = birthDateView
            false
        } else {
            birthDateView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateProvince(): Boolean {
        return if (provinceView.getString().isEmpty()) {
            provinceView.isErrorEnabled = true
            provinceView.error = getString(R.string.province_validation_text)
            firstInvalidView = provinceView
            false
        } else {
            provinceView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateCity(): Boolean {
        return if (cityView.getString().isEmpty()) {
            cityView.isErrorEnabled = true
            cityView.error = getString(R.string.city_validation_text)
            firstInvalidView = cityView
            false
        } else {
            cityView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateStreetName(): Boolean {
        return if (streetNameView.getString().isEmpty()) {
            streetNameView.isErrorEnabled = true
            streetNameView.error = getString(R.string.street_name_validation_text)
            firstInvalidView = streetNameView
            false
        } else {
            streetNameView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateBuildingNumber(): Boolean {
        return if (buildingNumberView.getString().isEmpty()) {
            buildingNumberView.isErrorEnabled = true
            buildingNumberView.error = getString(R.string.building_number_validation_text)
            firstInvalidView = buildingNumberView
            false
        } else {
            buildingNumberView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateZipCode(): Boolean {
        return if (zipCodeView.getString().isEmpty()) {
            zipCodeView.isErrorEnabled = true
            zipCodeView.error = getString(R.string.zip_validation_text)
            firstInvalidView = zipCodeView
            false
        } else {
            zipCodeView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateSsn(): Boolean {
        return if (ssnView.getString().isEmpty()) {
            ssnView.isErrorEnabled = true
            ssnView.error = getString(R.string.ssn_validation_text)
            firstInvalidView = ssnView
            false
        } else {
            ssnView.isErrorEnabled = false
            true
        }
    }

    private fun showLoading() {
        binding.progressView.visibility = View.VISIBLE
        binding.identityFieldsContainer.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressView.visibility = View.GONE
        binding.identityFieldsContainer.visibility = View.VISIBLE
    }
}