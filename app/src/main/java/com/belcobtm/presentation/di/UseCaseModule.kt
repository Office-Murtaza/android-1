package com.belcobtm.presentation.di

import com.belcobtm.data.cloud.storage.FirebaseCloudStorage
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage.Companion.VERIFICATION_STORAGE
import com.belcobtm.data.core.RandomStringGenerator
import com.belcobtm.domain.account.interactor.GetUserCoinListUseCase
import com.belcobtm.domain.account.interactor.UpdateUserCoinListUseCase
import com.belcobtm.domain.atm.interactor.GetAtmsUseCase
import com.belcobtm.domain.authorization.interactor.*
import com.belcobtm.domain.contacts.GetContactsUseCase
import com.belcobtm.domain.settings.interactor.*
import com.belcobtm.domain.socket.ConnectToSocketUseCase
import com.belcobtm.domain.socket.DisconnectFromSocketUseCase
import com.belcobtm.domain.tools.interactor.OldSendSmsToDeviceUseCase
import com.belcobtm.domain.tools.interactor.OldVerifySmsCodeUseCase
import com.belcobtm.domain.tools.interactor.SendSmsToDeviceUseCase
import com.belcobtm.domain.trade.ClearCacheUseCase
import com.belcobtm.domain.trade.create.CheckTradeCreationAvailabilityUseCase
import com.belcobtm.domain.trade.create.CreateTradeUseCase
import com.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.belcobtm.domain.trade.create.mapper.PaymentIdToAvailablePaymentOptionMapper
import com.belcobtm.domain.trade.details.CancelTradeUseCase
import com.belcobtm.domain.trade.details.EditTradeUseCase
import com.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.belcobtm.domain.trade.list.*
import com.belcobtm.domain.trade.list.filter.ApplyFilterUseCase
import com.belcobtm.domain.trade.list.filter.LoadFilterDataUseCase
import com.belcobtm.domain.trade.list.filter.ResetFilterUseCase
import com.belcobtm.domain.trade.list.filter.mapper.CoinCodeMapper
import com.belcobtm.domain.trade.list.filter.mapper.TradeFilterItemMapper
import com.belcobtm.domain.trade.list.filter.mapper.TradeFilterMapper
import com.belcobtm.domain.trade.list.mapper.*
import com.belcobtm.domain.trade.order.*
import com.belcobtm.domain.trade.order.mapper.ChatMessageMapper
import com.belcobtm.domain.transaction.interactor.*
import com.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCompleteUseCase
import com.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCreateUseCase
import com.belcobtm.domain.wallet.interactor.ConnectToWalletUseCase
import com.belcobtm.domain.wallet.interactor.GetChartsUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.presentation.core.DateFormat.CHAT_DATE_FORMAT
import com.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter.Companion.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.belcobtm.presentation.core.formatter.MilesFormatter.Companion.MILES_FORMATTER_QUALIFIER
import com.belcobtm.presentation.core.formatter.TradeCountFormatter.Companion.TRADE_COUNT_FORMATTER_QUALIFIER
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.text.SimpleDateFormat

