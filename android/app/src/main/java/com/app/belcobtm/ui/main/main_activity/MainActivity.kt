package com.app.belcobtm.ui.main.main_activity

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.authorization.interactor.ClearAppDataUseCase
import com.app.belcobtm.domain.wallet.interactor.GetLocalCoinListUseCase
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.authorization.seed.recover.RecoverSeedFragment
import com.app.belcobtm.presentation.features.wallet.balance.BalanceFragment
import com.app.belcobtm.ui.main.atm.AtmFragment
import com.app.belcobtm.ui.main.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val coinListUseCase: GetLocalCoinListUseCase by inject()
    private val clearAppDataUseCase: ClearAppDataUseCase by inject()
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coinListUseCase.invoke { dataItemList ->
            if (dataItemList.isEmpty() && prefsHelper.apiSeed.isNotEmpty()) {
                clearAppDataUseCase.invoke {
                    launchData()
                }
            } else {
                launchData()
            }
        }
    }

    private fun launchData() {
        setTheme(R.style.AppThemeNoActionBar)
        setContentView(R.layout.activity_main)
        checkPinEntered()
        bottom_bar.setOnNavigationItemSelectedListener(this)
        setFragment(BalanceFragment())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (bottom_bar.selectedItemId != item.itemId) {
            when (item.itemId) {
                R.id.menu_wallet -> {
                    setFragment(BalanceFragment())
                    return true
                }
                R.id.menu_atm -> {
                    setFragment(AtmFragment())
                    return true
                }
                R.id.menu_settings -> {
                    setFragment(SettingsFragment())
                    return true
                }
            }
        }
        return false
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun checkPinEntered() {
        when {
            prefsHelper.accessToken.isEmpty() -> {
                finishAffinity()
                // startActivity(Intent(this, WelcomeFragment::class.java))//TODO it's fragment now
            }
            prefsHelper.apiSeed.isEmpty() -> startActivity(Intent(this, RecoverSeedFragment::class.java))
            prefsHelper.userPin.isNotBlank() -> {
                val mode = PinActivity.Companion.Mode.MODE_PIN
                val intent = PinActivity.getIntent(this, mode)
                startActivityForResult(intent, mode.ordinal)
            }
            else -> {
                val mode = PinActivity.Companion.Mode.MODE_CREATE_PIN
                val intent = PinActivity.getIntent(this, mode)
                startActivityForResult(intent, mode.ordinal)
            }
        }
    }
}
