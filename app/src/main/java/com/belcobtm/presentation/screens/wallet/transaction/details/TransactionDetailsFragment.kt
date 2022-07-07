package com.belcobtm.presentation.screens.wallet.transaction.details

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTransactionDetailsBinding
import com.belcobtm.presentation.tools.decorator.DividerDecorator
import com.belcobtm.presentation.tools.decorator.SpaceDecorator
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.wallet.transaction.details.adapter.TransactionDetailsAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TransactionDetailsFragment :
    BaseFragment<FragmentTransactionDetailsBinding>(),
    TransactionDetailsAdapter.IOnLinkClickListener {

    private val viewModel: TransactionDetailsViewModel by viewModel {
        val args = TransactionDetailsFragmentArgs.fromBundle(requireArguments())
        parametersOf(args.transactionId, args.coinCode)
    }
    override val isBackButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true

    override fun FragmentTransactionDetailsBinding.initViews() {
        setToolbarTitle(getString(R.string.transaction_details_screen_title))
        initTransactionDetailsRecyclerView()
    }

    override fun FragmentTransactionDetailsBinding.initObservers() {
        viewModel.transactionDetailsLiveData.listen(success = { list ->
            val adapter = binding.rvTransactionDetails.adapter as TransactionDetailsAdapter
            adapter.submitList(list)
        })
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTransactionDetailsBinding =
        FragmentTransactionDetailsBinding.inflate(inflater, container, false)

    private fun initTransactionDetailsRecyclerView() {
        val divider = ContextCompat.getDrawable(requireContext(), R.drawable.divider_transactions)
        val hSpace = resources.getDimensionPixelOffset(R.dimen.margin_main)
        val spaceDecoration = SpaceDecorator(0, 0, hSpace, hSpace)
        val dividerDecorator = DividerDecorator(divider!!.mutate(), hSpace, hSpace)
        with(binding.rvTransactionDetails) {
            setHasFixedSize(true)
            addItemDecoration(spaceDecoration)
            addItemDecoration(dividerDecorator)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TransactionDetailsAdapter(this@TransactionDetailsFragment)
        }
    }

    override fun onLinkClicked(link: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(link)
        startActivity(i)
    }
}
