package com.app.belcobtm.di.module

import com.app.belcobtm.ui.main.atm.AtmContract
import com.app.belcobtm.ui.main.atm.AtmPresenter
import dagger.Module
import dagger.Provides


@Module
class FragmentModule {


    @Provides
    fun provideAtmPresenter(): AtmContract.Presenter {
        return AtmPresenter()
    }

}