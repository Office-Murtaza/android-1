package com.app.belcobtm.di.module

import com.app.belcobtm.di.ActivityScope
import com.app.belcobtm.di.FragmentScope
import com.app.belcobtm.presentation.features.authorization.create.wallet.CreateWalletFragment
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedFragment
import com.app.belcobtm.presentation.features.wallet.add.AddWalletActivity
import com.app.belcobtm.presentation.features.wallet.balance.WalletFragment
import com.app.belcobtm.ui.main.atm.AtmFragment
import com.app.belcobtm.ui.main.coins.details.DetailsActivity
import com.app.belcobtm.ui.main.coins.sell.SellActivity
import com.app.belcobtm.ui.main.coins.send_gift.SendGiftActivity
import com.app.belcobtm.ui.main.coins.withdraw.WithdrawActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindCreateWalletActivity(): CreateWalletFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindRecoverSeedPhraseActivity(): RecoverSeedFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindPinActivity(): PinActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindVisibilityCoinsActivity(): AddWalletActivity

    @FragmentScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindBalanceFragment(): WalletFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [FragmentModule::class])
    internal abstract fun bindAtmFragment(): AtmFragment

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