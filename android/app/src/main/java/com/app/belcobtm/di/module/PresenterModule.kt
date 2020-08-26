package com.app.belcobtm.di.module

import com.app.belcobtm.api.data_manager.WithdrawDataManager
import dagger.Module
import dagger.Provides

@Module
class PresenterModule {
    @Provides
    fun provideTempUtxoDataManager(): WithdrawDataManager {
        return WithdrawDataManager()
    }

}