val useCaseModule = module {
    single { AuthorizationStatusGetUseCase(get()) }
    single { ClearAppDataUseCase(get()) }
    single { AuthorizationCheckCredentialsUseCase(get()) }
    single { CreateWalletUseCase(get(), get()) }
    single { AuthorizeUseCase(get()) }
    single { GetAuthorizePinUseCase(get()) }
    single { SaveAuthorizePinUseCase(get()) }
    single { GetVerificationInfoUseCase(get()) }
    single {
        SendVerificationBlankUseCase(
            get(),
            androidApplication(),
            get(),
            get(),
            get(named(VERIFICATION_STORAGE))
        )
    }
    single { GetVerificationCountryListUseCase(get()) }
    single {
        SendVerificationVipUseCase(
            get(),
            androidApplication(),
            get(),
            get(),
            get(named(VERIFICATION_STORAGE))
        )
    }
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
    single { GetChartsUseCase(get()) }
    single { FetchTransactionsUseCase(get()) }
    single { WithdrawUseCase(get()) }
    single { GetCoinByCodeUseCase(get()) }
    single { RecoverWalletUseCase(get(), get()) }
    single { CreateSeedUseCase(get()) }
    single { CheckPassUseCase(get()) }
    single { UnlinkUseCase(get()) }
    single { ChangePassUseCase(get()) }
    single { GetPhoneUseCase(get()) }
    single { UpdatePhoneUseCase(get()) }
    single { GetAtmsUseCase(get()) }
    single { ObserveTransactionDetailsUseCase(get()) }
    single { GetCoinListUseCase(get()) }
    single { VerifyPhoneUseCase(get()) }
    single { StakeDetailsGetUseCase(get()) }
    single { StakeCreateUseCase(get()) }
    single { StakeCancelUseCase(get()) }
    single { StakeWithdrawUseCase(get()) }
    single { ConnectToWalletUseCase(get()) }
    single { CheckXRPAddressActivatedUseCase(get()) }
    single { GetContactsUseCase(get()) }
    single { SaveSeedUseCase(get()) }
    single { BioAuthSupportedByPhoneUseCase(get()) }
    single { BioAuthAllowedByUserUseCase(get()) }
    single { SetBioAuthStateAllowedUseCase(get()) }
    single { TradeRecallTransactionCompleteUseCase(get()) }
    single { FetchTradesUseCase(get(), get()) }
    single { ObserveTradesUseCase(get(), get(), get()) }
    single { ObserveUserTradeStatisticUseCase(get(), get()) }
    single { ObserveOrdersUseCase(get(), get(), get()) }
    single { ObserveMyTradesUseCase(get(), get(), get()) }
    single { GetAvailableTradePaymentOptionsUseCase(get(), get()) }
    single { CreateTradeUseCase(get()) }
    single { CheckTradeCreationAvailabilityUseCase(get(), get()) }
    single { LoadFilterDataUseCase(get()) }
    single { ResetFilterUseCase(get()) }
    single { ApplyFilterUseCase(get(), get()) }
    single { GetTradeDetailsUseCase(get(), get()) }
    single { CancelTradeUseCase(get()) }
    single { CancelOrderUseCase(get()) }
    single { EditTradeUseCase(get()) }
    single { CreateOrderUseCase(get()) }
    single { StartObserveTradeDataUseCase(get()) }
    single { StartObserveOrderDataUseCase(get()) }
    single { StopObserveTradeDataUseCase(get()) }
    single { StopObserveOrderDataUseCase(get()) }
    single { ObserveOrderDetailsUseCase(get(), get(), get()) }
    single { UpdateOrderStatusUseCase(get()) }
    single { ClearCacheUseCase(get()) }
    single { RateOrderUseCase(get()) }
    single {
        SendChatMessageUseCase(
            get(),
            get(),
            get(named(FirebaseCloudStorage.CHAT_STORAGE)),
            get()
        )
    }
    single { ObserveChatMessagesUseCase(get()) }
    single { ConnectToChatUseCase(get()) }
    single { DisconnectFromChatUseCase(get()) }
    single { GetChatHistoryUseCase(get()) }
    single { TradeReserveTransactionCompleteUseCase(get()) }
    single { TradeReserveTransactionCreateUseCase(get()) }
    single { ObserveMissedMessageCountUseCase(get()) }
    single { UpdateLastSeenMessageTimeStampUseCase(get()) }
    single { ObserveTransactionsUseCase(get()) }
    single { ConnectToTransactionsUseCase(get()) }
    single { ConnectToSocketUseCase(get()) }
    single { DisconnectFromSocketUseCase(get()) }
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
            get(), get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(TRADE_COUNT_FORMATTER_QUALIFIER)), get(), get(),
            get(named(MILES_FORMATTER_QUALIFIER))
        )
    }
    factory { TradesDataToOrderListMapper(get()) }
    factory { TradesDataToMyTradeMapper(get()) }
    factory { TradeFilterItemMapper(get(), get()) }
    factory { PaymentIdToAvailablePaymentOptionMapper(get()) }
    factory { TradeFilterMapper() }
    factory { RandomStringGenerator() }
    factory {
        ChatMessageMapper(
            get(),
            get(named(FirebaseCloudStorage.CHAT_STORAGE)),
            get(),
            SimpleDateFormat(CHAT_DATE_FORMAT)
        )
    }
}