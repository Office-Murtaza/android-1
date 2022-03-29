package com.belcobtm.presentation.features.settings.verification.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.belcobtm.R
import com.belcobtm.databinding.FragmentVerificationBinding
import com.belcobtm.domain.settings.type.VerificationStep
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.settings.verification.details.adapter.VerificationStepsAdapter
import com.belcobtm.presentation.features.settings.verification.details.country.VerificationCountryPageFragment
import com.belcobtm.presentation.features.settings.verification.details.document.VerificationDocumentPageFragment
import com.belcobtm.presentation.features.settings.verification.details.identity.VerificationIdentityPageFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class VerificationDetailsFragment : BaseFragment<FragmentVerificationBinding>() {
    val viewModel by sharedViewModel<VerificationDetailsViewModel>()
    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true
    private var appliedState: LoadingData<VerificationDetailsState>? = null

    //for viewpager
    private val countryFragment = VerificationCountryPageFragment()
    private val identityFragment = VerificationIdentityPageFragment()
    private val documentFragment = VerificationDocumentPageFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.currentStep != VerificationStep.COUNTRY_VERIFICATION_STEP) {
                    isEnabled = true
                    viewModel.onBackClick()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun FragmentVerificationBinding.initViews() {
        //appliedState = null
        setToolbarTitle(R.string.settings_verify_dialog_title)
        viewModel.getVerificationStatus()
        verificationViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        verificationViewPager.isUserInputEnabled = false
        verificationViewPager.adapter = createViewPagerAdapter()
    }

    override fun FragmentVerificationBinding.initListeners() {

    }

    override fun FragmentVerificationBinding.initObservers() {
        viewModel.actionData.observe(viewLifecycleOwner) { action ->
            when (action) {
                is VerificationDetailsAction.NavigateAction -> navigate(action.navDirections)
            }
        }
        viewModel.detailsStateData.listen(
            success = { state ->
                state.doIfChanged(appliedState) {
                    showContent()
                }
                state.currentStep.doIfChanged(appliedState?.commonData?.currentStep) {
                    verificationViewPager.post {
                        verificationViewPager.setCurrentItem(it.ordinal, false)
                    }
                }
                state.countryStepBackground.doIfChanged(appliedState?.commonData?.countryStepBackground) {
                    countryStepContainer.background =
                        ContextCompat.getDrawable(requireContext(), it)
                }
                state.countryStepIcon.doIfChanged(appliedState?.commonData?.countryStepIcon) {
                    countryStepImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            it
                        )
                    )
                }
                state.countryStepIconColor.doIfChanged(appliedState?.commonData?.countryStepIconColor) {
                    countryStepImage.setColorFilter(
                        ContextCompat.getColor(requireContext(), it),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                state.countryStepTextColor.doIfChanged(appliedState?.commonData?.countryStepTextColor) {
                    countryStepText.setTextColor(ContextCompat.getColor(requireContext(), it))

                }
                state.identityStepBackground.doIfChanged(appliedState?.commonData?.identityStepBackground) {
                    identityStepContainer.background =
                        ContextCompat.getDrawable(requireContext(), it)
                }
                state.identityStepIcon.doIfChanged(appliedState?.commonData?.identityStepIcon) {
                    identityStepImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            it
                        )
                    )
                }
                state.identityStepIconColor.doIfChanged(appliedState?.commonData?.identityStepIconColor) {
                    identityStepImage.setColorFilter(
                        ContextCompat.getColor(requireContext(), it),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                state.identityStepTextColor.doIfChanged(appliedState?.commonData?.identityStepTextColor) {
                    identityStepText.setTextColor(ContextCompat.getColor(requireContext(), it))

                }
                state.documentStepBackground.doIfChanged(appliedState?.commonData?.documentStepBackground) {
                    documentStepContainer.background =
                        ContextCompat.getDrawable(requireContext(), it)
                }
                state.documentStepIcon.doIfChanged(appliedState?.commonData?.documentStepIcon) {
                    documentStepImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            it
                        )
                    )
                }
                state.documentStepIconColor.doIfChanged(appliedState?.commonData?.documentStepIconColor) {
                    documentStepImage.setColorFilter(
                        ContextCompat.getColor(requireContext(), it),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                state.documentStepTextColor.doIfChanged(appliedState?.commonData?.documentStepTextColor) {
                    documentStepText.setTextColor(ContextCompat.getColor(requireContext(), it))
                }
            },
            error = {
                baseErrorHandler(it)
            },
            onUpdate = {
                appliedState = it
            }
        )
    }

    private fun createViewPagerAdapter(): VerificationStepsAdapter {
        val adapter = VerificationStepsAdapter(
            requireActivity().supportFragmentManager,
            requireActivity().lifecycle
        )
        adapter.addFragment(countryFragment)
        adapter.addFragment(identityFragment)
        adapter.addFragment(documentFragment)
        return adapter
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVerificationBinding =
        FragmentVerificationBinding.inflate(inflater, container, false)
}

