package com.belcobtm.presentation.screens.bank_accounts

import BankAccountItemDelegate
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.belcobtm.R
import com.belcobtm.databinding.FragmentBankAccountsBinding
import com.belcobtm.domain.bank_account.item.BankAccountListItem
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.show
import org.koin.androidx.viewmodel.ext.android.viewModel

class BankAccountsFragment : BaseFragment<FragmentBankAccountsBinding>() {

    private val viewModel by viewModel<BankAccountsViewModel>()

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(BankAccountItemDelegate(::onBankAccountClicked))
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBankAccountsBinding =
        FragmentBankAccountsBinding.inflate(inflater, container, false)

    override fun FragmentBankAccountsBinding.initViews() {
        setToolbarTitle(R.string.bank_accounts_screen_title)
        bankAccountsRecycleView.adapter = adapter
    }

    override fun FragmentBankAccountsBinding.initListeners() {
        verifyButton.setOnClickListener {
            val dest = BankAccountsFragmentDirections.toVerificationDetailsFragment()
            navigate(dest)
        }
        createBankAccountButton.setOnClickListener {
            val dest = BankAccountsFragmentDirections.toBankAccountSelectTypeFragment()
            navigate(dest)
        }
        linkBankAccountButton.setOnClickListener {
            onLinkBankAccountClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkVerification()
    }

    override fun FragmentBankAccountsBinding.initObservers() {
        viewModel.bankAccountsLiveData.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<List<BankAccountListItem>> -> showLoading()
                is LoadingData.Success<List<BankAccountListItem>> -> {
                    hideLoading()
                    if (loadingData.data.isNotEmpty()) {
                        bankAccountsRecycleView.show()
                        noBankAccountsAddedError.hide()
                        adapter.update(loadingData.data)
                    } else {
                        bankAccountsRecycleView.hide()
                        noBankAccountsAddedError.show()
                    }
                }
                is LoadingData.Error<List<BankAccountListItem>> -> {
                    hideLoading()
                    Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_LONG).show()
                }
                else -> {
                }
            }

        }
        viewModel.isVerifiedLiveData.observe(viewLifecycleOwner) {
            toggleVerificationViews(it)
        }
    }

    private fun onBankAccountClicked(item: BankAccountListItem) {
        val dest = BankAccountsFragmentDirections.toBankAccountDetailsFragment(item.id)
        navigate(dest)

    }

    private fun onLinkBankAccountClicked() {
        navigate(BankAccountsFragmentDirections.toAchFragment())
    }

    override fun showLoading() {
        binding.progressView.show()
        binding.bankAccountsRecycleView.hide()
        binding.noBankAccountsAddedError.hide()
    }

    private fun hideLoading() {
        binding.progressView.hide()
    }

    private fun toggleVerificationViews(isVerified: Boolean) {
        with(binding) {
            createBankAccountButton.isClickable = isVerified
            linkBankAccountButton.isClickable = isVerified
            verificationContainer.isVisible = isVerified.not()
        }
    }

}
