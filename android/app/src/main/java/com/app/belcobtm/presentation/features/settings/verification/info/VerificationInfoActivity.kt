package com.app.belcobtm.presentation.features.settings.verification.info

import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.type.VerificationStatus
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
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
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> with(loadingData.data) {
                    statusValueView.setTextColor(getColorByStatus(status))
                    rebuildVerifyButton(status)
                    statusValueView.text = resources.getStringArray(R.array.verification_status_array)[status.code]
                    txLimitValueView.text = getString(R.string.verification_unit_usd, txLimit.toString())
                    dailyLimitValueView.text = getString(R.string.verification_unit_usd, dayLimit.toString())
                    messageView.text = message
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.MessageError -> showError(loadingData.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
        })
    }

    private fun getColorByStatus(status: VerificationStatus): Int = ContextCompat.getColor(
        applicationContext,
        when (status) {
            VerificationStatus.VERIFICATION_PENDING -> R.color.verification_status_verification_pending
            VerificationStatus.VERIFICATION_REJECTED -> R.color.verification_status_verification_rejected
            VerificationStatus.VERIFIED -> R.color.verification_status_verified
            VerificationStatus.VIP_VERIFICATION_PENDING -> R.color.verification_status_vip_verification_pending
            VerificationStatus.VIP_VERIFICATION_REJECTED -> R.color.verification_status_vip_verification_rejected
            VerificationStatus.VIP_VERIFIED -> R.color.verification_status_vip_verified
            else -> R.color.verification_status_not_verified
        }
    )

    private fun rebuildVerifyButton(status: VerificationStatus) = when (status) {
        VerificationStatus.VERIFIED,
        VerificationStatus.VIP_VERIFICATION_REJECTED -> {
            verifyButtonView.setText(R.string.verification_vip_verify)
            verifyButtonView.show()
        }
        VerificationStatus.NOT_VERIFIED,
        VerificationStatus.VERIFICATION_PENDING,
        VerificationStatus.VERIFICATION_REJECTED,
        VerificationStatus.VIP_VERIFICATION_PENDING -> {
            verifyButtonView.setText(R.string.verification_verify)
            verifyButtonView.show()
        }
        VerificationStatus.VIP_VERIFIED -> verifyButtonView.hide()
    }
}