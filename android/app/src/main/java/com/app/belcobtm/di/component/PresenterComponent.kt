package com.app.belcobtm.di.component

import com.app.belcobtm.di.module.PresenterModule
import com.app.belcobtm.ui.main.atm.AtmPresenter
import com.app.belcobtm.ui.main.coins.sell.SellPresenter
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftPresenter
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawPresenter
import dagger.Component

@Component(modules = [PresenterModule::class])
interface PresenterComponent {

    fun inject(presenter: AtmPresenter)
    fun inject(presenter: WithdrawPresenter)
    fun inject(presenter: SendGiftPresenter)
    fun inject(sellPresenter: SellPresenter)
}