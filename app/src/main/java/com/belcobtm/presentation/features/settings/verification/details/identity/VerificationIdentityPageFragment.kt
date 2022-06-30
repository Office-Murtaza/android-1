package com.belcobtm.presentation.features.settings.verification.details.identity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationIdentityPageBinding
import com.belcobtm.domain.settings.item.VerificationIdentityDataItem
import com.belcobtm.domain.settings.type.RecordStatus
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import com.belcobtm.presentation.features.settings.verification.details.VerificationIdentityState
import com.belcobtm.presentation.tools.extensions.clearText
import com.belcobtm.presentation.tools.extensions.getString
import com.belcobtm.presentation.tools.extensions.setText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.Calendar

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
        with(binding)
        {
            firstNameView.showHelpText(getString(R.string.first_name_helper_text))
            lastNameView.showHelpText(getString(R.string.last_name_helper_text))
            birthDateView.showHelpText(getString(R.string.birth_date_helper_text))
            streetNameView.showHelpText(getString(R.string.street_name_helper_text))
            buildingNumberView.showHelpText(getString(R.string.building_number_helper_text))
            zipCodeView.showHelpText(getString(R.string.zip_code_helper_text))
            ssnView.showHelpText(getString(R.string.ssn_helper_text))
            sourceOfFundView.showHelpText(getString(R.string.source_of_funds_helper_text))
            occupationView.showHelpText(getString(R.string.occupation_helper_text))

            if (provinceView.getString().isEmpty())
                cityView.isEnabled = false
        }
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
                            ssn = ssnView.getString(),
                            sourceOfFunds = sourceOfFundView.getString(),
                            occupation = occupationView.getString(),
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
                val minimumYear = selectedYearOfBirth ?: c.get(Calendar.YEAR) - 18
                val month = selectedMonthOfBirth?.minus(1) ?: c.get(Calendar.MONTH)
                val day = selectedDayOfBirth ?: c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    requireActivity(),
                    R.style.MySpinnerDatePickerStyle,
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

            provinceView.editText?.keyListener = null
            provinceView.editText?.setOnClickListener {
                viewModel.countries
                    .find { it.name == viewModel.selectedCountry?.name }
                    ?.states?.let { stateList ->
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.verification_alert_state_title)
                            .setItems(stateList.map { it.name }.toTypedArray()) { _, which ->
                                provinceView.setText(stateList[which].name)
                                cityView.clearText()
                                cityView.isEnabled = true
                                validateProvince()
                            }
                            .create()
                            .show()
                    }
            }

            cityView.editText?.keyListener = null
            cityView.editText?.setOnClickListener {
                viewModel.countries
                    .find { it.name == viewModel.selectedCountry?.name }
                    ?.states
                    ?.find { it.name == provinceView.getString() }
                    ?.cities?.let { cities ->
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.verification_alert_city_title)
                            .setItems(cities.toTypedArray()) { _, which ->
                                cityView.setText(cities[which])
                                validateCity()
                            }
                            .create()
                            .show()
                    } ?: validateProvince()
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

            sourceOfFundView.editText?.keyListener = null
            sourceOfFundView.editText?.setOnClickListener {
                val listSourceOfFunds =
                    listOf("Loan", "Pension", "Savings", "Inheritance", "Property Sale")
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.verification_alert_source_of_funds_title)
                    .setItems(listSourceOfFunds.toTypedArray()) { _, which ->
                        sourceOfFundView.setText(listSourceOfFunds[which])
                        validateSourceOfFunds()
                    }
                    .create()
                    .show()
            }

            occupationView.editText?.addTextChangedListener {
                if (validated) {
                    validateOccupation()
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
                        populateUiWithIdentityStateData(loadingData.data)

                        firstInvalidView?.let {
                            binding.identityFieldsContainer.post {
                                binding.identityFieldsContainer.smoothScrollTo(0, it.top)
                            }
                        }
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

    private fun TextInputLayout.showErrorText(message: String) {
        isErrorEnabled = true
        isHelperTextEnabled = false
        error = message
        firstInvalidView = this

    }

    private fun TextInputLayout.showHelpText(message: String) {
        isHelperTextEnabled = true
        isErrorEnabled = false
        helperText = message
    }

    private fun populateUiWithIdentityStateData(identityState: VerificationIdentityState) {
        with(binding) {
            firstNameView.setText(identityState.firstNameValue)
            lastNameView.setText(identityState.lastNameValue)
            selectedDayOfBirth = identityState.dayOfBirthValue
            selectedMonthOfBirth = identityState.monthOfBirthValue
            selectedYearOfBirth = identityState.yearOfBirthValue
            birthDateView.setText("$selectedDayOfBirth/$selectedMonthOfBirth/$selectedYearOfBirth")
            provinceView.setText(identityState.provinceValue)
            cityView.setText(identityState.cityValue)
            streetNameView.setText(identityState.streetNameValue)
            buildingNumberView.setText(identityState.buildingNumberValue)
            zipCodeView.setText(identityState.zipCodeValue)
            ssnView.setText(identityState.ssnValue)
            occupationView.setText(identityState.occupation)
            sourceOfFundView.setText(identityState.sourceOfFunds)
            if (identityState.ssnValidationError) {
                ssnView.showErrorText(getString(R.string.ssn_invalid_error_text))
            }
            if (identityState.zipCodeValidationError) {
                zipCodeView.showErrorText(getString(R.string.zip_invalid_error_text))
            }
            if (identityState.buildingNumberValidationError) {
                buildingNumberView.showErrorText(getString(R.string.building_number_invalid_error_text))
            }
            if (identityState.streetNameValidationError) {
                streetNameView.showErrorText(getString(R.string.street_name_invalid_error_text))
            }
            if (identityState.cityValidationError) {
                cityView.showErrorText(getString(R.string.city_invalid_error_text))
            }
            if (identityState.provinceValidationError) {
                provinceView.showErrorText(getString(R.string.province_invalid_error_text))
            }
            if (identityState.birthDateValidationError) {
                birthDateView.showErrorText(getString(R.string.birth_date_invalid_error_text))
            }
            if (identityState.lastNameValidationError) {
                lastNameView.showErrorText(getString(R.string.last_name_invalid_error_text))
            }
            if (identityState.firstNameValidationError) {
                firstNameView.showErrorText(getString(R.string.first_name_invalid_error_text))
            }
            cityView.isEnabled = provinceView.getString().isNotEmpty()

            validated = true

        }
    }

    private fun isValidFields(): Boolean {
        //order this as opposed to the order they have on the scrollview ( for the firstInvalidView to work)
        val occupation = binding.validateOccupation()
        val sourceOfFunds = binding.validateSourceOfFunds()
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
            && sourceOfFunds
            && occupation
    }

    private fun FragmentVerificationIdentityPageBinding.validateFirstName(): Boolean {
        return if (firstNameView.getString().isEmpty()) {
            firstNameView.showErrorText(getString(R.string.first_name_validation_text))
            false
        } else {
            firstNameView.showHelpText(getString(R.string.first_name_helper_text))
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateLastName(): Boolean {
        return if (lastNameView.getString().isEmpty()) {
            lastNameView.showErrorText(getString(R.string.last_name_validation_text))
            false
        } else {
            lastNameView.showHelpText(getString(R.string.last_name_helper_text))
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateBirthDate(): Boolean {
        return if (birthDateView.getString().isEmpty()) {
            birthDateView.showErrorText(getString(R.string.birth_date_validation_text))
            false
        } else {
            birthDateView.showHelpText(getString(R.string.birth_date_helper_text))
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateProvince(): Boolean {
        return if (provinceView.getString().isEmpty()) {
            provinceView.isErrorEnabled = true
            provinceView.showErrorText(getString(R.string.province_validation_text))
            false
        } else {
            provinceView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateCity(): Boolean {
        return if (cityView.getString().isEmpty()) {
            cityView.isErrorEnabled = true
            cityView.showErrorText(getString(R.string.city_validation_text))
            false
        } else {
            cityView.isErrorEnabled = false
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateStreetName(): Boolean {
        return if (streetNameView.getString().isEmpty()) {
            streetNameView.showErrorText(getString(R.string.street_name_validation_text))
            false
        } else {
            streetNameView.showHelpText(getString(R.string.street_name_helper_text))
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateBuildingNumber(): Boolean {
        return if (buildingNumberView.getString().isEmpty()) {
            buildingNumberView.showErrorText(getString(R.string.building_number_validation_text))
            false
        } else {
            buildingNumberView.showHelpText(getString(R.string.building_number_helper_text))
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateZipCode(): Boolean {
        return if (zipCodeView.getString().isEmpty()) {
            zipCodeView.showErrorText(getString(R.string.zip_validation_text))
            false
        } else {
            zipCodeView.showHelpText(getString(R.string.zip_code_helper_text))
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateSsn(): Boolean {
        return if (ssnView.getString().isEmpty()) {
            ssnView.showErrorText(getString(R.string.ssn_validation_text))
            false
        } else {
            ssnView.showHelpText(getString(R.string.ssn_helper_text))
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateSourceOfFunds(): Boolean {
        return if (sourceOfFundView.getString().isEmpty()) {
            sourceOfFundView.showErrorText(getString(R.string.source_of_funds_validation_text))
            false
        } else {
            sourceOfFundView.showHelpText(getString(R.string.source_of_funds_helper_text))
            true
        }
    }

    private fun FragmentVerificationIdentityPageBinding.validateOccupation(): Boolean {
        return if (occupationView.getString().isEmpty()) {
            occupationView.showErrorText(getString(R.string.occupation_validation_text))
            false
        } else {
            occupationView.showHelpText(getString(R.string.occupation_helper_text))
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
