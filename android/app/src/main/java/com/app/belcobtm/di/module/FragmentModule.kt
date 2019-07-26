package com.app.belcobtm.di.module

import com.app.belcobtm.ui.coins.atm.AtmContract
import com.app.belcobtm.ui.coins.atm.AtmPresenter
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

    @Provides
    fun provideAtmPresenter(): AtmContract.Presenter {
        return AtmPresenter()
    }

}