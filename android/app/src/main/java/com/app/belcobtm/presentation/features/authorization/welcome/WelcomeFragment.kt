package com.app.belcobtm.presentation.features.authorization.welcome

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_welcome.*
import kotlinx.android.synthetic.main.view_support_dialog.*

class WelcomeFragment : BaseFragment() {
    override val isToolbarEnabled: Boolean = false
    override val resourceLayout: Int = R.layout.fragment_welcome
    override val backPressedListener: View.OnClickListener = View.OnClickListener { requireActivity().finish() }

    override fun initViews() {
        pagerView.apply {
            adapter = WelcomePagerAdapter()
            adapter?.registerAdapterDataObserver(pagerIndicatorView.adapterDataObserver)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        pagerIndicatorView.setViewPager(pagerView)
    }

    override fun initListeners() {
        contactSupportButtonView.setOnClickListener { showSupportDialog() }
        createNewWalletButtonView.setOnClickListener { navigate(R.id.to_create_wallet_fragment) }
        recoverWalletButtonView.setOnClickListener { navigate(R.id.to_recover_wallet_fragment) }
    }

    private fun showSupportDialog() {
        val dialog = MaterialAlertDialogBuilder(contactSupportButtonView.context)
            .setView(R.layout.view_support_dialog)
            .show()
        val supportEmail = getString(R.string.welcome_screen_support_email)
        val supportPhone = getString(R.string.welcome_screen_support_phone)
        dialog.callButtonView.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$supportPhone")
            startActivity(intent)
            dialog.cancel()
        }
        dialog.emailButtonView.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$supportEmail")
            if (intent.resolveActivity(contactSupportButtonView.context.packageManager) != null) {
                startActivity(intent)
                dialog.cancel()
            }
        }
        dialog.cancelButtonView.setOnClickListener { dialog.cancel() }
        dialog.setCanceledOnTouchOutside(false)
    }
}
