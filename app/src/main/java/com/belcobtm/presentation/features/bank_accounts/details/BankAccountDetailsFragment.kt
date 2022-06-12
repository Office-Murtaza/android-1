package com.belcobtm.presentation.features.bank_accounts.details

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.belcobtm.databinding.FragmentBankAccountDetailsBinding
import com.belcobtm.domain.bank_account.item.*
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.bank_accounts.details.delegate.BankAccountDetailsItemDelegate
import com.belcobtm.presentation.features.bank_accounts.details.delegate.BankAccountNoPaymentItemDelegate
import com.belcobtm.presentation.features.bank_accounts.details.delegate.BankAccountPaymentItemDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class BankAccountDetailsFragment : BaseFragment<FragmentBankAccountDetailsBinding>() {

    private val args by navArgs<BankAccountDetailsFragmentArgs>()
    private val viewModel: BankAccountDetailsViewModel by viewModel {
        val args = BankAccountDetailsFragmentArgs.fromBundle(requireArguments())
        parametersOf(args.bankAccountId)
    }
    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(
                BankAccountDetailsItemDelegate(
                    ::onBankAccountDetailsClicked,
                    ::onBuyClicked,
                    ::onSellClicked
                )
            )
            registerDelegate(BankAccountPaymentItemDelegate(::onBankAccountPaymentClicked))
            registerDelegate(BankAccountNoPaymentItemDelegate())
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBankAccountDetailsBinding =
        FragmentBankAccountDetailsBinding.inflate(inflater, container, false)

    override fun FragmentBankAccountDetailsBinding.initViews() {
        showBackButton(true)
        if (!viewModel.isInitialized)
            viewModel.getBankAccountPayments(args.bankAccountId)

        bankAccountDetailsRecycleView.adapter = adapter

    }

    override fun FragmentBankAccountDetailsBinding.initObservers() {
        viewModel.observeBankAccountDetailsLiveData.listen(success = { bankAccount ->
            bindBankAccountDetails(bankAccount)
        }, error = {})

        viewModel.observePaymentsLiveData.listen(success = {}, error = {})

        viewModel.bankAccountPaymentsLiveData.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<List<BankAccountPaymentListItem>> -> showLoading()
                is LoadingData.Success<List<BankAccountPaymentListItem>> -> {
                    val listOfItems = mutableListOf<ListItem>()
                    viewModel.bankAccountDataItem?.let { bankAccount ->
                        listOfItems.add(
                            bankAccount.toDetailsListItem(viewModel.isExpanded)
                        )
                    }
                    listOfItems.addAll(loadingData.data.reversed())
                    if (loadingData.data.isEmpty())
                        listOfItems.add(BankAccountNoPaymentsListItem(""))
                    adapter.update(listOfItems)
                    showContent()
                }
                is LoadingData.Error<List<BankAccountPaymentListItem>> -> {
                    Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_LONG).show()
                    val listOfItems = mutableListOf<ListItem>()
                    listOfItems.add(
                        adapter.content[0] as BankAccountDetailsListItem
                    )
                    adapter.update(listOfItems)
                }
                else -> {
                }
            }
        }
    }

    private fun onBankAccountDetailsClicked(isExpanded: Boolean) {
        (adapter.content.get(0) as BankAccountDetailsListItem).isExpanded = isExpanded
        viewModel.isExpanded = isExpanded
        if (isExpanded)
            adapter.notifyItemChanged(0)
        else {
            adapter.notifyDataSetChanged()
            binding.bankAccountDetailsRecycleView.post {
                binding.bankAccountDetailsRecycleView.scrollToPosition(
                    0
                )
            }
        }
    }

    private fun onBankAccountPaymentClicked(payment: BankAccountPaymentListItem) {
        val dest = BankAccountDetailsFragmentDirections.toPaymentDetailsFragment(
            payment,
            viewModel.bankAccountDataItem?.paymentInstructions
        )
        navigate(dest)

    }

    private fun onBuyClicked() {
        val dest = BankAccountDetailsFragmentDirections.toBuyUsdcFragment(
            viewModel.bankAccountDataItem!!,
            BankAccountInfoDataItem(
                viewModel.walletId!!,
                viewModel.walletAddress!!,
                viewModel.feePercent!!,
                viewModel.limits!!
            )
        )
        navigate(dest)
    }

    private fun onSellClicked() {
        val dest = BankAccountDetailsFragmentDirections.toSellUsdcFragment(
            viewModel.bankAccountDataItem!!,
            BankAccountInfoDataItem(
                viewModel.walletId!!,
                viewModel.walletAddress!!,
                viewModel.feePercent!!,
                viewModel.limits!!
            )
        )
        navigate(dest)
    }

    private fun bindBankAccountDetails(bankAccount: BankAccountDataItem) {
        setToolbarTitle(bankAccount.bankName)
        if (adapter.content.size > 0 && adapter.content[0] is BankAccountDetailsListItem) {
            adapter.content[0] = bankAccount.toDetailsListItem(viewModel.isExpanded)
            adapter.notifyItemChanged(0)

        } else {
            adapter.content.add(
                0, bankAccount.toDetailsListItem(viewModel.isExpanded)
            )
            adapter.notifyDataSetChanged()
        }

    }

}