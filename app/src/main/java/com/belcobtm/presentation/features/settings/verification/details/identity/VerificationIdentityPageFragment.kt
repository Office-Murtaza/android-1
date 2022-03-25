package com.belcobtm.presentation.features.settings.verification.details.identity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.belcobtm.databinding.FragmentVerificationIdentityPageBinding
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import com.belcobtm.presentation.features.settings.verification.details.VerificationFieldsState
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class VerificationIdentityPageFragment : Fragment() {
    val viewModel by sharedViewModel<VerificationDetailsViewModel>()
    lateinit var binding: FragmentVerificationIdentityPageBinding
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
    }

    private fun initViews() {
        viewModel.getVerificationFields()
    }

    private fun initListeners() {

    }

    private fun initObservers() {
        viewModel.fieldsStateData.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<VerificationFieldsState> -> showLoading()
                is LoadingData.Success<VerificationFieldsState> -> {
                    val x = loadingData.data
                }
                is LoadingData.Error<VerificationFieldsState> -> {

                }
            }

        }
    }

    private fun showLoading() {

    }
}