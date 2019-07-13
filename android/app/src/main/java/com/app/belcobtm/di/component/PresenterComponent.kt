package com.app.belcobtm.di.component

import com.app.belcobtm.di.module.PresenterModule
import com.app.belcobtm.ui.auth.create_wallet.CreateWalletPresenter
import com.app.belcobtm.ui.auth.login.LoginPresenter
import com.app.belcobtm.ui.auth.pin.PinPresenter
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedPresenter
import com.app.belcobtm.ui.auth.recover_wallet.RecoverWalletPresenter
import com.app.belcobtm.ui.coins.balance.BalancePresenter
import dagger.Component


@Component(modules = arrayOf(PresenterModule::class))
interface PresenterComponent {

    fun inject(presenter: LoginPresenter)
    fun inject(presenter: RecoverWalletPresenter)
    fun inject(presenter: CreateWalletPresenter)
    fun inject(presenter: RecoverSeedPresenter)
    fun inject(presenter: BalancePresenter)
    fun inject(presenter: PinPresenter)

}