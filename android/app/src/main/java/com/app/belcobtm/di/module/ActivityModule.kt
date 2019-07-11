package com.app.belcobtm.di.module

import com.app.belcobtm.ui.auth.create_wallet.CreateWalletContract
import com.app.belcobtm.ui.auth.create_wallet.CreateWalletPresenter
import com.app.belcobtm.ui.auth.login.LoginContract
import com.app.belcobtm.ui.auth.login.LoginPresenter
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedContract
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedPresenter
import com.app.belcobtm.ui.auth.recover_wallet.RecoverWalletContract
import com.app.belcobtm.ui.auth.recover_wallet.RecoverWalletPresenter
import com.app.belcobtm.ui.coins.balance.BalanceContract
import com.app.belcobtm.ui.coins.balance.BalancePresenter
import dagger.Module
import dagger.Provides


@Module
class ActivityModule {

    @Provides
    fun provideLoginPresenter(): LoginContract.Presenter {
        return LoginPresenter()
    }

    @Provides
    fun provideCreateWalletPresenter(): CreateWalletContract.Presenter {
        return CreateWalletPresenter()
    }

    @Provides
    fun provideRecoverSeedPresenter(): RecoverSeedContract.Presenter {
        return RecoverSeedPresenter()
    }

    @Provides
    fun provideRecoverWalletPresenter(): RecoverWalletContract.Presenter {
        return RecoverWalletPresenter()
    }

    @Provides
    fun provideBalancePresenter(): BalanceContract.Presenter {
        return BalancePresenter()
    }



}