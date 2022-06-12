package com.belcobtm.presentation.features.bank_accounts.create

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.databinding.FragmentBankAccountSelectTypeBinding
import com.belcobtm.domain.bank_account.type.CreateBankAccountType
import com.belcobtm.presentation.core.ui.fragment.BaseFragment

class BankAccountSelectTypeFragment : BaseFragment<FragmentBankAccountSelectTypeBinding>() {
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBankAccountSelectTypeBinding =
        FragmentBankAccountSelectTypeBinding.inflate(inflater, container, false)

    override fun FragmentBankAccountSelectTypeBinding.initViews() {
        setToolbarTitle(getString(R.string.bank_accounts_create_screen_title))
        showBackButton(true)
        nonUsIbanTypeItem.setOnClickListener {
            val dest =
                BankAccountSelectTypeFragmentDirections.toBankAccountCreateFragment(CreateBankAccountType.NON_US_IBAN)
            navigate(dest)
        }
        usTypeItem.setOnClickListener {
            val dest =
                BankAccountSelectTypeFragmentDirections.toBankAccountCreateFragment(CreateBankAccountType.US)
            navigate(dest)
        }
        nonUsNonIbanTypeItem.setOnClickListener {
            val dest =
                BankAccountSelectTypeFragmentDirections.toBankAccountCreateFragment(CreateBankAccountType.NON_US_NON_IBAN)
            navigate(dest)
        }
    }

    override fun FragmentBankAccountSelectTypeBinding.initListeners() {

    }

    override fun FragmentBankAccountSelectTypeBinding.initObservers() {

    }
}