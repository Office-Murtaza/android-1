package com.app.belcobtm.di.module

import com.app.belcobtm.di.ActivityScope
import com.app.belcobtm.presentation.features.authorization.create.wallet.CreateWalletFragment
import com.app.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedFragment
import com.app.belcobtm.presentation.features.wallet.send.gift.SendGiftFragment
import com.app.belcobtm.presentation.features.wallet.withdraw.WithdrawFragment
import com.app.belcobtm.ui.main.coins.sell.SellActivity
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
    internal abstract fun bindWithdrawActivity(): WithdrawFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindSendGiftActivity(): SendGiftFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ActivityModule::class])
    internal abstract fun bindSellActivity(): SellActivity

}