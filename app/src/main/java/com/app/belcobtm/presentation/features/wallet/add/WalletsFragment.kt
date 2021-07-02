package com.app.belcobtm.presentation.features.wallet.add

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentWalletsBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinsAdapter
import org.koin.android.ext.android.inject

class WalletsFragment : BaseFragment<FragmentWalletsBinding>() {
    private val viewModel: WalletsViewModel by inject()
    private val adapter: AddWalletCoinsAdapter = AddWalletCoinsAdapter { position, isChecked ->
        viewModel.changeCoinState(position, isChecked)
    }
    override val backPressedListener: View.OnClickListener = View.OnClickListener {
        setFragmentResult(REQUEST_KEY, bundleOf())
        popBackStack()
    }
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        viewModel.retry()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            setFragmentResult(REQUEST_KEY, bundleOf())
            hideKeyboard()
            popBackStack()
            true
        } else {
            false
        }

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
            val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            itemDecorator.setDrawable(it)
            coinListView.addItemDecoration(itemDecorator)
        }
        coinListView.adapter = adapter
    }

    companion object {
        const val REQUEST_KEY = "request_key_manage_wallets_fragment"
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWalletsBinding =
        FragmentWalletsBinding.inflate(inflater, container, false)
}
