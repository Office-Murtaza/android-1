package com.app.belcobtm.di.module

import com.app.belcobtm.ui.auth.create_wallet.CreateWalletContract
import com.app.belcobtm.ui.auth.create_wallet.CreateWalletPresenter
import com.app.belcobtm.ui.auth.login.LoginContract
import com.app.belcobtm.ui.auth.login.LoginPresenter
import com.app.belcobtm.ui.auth.pin.PinContract
import com.app.belcobtm.ui.auth.pin.PinPresenter
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedContract
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedPresenter
import com.app.belcobtm.ui.auth.recover_wallet.RecoverWalletContract
import com.app.belcobtm.ui.auth.recover_wallet.RecoverWalletPresenter
import com.app.belcobtm.ui.main.coins.settings.check_pass.CheckPassContract
import com.app.belcobtm.ui.main.main_activity.MainContract
import com.app.belcobtm.ui.main.main_activity.MainPresenter
import com.app.belcobtm.ui.main.coins.visibility.VisibilityCoinsContract
import com.app.belcobtm.ui.main.coins.visibility.VisibilityCoinsPresenter
import com.app.belcobtm.ui.main.settings.check_pass.CheckPassPresenter
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
    fun provideBalancePresenter(): MainContract.Presenter {
        return MainPresenter()
    }

    @Provides
    fun providePinPresenter(): PinContract.Presenter {
        return PinPresenter()
    }

    @Provides
    fun provideVisibilityCoinsPresenter(): VisibilityCoinsContract.Presenter {
        return VisibilityCoinsPresenter()
    }

    @Provides
    fun provideCheckPassPresenter(): CheckPassContract.Presenter {
        return CheckPassPresenter()
    }


}