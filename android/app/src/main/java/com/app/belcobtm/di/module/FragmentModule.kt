package com.app.belcobtm.di.module

import com.app.belcobtm.ui.coins.balance.BalanceContract
import com.app.belcobtm.ui.coins.balance.BalancePresenter
import dagger.Module
import dagger.Provides


@Module
class FragmentModule {

    @Provides
    fun provideBalancePresenter(): BalanceContract.Presenter {
        return BalancePresenter()
    }

}