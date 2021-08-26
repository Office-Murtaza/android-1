package com.belcobtm.presentation.features.wallet.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.belcobtm.R
import com.belcobtm.databinding.FragmentWalletsBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinsAdapter
import org.koin.android.ext.android.inject

class WalletsFragment : BaseFragment<FragmentWalletsBinding>() {
    private val viewModel: WalletsViewModel by inject()
    private val adapter: AddWalletCoinsAdapter = AddWalletCoinsAdapter { position, isChecked ->
        viewModel.changeCoinState(position, isChecked)
    }
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { viewModel.retry() }

    override fun FragmentWalletsBinding.initObservers() {
        viewModel.coinListLiveData.listen(
            success = adapter::setItemList,
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showSnackBar(it.message ?: "")
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    else -> showErrorSomethingWrong()
                }
            }
        )
    }

    override fun FragmentWalletsBinding.initViews() {
        setToolbarTitle(R.string.wallets_screen_title)
        ContextCompat.getDrawable(requireContext(), R.drawable.bg_divider)?.let {
            val itemDecorator =
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            itemDecorator.setDrawable(it)
            coinListView.addItemDecoration(itemDecorator)
        }
        coinListView.adapter = adapter
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWalletsBinding =
        FragmentWalletsBinding.inflate(inflater, container, false)
}
