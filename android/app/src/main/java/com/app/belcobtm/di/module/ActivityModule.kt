package com.app.belcobtm.di.module

import com.app.belcobtm.ui.main.coins.sell.SellContract
import com.app.belcobtm.ui.main.coins.sell.SellPresenter
import dagger.Module
import dagger.Provides

@Module
class ActivityModule {


    @Provides
    fun provideSellPresenter(): SellContract.Presenter = SellPresenter()
}