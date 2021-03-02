package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.account.interactor.GetUserCoinListUseCase
import com.app.belcobtm.domain.account.interactor.UpdateUserCoinListUseCase
import com.app.belcobtm.domain.atm.interactor.GetAtmsUseCase
import com.app.belcobtm.domain.authorization.interactor.*
import com.app.belcobtm.domain.contacts.GetContactsUseCase
import com.app.belcobtm.domain.settings.interactor.*
import com.app.belcobtm.domain.tools.interactor.OldSendSmsToDeviceUseCase
import com.app.belcobtm.domain.tools.interactor.OldVerifySmsCodeUseCase
import com.app.belcobtm.domain.tools.interactor.SendSmsToDeviceUseCase
import com.app.belcobtm.domain.trade.list.FetchTradesUseCase
import com.app.belcobtm.domain.trade.list.ObserveTradesUseCase
import com.app.belcobtm.domain.trade.list.ObserveUserTradeStatisticUseCase
import com.app.belcobtm.domain.trade.list.mapper.TradesDataToStatisticsMapper
import com.app.belcobtm.domain.trade.list.mapper.TradesDataToTradeListMapper
import com.app.belcobtm.domain.transaction.interactor.*
import com.app.belcobtm.domain.wallet.interactor.*
import org.koin.dsl.module

val useCaseModule = module {
    single { AuthorizationStatusGetUseCase(get()) }
    single { ClearAppDataUseCase(get()) }
    single { AuthorizationCheckCredentialsUseCase(get()) }
    single { CreateWalletUseCase(get(), get()) }
    single { AuthorizeUseCase(get()) }
    single { GetAuthorizePinUseCase(get()) }
    single { SaveAuthorizePinUseCase(get()) }
    single { GetVerificationInfoUseCase(get()) }
    single { SendVerificationBlankUseCase(get()) }
    single { GetVerificationCountryListUseCase(get()) }
    single { SendVerificationVipUseCase(get()) }
    single { SwapUseCase(get()) }
    single { CreateTransactionUseCase(get()) }
    single { SendGiftTransactionCreateUseCase(get()) }
    single { OldSendSmsToDeviceUseCase(get()) }
    single { OldVerifySmsCodeUseCase(get()) }
    single { SendSmsToDeviceUseCase(get()) }
    single { SellPreSubmitUseCase(get()) }
    single { SellGetLimitsUseCase(get()) }
    single { GetUserCoinListUseCase(get()) }
    single { UpdateUserCoinListUseCase(get()) }
    single { GetBalanceUseCase(get()) }
    single { GetChartsUseCase(get()) }
    single { GetTransactionListUseCase(get()) }
    single { UpdateCoinDetailsUseCase(get()) }
    single { WithdrawUseCase(get()) }
    single { GetCoinByCodeUseCase(get()) }
    single { GetFreshCoinUseCase(get()) }
    single { RecoverWalletUseCase(get(), get()) }
    single { CreateSeedUseCase(get()) }
    single { CheckPassUseCase(get()) }
    single { UnlinkUseCase(get(), get()) }
    single { ChangePassUseCase(get()) }
    single { GetPhoneUseCase(get()) }
    single { UpdatePhoneUseCase(get()) }
    single { GetAtmsUseCase(get()) }
    single { GetTransactionDetailsUseCase(get()) }
    single { GetCoinListUseCase(get()) }
    single { VerifyPhoneUseCase(get()) }
    single { StakeDetailsGetUseCase(get()) }
    single { StakeCreateUseCase(get()) }
    single { StakeCancelUseCase(get()) }
    single { StakeWithdrawUseCase(get()) }
    single { ObserveBalanceUseCase(get()) }
    single { ConnectToWalletUseCase(get()) }
    single { GetCoinDetailsUseCase(get()) }
    single { CheckXRPAddressActivatedUseCase(get()) }
    single { GetFreshCoinsUseCase(get()) }
    single { GetContactsUseCase(get()) }
    single { SaveSeedUseCase(get()) }
    single { BioAuthSupportedByPhoneUseCase(get()) }
    single { BioAuthAllowedByUserUseCase(get()) }
    single { SetBioAuthStateAllowedUseCase(get()) }
    single { FetchTradesUseCase(get()) }
    single { ObserveTradesUseCase(get(), get()) }
    single { ObserveUserTradeStatisticUseCase(get(), get()) }
    factory { TradesDataToTradeListMapper() }
    factory { TradesDataToStatisticsMapper() }
}