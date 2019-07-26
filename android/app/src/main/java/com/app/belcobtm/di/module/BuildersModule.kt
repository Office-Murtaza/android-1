package com.app.belcobtm.di.module

import com.app.belcobtm.di.ActivityScope
import com.app.belcobtm.di.FragmentScope
import com.app.belcobtm.ui.auth.create_wallet.CreateWalletActivity
import com.app.belcobtm.ui.auth.login.LoginActivity
import com.app.belcobtm.ui.auth.pin.PinActivity
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedActivity
import com.app.belcobtm.ui.auth.recover_wallet.RecoverWalletActivity
import com.app.belcobtm.ui.coins.atm.AtmFragment
import com.app.belcobtm.ui.coins.balance.BalanceFragment
import com.app.belcobtm.ui.coins.main.MainActivity
import com.app.belcobtm.ui.coins.visibility.VisibilityCoinsActivity
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


    @FragmentScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindBalanceFragment(): BalanceFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindAtmFragment(): AtmFragment
}