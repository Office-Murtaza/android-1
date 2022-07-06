package com.belcobtm.presentation.screens.settings.referral.contacts

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentInviteFromContactsBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.toHtmlSpan
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.screens.contacts.adapter.delegate.ContactHeaderDelegate
import com.belcobtm.presentation.screens.settings.referral.contacts.adapter.InviteContactsDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions


@RuntimePermissions
class InviteFromContactsFragment : BaseFragment<FragmentInviteFromContactsBinding>() {

    override var isMenuEnabled: Boolean = true
    override val isBackButtonEnabled: Boolean = true

    val args by navArgs<InviteFromContactsFragmentArgs>()

    override val retryListener: View.OnClickListener =
        View.OnClickListener {
            loadInitialDataWithPermissionCheck()
        }
    private val viewModel: InviteFromContactsViewModel by viewModel()
    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter(InviteContactsDiffUtil()).apply {
            registerDelegate(InviteContactsDelegate(viewModel::selectContact))
            registerDelegate(ContactHeaderDelegate())
        }
    }
    private val searchQueryTextWatcher by lazy {
        SafeDecimalEditTextWatcher { editable ->
            val rawQuery = editable.toString()
            val formattedSearchQuery = viewModel.getFormattedPhoneNumber(rawQuery)
            if (formattedSearchQuery != editable.toString()) {
                editable.replace(0, editable.length, formattedSearchQuery)
                return@SafeDecimalEditTextWatcher
            }
            viewModel.loadContacts(formattedSearchQuery)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadInitialDataWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun loadInitialData() {
        viewModel.loadInitialData(binding.searchEditText.text.toString())
        // add textwatcher only after permission granting
        binding.searchEditText.addTextChangedListener(searchQueryTextWatcher)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun FragmentInviteFromContactsBinding.initListeners() {
        searchEditText.actionDoneListener {
            hideKeyboard()
            viewModel.createFormattedRecipients()
        }
        nextButton.setOnClickListener {
            viewModel.createFormattedRecipients()
        }
    }

    override fun FragmentInviteFromContactsBinding.initViews() {
        contactsList.adapter = adapter
        setToolbarTitle(R.string.invite_from_contacts_screen_title)
    }

    override fun FragmentInviteFromContactsBinding.initObservers() {
        viewModel.initialLoadingData.listen()
        viewModel.contacts.observe(viewLifecycleOwner, adapter::update)
        viewModel.sendReferralLoadingData.listen(success = { recipients ->
            if (recipients.isNotEmpty()) {
                binding.searchView.error = null
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(recipients))
                smsIntent.putExtra("sms_body", args.message)
                startActivity(smsIntent)
            } else {
                binding.searchView.error = getString(R.string.referral_contacts_not_selected_error)
            }
        })
        viewModel.coinsToReceive.observe(viewLifecycleOwner) {
            coinsToReceive.text = getString(R.string.referral_contacts_coins_to_receive_format, it)
                .toHtmlSpan()
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentInviteFromContactsBinding =
        FragmentInviteFromContactsBinding.inflate(inflater, container, false)
}