package com.app.belcobtm.di.module

import com.app.belcobtm.presentation.features.authorization.wallet.create.CreateWalletContract
import com.app.belcobtm.presentation.features.authorization.wallet.create.CreateWalletPresenter
import com.app.belcobtm.ui.auth.pin.PinContract
import com.app.belcobtm.ui.auth.pin.PinPresenter
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedContract
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedPresenter
import com.app.belcobtm.ui.main.coins.details.DetailsContract
import com.app.belcobtm.ui.main.coins.details.DetailsPresenter
import com.app.belcobtm.ui.main.coins.sell.SellContract
import com.app.belcobtm.ui.main.coins.sell.SellPresenter
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftContract
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftPresenter
import com.app.belcobtm.ui.main.coins.settings.change_pass.ChangePassContract
import com.app.belcobtm.ui.main.coins.settings.check_pass.CheckPassContract
import com.app.belcobtm.ui.main.coins.settings.phone.ShowPhoneContract
import com.app.belcobtm.ui.main.coins.transactions.TransactionsContract
import com.app.belcobtm.ui.main.coins.transactions.TransactionsPresenter
import com.app.belcobtm.ui.main.coins.visibility.VisibilityCoinsContract
import com.app.belcobtm.ui.main.coins.visibility.VisibilityCoinsPresenter
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawContract
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawPresenter
import com.app.belcobtm.ui.main.main_activity.MainContract
import com.app.belcobtm.ui.main.main_activity.MainPresenter
import com.app.belcobtm.ui.main.settings.change_pass.ChangePassPresenter
import com.app.belcobtm.ui.main.settings.check_pass.CheckPassPresenter
import com.app.belcobtm.ui.main.settings.phone.ShowPhonePresenter
import dagger.Module
import dagger.Provides


@Module
class ActivityModule {

    @Provides
    fun provideCreateWalletPresenter(): CreateWalletContract.Presenter {
        return CreateWalletPresenter()
    }

    @Provides
    fun provideRecoverSeedPresenter(): RecoverSeedContract.Presenter {
        return RecoverSeedPresenter()
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

    @Provides
    fun provideChangePassPresenter(): ChangePassContract.Presenter {
        return ChangePassPresenter()
    }

    @Provides
    fun provideShowPhonePresenter(): ShowPhoneContract.Presenter {
        return ShowPhonePresenter()
    }

    @Provides
    fun provideTransactionsPresenter(): TransactionsContract.Presenter {
        return TransactionsPresenter()
    }

    @Provides
    fun provideWithdrawPresenter(): WithdrawContract.Presenter {
        return WithdrawPresenter()
    }

    @Provides
    fun provideSendGiftPresenter(): SendGiftContract.Presenter {
        return SendGiftPresenter()
    }

    @Provides
    fun provideSellPresenter(): SellContract.Presenter {
        return SellPresenter()
    }

    @Provides
    fun provideDetailsPresenter(): DetailsContract.Presenter {
        return DetailsPresenter()
    }
}