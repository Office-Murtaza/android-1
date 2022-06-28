package com.belcobtm.presentation.features.wallet.balance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.belcobtm.R
import com.belcobtm.databinding.FragmentBalanceBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.wallet.balance.adapter.CoinsAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

@ExperimentalCoroutinesApi
class WalletFragment : BaseFragment<FragmentBalanceBinding>() {

    private val viewModel: WalletViewModel by viewModel()
    private lateinit var adapter: CoinsAdapter
    private val currencyFormatter: Formatter<Double> by inject(
        named(DoubleCurrencyPriceFormatter.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )
    override val isToolbarEnabled: Boolean = false
    override var isMenuEnabled: Boolean = true
    override val isFirstShowContent: Boolean = false

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBalanceBinding =
        FragmentBalanceBinding.inflate(inflater, container, false)

    override fun FragmentBalanceBinding.initViews() {
        updateStatusBar()
        adapter = CoinsAdapter(currencyFormatter) {
            navigate(WalletFragmentDirections.toTransactionsFragment(it.code))
        }
        listView.adapter = adapter
    }

    private fun updateStatusBar() {
        with(requireActivity().window) {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = false
        }
    }

    override fun FragmentBalanceBinding.initObservers() {
        viewModel.needToShowRestrictions.observe(viewLifecycleOwner) {
            if (it) {
                showToast(getString(R.string.restrictions_message))
                viewModel.restrictionsShown()
            }
        }

        viewModel.balanceLiveData.listen(
            success = {
                balanceView.text = currencyFormatter.format(it.first)
                adapter.setItemList(it.second)
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showToast(it.message ?: "")
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    else -> showErrorSomethingWrong()
                }
            }
        )
    }

}
