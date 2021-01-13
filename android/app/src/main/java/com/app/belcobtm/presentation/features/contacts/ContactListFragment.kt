package com.app.belcobtm.presentation.features.contacts

import android.Manifest
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentContactListBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.extensions.actionDoneListener
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.contacts.adapter.ContactListDiffUtil
import com.app.belcobtm.presentation.features.contacts.adapter.delegate.ContactDelegate
import com.app.belcobtm.presentation.features.contacts.adapter.delegate.ContactHeaderDelegate
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class ContactListFragment : BaseFragment<FragmentContactListBinding>() {

    override var isMenuEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true

    private val viewModel: ContactListViewModel by viewModel()
    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter(ContactListDiffUtil()).apply {
            registerDelegate(ContactDelegate(viewModel::selectContact))
            registerDelegate(ContactHeaderDelegate())
        }
    }
    private val searchQueryTextWatcher by lazy {
        object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(editable: Editable) {
                val formattedSearchQuery = viewModel.getFormattedPhoneNumber(editable.toString())
                if (formattedSearchQuery != editable.toString()) {
                    editable.replace(0, editable.length, formattedSearchQuery)
                    return
                }
                viewModel.clearSelectedContact()
                viewModel.loadContacts(formattedSearchQuery)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadInitialDataWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun loadInitialData() {
        viewModel.loadContacts(binding.searchEditText.text.toString())
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
        searchEditText.addTextChangedListener(searchQueryTextWatcher)
        searchEditText.actionDoneListener {
            hideKeyboard()
            validateSearchQuery()
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
                searchEditText.removeTextChangedListener(searchQueryTextWatcher)
                searchEditText.setText(viewModel.getFormattedPhoneNumber(it.phoneNumber))
                viewModel.loadContacts(it.phoneNumber)
                searchEditText.addTextChangedListener(searchQueryTextWatcher)
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
                    "", phoneNumber, selectedContact?.photoUri, selectedContact?.displayName
                )
            )
        } else {
            binding.searchView.error = getString(R.string.transfer_phone_number_invalid)
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentContactListBinding =
        FragmentContactListBinding.inflate(inflater, container, false)
}