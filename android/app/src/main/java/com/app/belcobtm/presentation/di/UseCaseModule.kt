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
import com.app.belcobtm.domain.trade.ClearCacheUseCase
import com.app.belcobtm.domain.trade.create.CheckTradeCreationAvailabilityUseCase
import com.app.belcobtm.domain.trade.create.CreateTradeUseCase
import com.app.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.app.belcobtm.domain.trade.create.mapper.PaymentIdToAvailablePaymentOptionMapper
import com.app.belcobtm.domain.trade.details.CancelTradeUseCase
import com.app.belcobtm.domain.trade.details.EditTradeUseCase
import com.app.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.app.belcobtm.domain.trade.list.*
import com.app.belcobtm.domain.trade.list.filter.ApplyFilterUseCase
import com.app.belcobtm.domain.trade.list.filter.LoadFilterDataUseCase
import com.app.belcobtm.domain.trade.list.filter.ResetFilterUseCase
import com.app.belcobtm.domain.trade.list.filter.mapper.CoinCodeMapper
import com.app.belcobtm.domain.trade.list.filter.mapper.TradeFilterItemMapper
import com.app.belcobtm.domain.trade.list.filter.mapper.TradeFilterMapper
import com.app.belcobtm.domain.trade.list.mapper.*
import com.app.belcobtm.domain.trade.order.CreateOrderUseCase
import com.app.belcobtm.domain.trade.order.ObserveOrderDetailsUseCase
import com.app.belcobtm.domain.trade.order.RateOrderUseCase
import com.app.belcobtm.domain.trade.order.UpdateOrderStatusUseCase
import com.app.belcobtm.domain.transaction.interactor.*
import com.app.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.app.belcobtm.domain.wallet.interactor.*
import com.app.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter.Companion.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.app.belcobtm.presentation.core.formatter.MilesFormatter.Companion.MILES_FORMATTER_QUALIFIER
import com.app.belcobtm.presentation.core.formatter.TradeCountFormatter.Companion.TRADE_COUNT_FORMATTER_QUALIFIER
import org.koin.core.qualifier.named
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
    single { TradeRecallTransactionCompleteUseCase(get()) }
    single { FetchTradesUseCase(get()) }
    single { ObserveTradesUseCase(get(), get(), get()) }
    single { ObserveUserTradeStatisticUseCase(get(), get()) }
    single { ObserveOrdersUseCase(get(), get(), get()) }
    single { ObserveMyTradesUseCase(get(), get(), get()) }
    single { GetAvailableTradePaymentOptionsUseCase(get(), get()) }
    single { CreateTradeUseCase(get()) }
    single { CheckTradeCreationAvailabilityUseCase(get(), get()) }
    single { LoadFilterDataUseCase(get(), get(), get()) }
    single { ResetFilterUseCase(get()) }
    single { ApplyFilterUseCase(get(), get()) }
    single { GetTradeDetailsUseCase(get(), get()) }
    single { CancelTradeUseCase(get()) }
    single { EditTradeUseCase(get()) }
    single { CreateOrderUseCase(get()) }
    single { StartObserveTradeDataUseCase(get(), get()) }
    single { StopObserveTradeDataUseCase(get(), get()) }
    single { ObserveOrderDetailsUseCase(get(), get(), get()) }
    single { UpdateOrderStatusUseCase(get()) }
    single { ClearCacheUseCase(get()) }
    single { RateOrderUseCase(get()) }
    factory { TradePaymentOptionMapper() }
    factory { CoinCodeMapper() }
    factory { TradesDataToTradeListMapper(get()) }
    factory {
        TradeToTradeItemMapper(
            get(), get(named(MILES_FORMATTER_QUALIFIER)),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(TRADE_COUNT_FORMATTER_QUALIFIER)),
            get()
        )
    }
    factory { TraderStatusToIconMapper() }
    factory { TradesDataToStatisticsMapper(get()) }
    factory {
        TradeOrderDataToItemMapper(
            get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(TRADE_COUNT_FORMATTER_QUALIFIER)),
            get()
        )
    }
    factory { TradesDataToOrderListMapper(get()) }
    factory { TradesDataToMyTradeMapper(get()) }
    factory { TradeFilterItemMapper(get(), get()) }
    factory { PaymentIdToAvailablePaymentOptionMapper(get()) }
    factory { TradeFilterMapper() }
}