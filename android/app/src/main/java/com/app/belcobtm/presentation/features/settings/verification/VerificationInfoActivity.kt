package com.app.belcobtm.presentation.features.settings.verification

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_verification_info.*
import org.koin.android.viewmodel.ext.android.viewModel

class VerificationInfoActivity : BaseActivity() {
    private val viewModel: VerificationInfoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_info)
        initViews()
        initListeners()
        initObservers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        setSupportActionBar(toolbarView)
        supportActionBar?.let { toolbar ->
            toolbar.setDisplayHomeAsUpEnabled(true)
            toolbar.setDisplayShowHomeEnabled(true)
            toolbar.setTitle(R.string.verification_screen_title)
        }
    }

    private fun initListeners() {
        verifyButtonView.setOnClickListener {

        }
    }

    private fun initObservers() {
        viewModel.verificationInfoLiveData.observe(this, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> showProgress(true)
                is LoadingData.Success -> with(loadingData.data) {
                    statusValueView.text = resources.getStringArray(R.array.verification_status_array)[status.code]
                    txLimitValueView.text = getString(R.string.verification_unit_usd, txLimit.toString())
                    dailyLimitValueView.text = getString(R.string.verification_unit_usd, dayLimit.toString())
                    messageView.text = message

                    showProgress(false)
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    showProgress(false)
                }
            }

        })
    }
}