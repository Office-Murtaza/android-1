package com.belcobtm.presentation.features.bank_accounts

import BankAccountItemDelegate
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.belcobtm.R
import com.belcobtm.databinding.FragmentBankAccountsBinding
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem
import com.belcobtm.domain.bank_account.item.BankAccountListItem
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.extensions.show
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.plaid.link.OpenPlaidLink
import com.plaid.link.linkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess
import org.koin.androidx.viewmodel.ext.android.viewModel

class BankAccountsFragment : BaseFragment<FragmentBankAccountsBinding>() {

    private val viewModel by viewModel<BankAccountsViewModel>()
    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(BankAccountItemDelegate(::onBankAccountClicked))
        }
    }

    private val linkAccountToPlaid =
        registerForActivityResult(OpenPlaidLink()) {
            when (it) {
                is LinkSuccess -> {
                    viewModel.linkBankAccounts(
                        BankAccountLinkDataItem(
                            bankName = it.metadata.institution?.name ?: "",
                            accountsId = it.metadata.accounts.map { linkAccount -> linkAccount.id },
                            publicToken = it.publicToken
                        )
                    )
                }/* handle LinkSuccess */
                is LinkExit -> {
                    Log.i("Plaid", "User has canceled Plaid")
                }/* handle LinkExit */
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
                        //   bankAccountsRecycleView.post { bankAccountsRecycleView.scrollToPosition(0) }
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

        viewModel.observeBankAccountsLiveData.listen(success = {}, error = {})

        viewModel.linkToken.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<String> -> {
                }
                is LoadingData.Success<String> -> {
                    initPlaidSdk(loadingData.data)
                }
                is LoadingData.Error<String> -> {
                    Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_LONG).show()
                }
                else -> {
                }
            }

        }

    }

    private fun onBankAccountClicked(item: BankAccountListItem) {
        val dest = BankAccountsFragmentDirections.toBankAccountDetailsFragment(item.id)
        navigate(dest)

    }

    private fun onLinkBankAccountClicked() {
        viewModel.getLinkToken()
    }

    private fun initPlaidSdk(linkToken: String) {
        val linkTokenConfiguration = linkTokenConfiguration {
            token = linkToken
        }
        linkAccountToPlaid.launch(linkTokenConfiguration)
    }

    override fun showLoading() {
        binding.progressView.show()
        binding.bankAccountsRecycleView.hide()
        binding.noBankAccountsAddedError.hide()
    }

    private fun hideLoading() {
        binding.progressView.hide()

    }

}