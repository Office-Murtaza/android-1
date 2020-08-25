package com.app.belcobtm.di.module

import com.app.belcobtm.ui.main.coins.sell.SellContract
import com.app.belcobtm.ui.main.coins.sell.SellPresenter
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftContract
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftPresenter
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawContract
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawPresenter
import dagger.Module
import dagger.Provides

@Module
class ActivityModule {

    @Provides
    fun provideWithdrawPresenter(): WithdrawContract.Presenter = WithdrawPresenter()

    @Provides
    fun provideSendGiftPresenter(): SendGiftContract.Presenter = SendGiftPresenter()

    @Provides
    fun provideSellPresenter(): SellContract.Presenter = SellPresenter()
}