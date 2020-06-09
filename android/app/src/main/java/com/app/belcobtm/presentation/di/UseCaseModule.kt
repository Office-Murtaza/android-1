package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.authorization.interactor.*
import com.app.belcobtm.domain.settings.interactor.GetVerificationCountryListUseCase
import com.app.belcobtm.domain.settings.interactor.GetVerificationInfoUseCase
import com.app.belcobtm.domain.settings.interactor.SendVerificationBlankUseCase
import com.app.belcobtm.domain.settings.interactor.SendVerificationVipUseCase
import com.app.belcobtm.domain.wallet.interactor.*
import com.app.belcobtm.domain.wallet.interactor.trade.CreateBuyTradeUseCase
import com.app.belcobtm.domain.wallet.interactor.trade.CreateSellTradeUseCase
import com.app.belcobtm.domain.wallet.interactor.trade.GetListTradeUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { ClearAppDataUseCase(get()) }
    single { RecoverWalletUseCase(get()) }
    single { RecoverWalletVerifySmsCodeUseCase(get()) }
    single { CreateWalletUseCase(get()) }
    single { CreateWalletVerifySmsCodeUseCase(get()) }
    single { AuthorizeUseCase(get()) }
    single { GetAuthorizePinUseCase(get()) }
    single { SaveAuthorizePinUseCase(get()) }
    single { GetVerificationInfoUseCase(get()) }
    single { SendVerificationBlankUseCase(get()) }
    single { GetVerificationCountryListUseCase(get()) }
    single { SendVerificationVipUseCase(get()) }
    single { GetCoinFeeMapUseCase(get()) }
    single { CoinToCoinExchangeUseCase(get()) }
    single { CreateTransactionUseCase(get()) }
    single { WithdrawUseCase(get()) }
    single { GetGiftAddressUseCase(get()) }
    single { SendGiftUseCase(get()) }
    single { SendSmsToDeviceUseCase(get()) }
    single { VerifySmsCodeUseCase(get()) }
    single { SellPreSubmitUseCase(get()) }
    single { SellGetLimitsUseCase(get()) }
    single { GetCoinListUseCase(get()) }
    single { UpdateCoinUseCase(get()) }
    single { GetListTradeUseCase.Buy(get()) }
    single { GetListTradeUseCase.Sell(get()) }
    single { GetListTradeUseCase.My(get()) }
    single { GetListTradeUseCase.Open(get()) }
    single { TradeBuySellUseCase(get()) }
    single { CreateBuyTradeUseCase(get()) }
    single { CreateSellTradeUseCase(get()) }
}