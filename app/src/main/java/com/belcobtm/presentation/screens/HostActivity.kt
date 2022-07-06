package com.belcobtm.presentation.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.belcobtm.R
import com.belcobtm.domain.authorization.interactor.ClearAppDataUseCase
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HostActivity : AppCompatActivity() {
    private val clearAppDataUseCase: ClearAppDataUseCase by inject()
    private val hostViewModel: HostViewModel by viewModel()
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

    companion object {
        const val FORCE_UNLINK_KEY = "force.unlink.key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        if (intent.extras?.getBoolean(FORCE_UNLINK_KEY) == true) {
            showAuthorizationScreen()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeeplink(intent?.data)
    }

    private fun handleDeeplink(deeplink: Uri?) {
        if (deeplink != null) {
            navController.navigate(
                R.id.nav_pin_code, bundleOf(PinCodeFragment.KEY_DEEPLINK to deeplink.toString())
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        hostViewModel.disconnectFromSocket()
    }

    fun showAuthorizationScreen() {
        clearAppDataUseCase.invoke()
        navController.navigate(R.id.nav_authorization)
    }
}
