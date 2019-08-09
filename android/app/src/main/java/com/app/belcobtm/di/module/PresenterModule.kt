package com.app.belcobtm.di.module

import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.data_manager.SettingsDataManager
import dagger.Module
import dagger.Provides

@Module
class PresenterModule {

    @Provides
    fun provideAuthDataManager(): AuthDataManager {
        return AuthDataManager()
    }

    @Provides
    fun provideCoinsDataManager(): CoinsDataManager {
        return CoinsDataManager()
    }

    @Provides
    fun provideSettingsDataManager(): SettingsDataManager {
        return SettingsDataManager()
    }

}