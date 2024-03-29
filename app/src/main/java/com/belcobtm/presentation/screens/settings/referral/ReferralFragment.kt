package com.belcobtm.presentation.screens.settings.referral

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.belcobtm.R
import com.belcobtm.databinding.FragmentReferralsBinding
import com.belcobtm.presentation.tools.extensions.toHtmlSpan
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import com.belcobtm.presentation.core.helper.ClipBoardHelper
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class ReferralFragment : BaseFragment<FragmentReferralsBinding>() {

    override val isBackButtonEnabled: Boolean = true

    private val viewModel by viewModel<ReferralViewModel>()
    private val clipBoardHelper: ClipBoardHelper by inject()
    private val currencyFormatter: Formatter<Double> by inject(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentReferralsBinding =
        FragmentReferralsBinding.inflate(inflater, container, false)

    override fun FragmentReferralsBinding.initViews() {
        setToolbarTitle(R.string.referral_screen_title)
        additionalInfo.text = getString(R.string.referrals_screen_additional_title).toHtmlSpan()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadData()
    }

    override fun FragmentReferralsBinding.initListeners() {
        shareButton.setOnClickListener {
            viewModel.getReferralMessage().takeIf { it.isNotEmpty() }?.let {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, it)
                    type = "text/plain"
                }
                val chooser = Intent.createChooser(shareIntent, it)
                startActivity(chooser)
            }
        }
        inviteFromContacts.setOnClickListener {
            findNavController().navigate(
                ReferralFragmentDirections.referralsToInviteFromContactsFragment(
                    viewModel.getReferralMessage()
                )
            )
        }
        copyLink.setOnClickListener {
            viewModel.getReferralLink().takeIf { it.isNotEmpty() }?.let {
                clipBoardHelper.setTextToClipboard(it)
                Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun FragmentReferralsBinding.initObservers() {
        viewModel.initialLoadingData.listen(success = { (dataItem, earnedUsd) ->
            copyLink.text = dataItem.link
            invitedUsersValue.text = getString(
                R.string.referral_screen_users_formatted, dataItem.invited
            )
            totalEarnValue.text = getString(
                R.string.referral_screen_coin_amount_formatted, dataItem.earned.toStringCoin()
            )
            usdAmountLabel.text = getString(
                R.string.referral_screen_usd_amount_formatted,
                currencyFormatter.format(earnedUsd)
            )
        })
    }
}