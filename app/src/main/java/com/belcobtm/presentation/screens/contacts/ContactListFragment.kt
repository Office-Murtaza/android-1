package com.belcobtm.presentation.screens.contacts

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import com.belcobtm.R
import com.belcobtm.databinding.FragmentContactListBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.contacts.adapter.ContactListDiffUtil
import com.belcobtm.presentation.screens.contacts.adapter.delegate.ContactDelegate
import com.belcobtm.presentation.screens.contacts.adapter.delegate.ContactHeaderDelegate
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.setTextSilently
import org.koin.androidx.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class ContactListFragment : BaseFragment<FragmentContactListBinding>() {

    override var isMenuEnabled: Boolean = true
    override val isBackButtonEnabled: Boolean = true

    private val viewModel: ContactListViewModel by viewModel()
    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter(ContactListDiffUtil()).apply {
            registerDelegate(ContactDelegate(viewModel::selectContact))
            registerDelegate(ContactHeaderDelegate())
        }
    }
    private lateinit var searchQueryTextWatcher: TextWatcher

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadInitialDataWithPermissionCheck()
        binding.root.post {
            binding.searchEditText.requestFocus()
            binding.searchEditText.setSelection(
                binding.searchEditText.length()
            )
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showSoftInput(binding.searchEditText, 0)
        }
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun loadInitialData() {
        viewModel.loadContacts(binding.searchEditText.text.toString())
        // add textwatcher only after permission granting
        searchQueryTextWatcher = binding.searchEditText.addTextChangedListener { editable ->
            viewModel.clearSelectedContact()
            viewModel.loadContacts(editable.toString())
        }
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

    override fun FragmentContactListBinding.initListeners() {
        searchEditText.apply {
            addTextChangedListener(PhoneNumberFormattingTextWatcher())
            actionDoneListener {
                hideKeyboard()
                validateSearchQuery()
            }
        }
        nextButton.setOnClickListener {
            validateSearchQuery()
        }
    }

    override fun FragmentContactListBinding.initViews() {
        contactsList.adapter = adapter
        setToolbarTitle(R.string.transfer_screen_title)
    }

    override fun FragmentContactListBinding.initObservers() {
        viewModel.contacts.observe(viewLifecycleOwner, adapter::update)
        viewModel.selectedContact.observe(viewLifecycleOwner) {
            if (it != null) {
                searchEditText.setTextSilently(searchQueryTextWatcher, it.phoneNumber)
            }
        }
    }

    private fun validateSearchQuery() {
        val phoneNumber = binding.searchEditText.text.toString()
        if (viewModel.isValidMobileNumber(phoneNumber)) {
            binding.searchView.error = null
            val selectedContact = viewModel.selectedContact.value
            navigate(
                ContactListFragmentDirections.toSendGifFragment(
                    phoneNumber, selectedContact?.photoUri, selectedContact?.displayName
                )
            )
        } else {
            binding.searchView.error = getString(R.string.transfer_phone_number_invalid)
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentContactListBinding =
        FragmentContactListBinding.inflate(inflater, container, false)
}