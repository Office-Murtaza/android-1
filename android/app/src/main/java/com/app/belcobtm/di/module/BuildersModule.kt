package com.app.belcobtm.di.module

import com.app.belcobtm.di.ActivityScope
import com.app.belcobtm.di.FragmentScope
import com.app.belcobtm.ui.auth.create_wallet.CreateWalletActivity
import com.app.belcobtm.presentation.features.authorization.login.LoginActivity
import com.app.belcobtm.ui.auth.pin.PinActivity
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedActivity
import com.app.belcobtm.presentation.features.authorization.recover.wallet.RecoverWalletActivity
import com.app.belcobtm.ui.main.atm.AtmFragment
import com.app.belcobtm.ui.main.coins.balance.BalanceFragment
import com.app.belcobtm.ui.main.coins.details.DetailsActivity
import com.app.belcobtm.ui.main.coins.sell.SellActivity
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftActivity
import com.app.belcobtm.ui.main.coins.transactions.TransactionsActivity
import com.app.belcobtm.ui.main.coins.visibility.VisibilityCoinsActivity
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawActivity
import com.app.belcobtm.ui.main.main_activity.MainActivity
import com.app.belcobtm.ui.main.settings.change_pass.ChangePassActivity
import com.app.belcobtm.ui.main.settings.check_pass.CheckPassActivity
import com.app.belcobtm.ui.main.settings.phone.ShowPhoneActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindCreateWalletActivity(): CreateWalletActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindLoginActivity(): LoginActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindRecoverSeedPhraseActivity(): RecoverSeedActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindRecoverWalletActivity(): RecoverWalletActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindPinActivity(): PinActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindVisibilityCoinsActivity(): VisibilityCoinsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindCheckPassActivity(): CheckPassActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindChangePassActivity(): ChangePassActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindShowPhoneActivity(): ShowPhoneActivity

    @FragmentScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindBalanceFragment(): BalanceFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindAtmFragment(): AtmFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindTransactionsActivity(): TransactionsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindWithdrawActivity(): WithdrawActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindSendGiftActivity(): SendGiftActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindSellActivity(): SellActivity


    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindDetailsActivity(): DetailsActivity


}