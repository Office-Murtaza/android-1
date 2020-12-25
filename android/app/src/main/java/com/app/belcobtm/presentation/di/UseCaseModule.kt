package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.atm.interactor.GetAtmsUseCase
import com.app.belcobtm.domain.authorization.interactor.*
import com.app.belcobtm.domain.settings.interactor.*
import com.app.belcobtm.domain.tools.interactor.OldSendSmsToDeviceUseCase
import com.app.belcobtm.domain.tools.interactor.OldVerifySmsCodeUseCase
import com.app.belcobtm.domain.tools.interactor.SendSmsToDeviceUseCase
import com.app.belcobtm.domain.transaction.interactor.*
import com.app.belcobtm.domain.transaction.interactor.trade.*
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
    single { GetCoinDetailsMapUseCase(get()) }
    single { SwapUseCase(get()) }
    single { CreateTransactionUseCase(get()) }
    single { SendGiftTransactionCreateUseCase(get()) }
    single { OldSendSmsToDeviceUseCase(get()) }
    single { OldVerifySmsCodeUseCase(get()) }
    single { SendSmsToDeviceUseCase(get()) }
    single { SellPreSubmitUseCase(get()) }
    single { SellGetLimitsUseCase(get()) }
    single { GetLocalCoinListUseCase(get()) }
    single { UpdateCoinUseCase(get()) }
    single { GetListTradeUseCase.Buy(get()) }
    single { GetListTradeUseCase.Sell(get()) }
    single { GetListTradeUseCase.My(get()) }
    single { GetListTradeUseCase.Open(get()) }
    single { TradeBuySellUseCase(get()) }
    single { CreateBuyTradeUseCase(get()) }
    single { CreateSellTradeUseCase(get()) }
    single { GetBalanceUseCase(get()) }
    single { GetChartsUseCase(get()) }
    single { GetTransactionListUseCase(get()) }
    single { UpdateCoinDetailsUseCase(get()) }
    single { WithdrawUseCase(get()) }
    single { GetCoinByCodeUseCase(get()) }
    single { TradeRecallTransactionCompleteUseCase(get()) }
    single { TradeReserveTransactionCompleteUseCase(get()) }
    single { TradeReserveTransactionCreateUseCase(get()) }
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
}