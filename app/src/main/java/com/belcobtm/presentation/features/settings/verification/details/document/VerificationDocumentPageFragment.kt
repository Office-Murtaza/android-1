package com.belcobtm.presentation.features.settings.verification.details.document

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.belcobtm.databinding.FragmentVerificationDocumentPageBinding
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VerificationDocumentPageFragment : Fragment() {
    val viewModel by sharedViewModel<VerificationDetailsViewModel>()
    lateinit var binding: FragmentVerificationDocumentPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerificationDocumentPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews() {

    }

    private fun initListeners() {

    }
}