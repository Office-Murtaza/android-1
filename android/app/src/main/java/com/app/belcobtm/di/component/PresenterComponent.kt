package com.app.belcobtm.di.component

import com.app.belcobtm.di.module.PresenterModule
import com.app.belcobtm.presentation.features.authorization.recover.wallet.RecoverWalletPresenter
import com.app.belcobtm.ui.auth.create_wallet.CreateWalletPresenter
import com.app.belcobtm.ui.auth.pin.PinPresenter
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedPresenter
import com.app.belcobtm.ui.main.atm.AtmPresenter
import com.app.belcobtm.ui.main.coins.balance.BalancePresenter
import com.app.belcobtm.ui.main.coins.details.DetailsPresenter
import com.app.belcobtm.ui.main.coins.sell.SellPresenter
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftPresenter
import com.app.belcobtm.ui.main.coins.transactions.TransactionsPresenter
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawPresenter
import com.app.belcobtm.ui.main.main_activity.MainPresenter
import com.app.belcobtm.ui.main.settings.change_pass.ChangePassPresenter
import com.app.belcobtm.ui.main.settings.check_pass.CheckPassPresenter
import com.app.belcobtm.ui.main.settings.phone.ShowPhonePresenter
import dagger.Component


@Component(modules = [PresenterModule::class])
interface PresenterComponent {

    fun inject(presenter: RecoverWalletPresenter)
    fun inject(presenter: CreateWalletPresenter)
    fun inject(presenter: RecoverSeedPresenter)
    fun inject(presenter: MainPresenter)
    fun inject(presenter: PinPresenter)
    fun inject(presenter: BalancePresenter)
    fun inject(presenter: AtmPresenter)
    fun inject(presenter: CheckPassPresenter)
    fun inject(presenter: ChangePassPresenter)
    fun inject(presenter: ShowPhonePresenter)
    fun inject(presenter: TransactionsPresenter)
    fun inject(presenter: WithdrawPresenter)
    fun inject(presenter: SendGiftPresenter)
    fun inject(sellPresenter: SellPresenter)
    fun inject(detailsPresenter: DetailsPresenter)
}