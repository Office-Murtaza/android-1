package com.belcobtm.presentation.screens.bank_accounts.ach

import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.databinding.FragmentBankAchBinding
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.bank_accounts.ach.BankAchViewModel.Companion.USDC_TERMS_LINK
import com.plaid.link.OpenPlaidLink
import com.plaid.link.linkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess
import org.koin.androidx.viewmodel.ext.android.viewModel

class BankAchFragment : BaseFragment<FragmentBankAchBinding>() {

    private val viewModel by viewModel<BankAchViewModel>()

    override val isBackButtonEnabled: Boolean = true

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
    ): FragmentBankAchBinding =
        FragmentBankAchBinding.inflate(inflater, container, false)

    override fun FragmentBankAchBinding.initViews() {
        setToolbarTitle(R.string.bank_ach_title)
        setUsdcTermsTextView()
    }

    private fun setUsdcTermsTextView() {
        val mainText = getString(R.string.bank_ach_usdc_terms_text)
        val clickableText = getString(R.string.bank_usdc_terms_clickable)

        binding.termsTextView.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = SpannableStringBuilder()
                .append(mainText)
                .append(clickableText).apply {
                    setSpan(
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                openUsdcTermsLink()
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                ds.isUnderlineText = false
                                ds.color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                            }
                        },
                        mainText.length,
                        mainText.length + clickableText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
        }
    }

    private fun openUsdcTermsLink() {
        runCatching {
            activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(USDC_TERMS_LINK)).apply {
                putExtra(Browser.EXTRA_APPLICATION_ID, requireActivity().packageName)
            })
        }
    }

    override fun FragmentBankAchBinding.initListeners() {
        confirmButton.setOnClickListener {
            onLinkBankAccountClicked()
        }
    }

    private fun onLinkBankAccountClicked() {
        viewModel.getLinkToken()
    }

    override fun FragmentBankAchBinding.initObservers() {
        with(viewModel) {
            linkToken.observe(viewLifecycleOwner) { loadingData ->
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
            consumerName.observe(viewLifecycleOwner) {
                setMainTextView(it)
            }
        }
    }

    private fun initPlaidSdk(linkToken: String) {
        val linkTokenConfiguration = linkTokenConfiguration {
            token = linkToken
        }
        linkAccountToPlaid.launch(linkTokenConfiguration)
    }

    private fun setMainTextView(consumerName: String) {
        binding.mainTextView.text = getString(R.string.bank_ach_main_text, consumerName)
    }

}
