package com.app.belcobtm.ui.main.main_activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.app.belcobtm.R
import com.app.belcobtm.domain.authorization.interactor.ClearAppDataUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.authorization.welcome.WelcomeActivity
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedActivity
import com.app.belcobtm.ui.main.atm.AtmFragment
import com.app.belcobtm.ui.main.coins.balance.BalanceFragment
import com.app.belcobtm.ui.main.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity : BaseMvpActivity<MainContract.View, MainContract.Presenter>(), MainContract.View,
    BottomNavigationView.OnNavigationItemSelectedListener {
    private val coinListUseCase: GetCoinListUseCase by inject()
    private val clearAppDataUseCase: ClearAppDataUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coinListUseCase.invoke {
            if (it.isEmpty() && !mPresenter.isApiSeedEmpty()) {
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
        mPresenter.checkPinEntered()
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

    override fun onTokenNotSaved() {
        finishAffinity()
        startActivity(Intent(this, WelcomeActivity::class.java))
    }

    override fun onPinSaved() {
        val mode = PinActivity.Companion.Mode.MODE_PIN
        val intent = PinActivity.getIntent(this, mode)
        startActivityForResult(intent, mode.ordinal)
    }

    override fun onPinNotSaved() {
        val mode = PinActivity.Companion.Mode.MODE_CREATE_PIN
        val intent = PinActivity.getIntent(this, mode)
        startActivityForResult(intent, mode.ordinal)
    }

    override fun onSeedNotSaved() {
        startActivity(Intent(this, RecoverSeedActivity::class.java))
    }
}
