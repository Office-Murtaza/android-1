package com.belcobtm.presentation.features.bank_accounts

import BankAccountItemDelegate
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.databinding.FragmentBankAccountsBinding
import com.belcobtm.domain.bank_account.item.BankAccountListItem
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.extensions.show
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.bank_accounts.ach.BankAchFragment
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

}
