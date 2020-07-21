package com.app.belcobtm.di.module

import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.data_manager.SettingsDataManager
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import dagger.Module
import dagger.Provides

@Module
class PresenterModule {

    @Provides
    fun provideCoinsDataManager(): CoinsDataManager {
        return CoinsDataManager()
    }

    @Provides
    fun provideSettingsDataManager(): SettingsDataManager {
        return SettingsDataManager()
    }

    @Provides
    fun provideTempUtxoDataManager(): WithdrawDataManager {
        return WithdrawDataManager()
    }

}