package com.belcobtm.presentation.di

import com.belcobtm.data.cloud.storage.FirebaseCloudStorage
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage.Companion.VERIFICATION_STORAGE
import com.belcobtm.data.core.RandomStringGenerator
import com.belcobtm.domain.account.interactor.GetUserCoinListUseCase
import com.belcobtm.domain.account.interactor.UpdateUserCoinListUseCase
import com.belcobtm.domain.atm.interactor.GetAtmsUseCase
import com.belcobtm.domain.authorization.interactor.AuthorizationCheckCredentialsUseCase
import com.belcobtm.domain.authorization.interactor.AuthorizationStatusGetUseCase
import com.belcobtm.domain.authorization.interactor.AuthorizeUseCase
import com.belcobtm.domain.authorization.interactor.CheckPassUseCase
import com.belcobtm.domain.authorization.interactor.ClearAppDataUseCase
import com.belcobtm.domain.authorization.interactor.CreateSeedUseCase
import com.belcobtm.domain.authorization.interactor.CreateWalletUseCase
import com.belcobtm.domain.authorization.interactor.GetAuthorizePinUseCase
import com.belcobtm.domain.authorization.interactor.GetVerificationStatusUseCase
import com.belcobtm.domain.authorization.interactor.RecoverWalletUseCase
import com.belcobtm.domain.authorization.interactor.SaveAuthorizePinUseCase
import com.belcobtm.domain.authorization.interactor.SaveSeedUseCase
import com.belcobtm.domain.authorization.interactor.SaveUserAuthedUseCase
import com.belcobtm.domain.bank_account.interactor.ConnectToBankAccountsUseCase
import com.belcobtm.domain.bank_account.interactor.ConnectToPaymentsUseCase
import com.belcobtm.domain.bank_account.interactor.CreateBankAccountPaymentUseCase
import com.belcobtm.domain.bank_account.interactor.CreateBankAccountUseCase
import com.belcobtm.domain.bank_account.interactor.DisconnectFromBankAccountsUseCase
import com.belcobtm.domain.bank_account.interactor.DisconnectFromPaymentsUseCase
import com.belcobtm.domain.bank_account.interactor.GetBankAccountPaymentsUseCase
import com.belcobtm.domain.bank_account.interactor.GetBankAccountsListUseCase
import com.belcobtm.domain.bank_account.interactor.GetLinkTokenUseCase
import com.belcobtm.domain.bank_account.interactor.LinkBankAccountUseCase
import com.belcobtm.domain.bank_account.interactor.ObserveBankAccountDetailsUseCase
import com.belcobtm.domain.bank_account.interactor.ObserveBankAccountsListUseCase
import com.belcobtm.domain.bank_account.interactor.ObservePaymentsUseCase
import com.belcobtm.domain.contacts.GetContactsUseCase
import com.belcobtm.domain.referral.CreateRecipientsUseCase
import com.belcobtm.domain.referral.GetExistedPhoneNumbersUseCase
import com.belcobtm.domain.referral.LoadReferralUseCase
import com.belcobtm.domain.referral.SearchAvailableContactsUseCase
import com.belcobtm.domain.service.ConnectToServicesUseCase
import com.belcobtm.domain.service.DisconnectFromServicesUseCase
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.settings.interactor.BioAuthAllowedByUserUseCase
import com.belcobtm.domain.settings.interactor.BioAuthSupportedByPhoneUseCase
import com.belcobtm.domain.settings.interactor.ChangePassUseCase
import com.belcobtm.domain.settings.interactor.GetNeedToShowRestrictions
import com.belcobtm.domain.settings.interactor.GetPhoneUseCase
import com.belcobtm.domain.settings.interactor.GetVerificationCountryListUseCase
import com.belcobtm.domain.settings.interactor.GetVerificationDetailsUseCase
import com.belcobtm.domain.settings.interactor.GetVerificationFieldsUseCase
import com.belcobtm.domain.settings.interactor.GetVerificationInfoUseCase
import com.belcobtm.domain.settings.interactor.SendVerificationBlankUseCase
import com.belcobtm.domain.settings.interactor.SendVerificationDocumentUseCase
import com.belcobtm.domain.settings.interactor.SendVerificationIdentityUseCase
import com.belcobtm.domain.settings.interactor.SendVerificationVipUseCase
import com.belcobtm.domain.settings.interactor.SetBioAuthStateAllowedUseCase
import com.belcobtm.domain.settings.interactor.SetNeedToShowRestrictionsUseCase
import com.belcobtm.domain.settings.interactor.UnlinkUseCase
import com.belcobtm.domain.settings.interactor.UpdatePhoneUseCase
import com.belcobtm.domain.settings.interactor.VerifyPhoneUseCase
import com.belcobtm.domain.socket.ConnectToSocketUseCase
import com.belcobtm.domain.socket.DisconnectFromSocketUseCase
import com.belcobtm.domain.support.SupportChatInteractor
import com.belcobtm.domain.tools.interactor.OldSendSmsToDeviceUseCase
import com.belcobtm.domain.tools.interactor.OldVerifySmsCodeUseCase
import com.belcobtm.domain.tools.interactor.SendSmsToDeviceUseCase
import com.belcobtm.domain.tools.interactor.VerifySmsCodeUseCase
import com.belcobtm.domain.trade.ClearCacheUseCase
import com.belcobtm.domain.trade.create.CheckTradeCreationAvailabilityUseCase
import com.belcobtm.domain.trade.create.CreateTradeUseCase
import com.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.belcobtm.domain.trade.create.mapper.PaymentIdToAvailablePaymentOptionMapper
import com.belcobtm.domain.trade.details.CancelTradeUseCase
import com.belcobtm.domain.trade.details.DeleteTradeUseCase
import com.belcobtm.domain.trade.details.EditTradeUseCase
import com.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.belcobtm.domain.trade.details.ObserveTradeDetailsUseCase
import com.belcobtm.domain.trade.list.FetchTradesUseCase
import com.belcobtm.domain.trade.list.ObserveMyTradesUseCase
import com.belcobtm.domain.trade.list.ObserveOrdersUseCase
import com.belcobtm.domain.trade.list.ObserveTradesUseCase
import com.belcobtm.domain.trade.list.ObserveUserTradeStatisticUseCase
import com.belcobtm.domain.trade.list.StartObserveOrderDataUseCase
import com.belcobtm.domain.trade.list.StartObserveTradeDataUseCase
import com.belcobtm.domain.trade.list.StopObserveOrderDataUseCase
import com.belcobtm.domain.trade.list.StopObserveTradeDataUseCase
import com.belcobtm.domain.trade.list.filter.ApplyFilterUseCase
import com.belcobtm.domain.trade.list.filter.LoadFilterDataUseCase
import com.belcobtm.domain.trade.list.filter.ResetFilterUseCase
import com.belcobtm.domain.trade.list.filter.mapper.CoinCodeMapper
import com.belcobtm.domain.trade.list.filter.mapper.TradeFilterItemMapper
import com.belcobtm.domain.trade.list.filter.mapper.TradeFilterMapper
import com.belcobtm.domain.trade.list.mapper.TradeOrderDataToItemMapper
import com.belcobtm.domain.trade.list.mapper.TradePaymentOptionMapper
import com.belcobtm.domain.trade.list.mapper.TradeToTradeItemMapper
import com.belcobtm.domain.trade.list.mapper.TraderStatusToIconMapper
import com.belcobtm.domain.trade.list.mapper.TradesDataToMyTradeMapper
import com.belcobtm.domain.trade.list.mapper.TradesDataToOrderListMapper
import com.belcobtm.domain.trade.list.mapper.TradesDataToStatisticsMapper
import com.belcobtm.domain.trade.list.mapper.TradesDataToTradeListMapper
import com.belcobtm.domain.trade.order.CancelOrderUseCase
import com.belcobtm.domain.trade.order.ConnectToChatUseCase
import com.belcobtm.domain.trade.order.CreateOrderUseCase
import com.belcobtm.domain.trade.order.DisconnectFromChatUseCase
import com.belcobtm.domain.trade.order.GetChatHistoryUseCase
import com.belcobtm.domain.trade.order.ObserveChatMessagesUseCase
import com.belcobtm.domain.trade.order.ObserveMissedMessageCountUseCase
import com.belcobtm.domain.trade.order.ObserveOrderDetailsUseCase
import com.belcobtm.domain.trade.order.RateOrderUseCase
import com.belcobtm.domain.trade.order.SendChatMessageUseCase
import com.belcobtm.domain.trade.order.UpdateLastSeenMessageTimeStampUseCase
import com.belcobtm.domain.trade.order.UpdateOrderStatusUseCase
import com.belcobtm.domain.trade.order.mapper.ChatMessageMapper
import com.belcobtm.domain.transaction.interactor.ConnectToTransactionsUseCase
import com.belcobtm.domain.transaction.interactor.CreateTransactionToAddressUseCase
import com.belcobtm.domain.transaction.interactor.CreateTransactionUseCase
import com.belcobtm.domain.transaction.interactor.DisconnectFromTransactionsUseCase
import com.belcobtm.domain.transaction.interactor.FetchTransactionsUseCase
import com.belcobtm.domain.transaction.interactor.GetFakeSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetMaxValueBySignedTransactionUseCase
import com.belcobtm.domain.transaction.interactor.GetSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetTransferAddressUseCase
import com.belcobtm.domain.transaction.interactor.ObserveTransactionDetailsUseCase
import com.belcobtm.domain.transaction.interactor.ObserveTransactionsUseCase
import com.belcobtm.domain.transaction.interactor.ReceiverAccountActivatedUseCase
import com.belcobtm.domain.transaction.interactor.SellPreSubmitUseCase
import com.belcobtm.domain.transaction.interactor.SellUseCase
import com.belcobtm.domain.transaction.interactor.SendGiftTransactionCreateUseCase
import com.belcobtm.domain.transaction.interactor.StakeCancelUseCase
import com.belcobtm.domain.transaction.interactor.StakeCreateUseCase
import com.belcobtm.domain.transaction.interactor.StakeDetailsGetUseCase
import com.belcobtm.domain.transaction.interactor.StakeWithdrawUseCase
import com.belcobtm.domain.transaction.interactor.SwapUseCase
import com.belcobtm.domain.transaction.interactor.WithdrawUseCase
import com.belcobtm.domain.transaction.interactor.trade.TradeRecallTransactionCompleteUseCase
import com.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCompleteUseCase
import com.belcobtm.domain.transaction.interactor.trade.TradeReserveTransactionCreateUseCase
import com.belcobtm.domain.wallet.interactor.ConnectToWalletUseCase
import com.belcobtm.domain.wallet.interactor.DisconnectFromWalletUseCase
import com.belcobtm.domain.wallet.interactor.GetChartsUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.interactor.UpdateBalanceUseCase
import com.belcobtm.domain.wallet.interactor.UpdateReservedBalanceUseCase
import com.belcobtm.presentation.core.DateFormat.CHAT_DATE_FORMAT
import com.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter.Companion.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.belcobtm.presentation.core.formatter.MilesFormatter.Companion.MILES_FORMATTER_QUALIFIER
import com.belcobtm.presentation.core.formatter.TradeCountFormatter.Companion.TRADE_COUNT_FORMATTER_QUALIFIER
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.text.SimpleDateFormat
import java.util.Calendar

val useCaseModule = module {
    single { AuthorizationStatusGetUseCase(get()) }
    single { ClearAppDataUseCase(get()) }
    single { AuthorizationCheckCredentialsUseCase(get()) }
    single { CreateWalletUseCase(get(), get()) }
    single { AuthorizeUseCase(get()) }
    single { GetAuthorizePinUseCase(get()) }
    single { SaveAuthorizePinUseCase(get()) }
    single { GetBankAccountsListUseCase(get()) }
    single { GetBankAccountPaymentsUseCase(get()) }
    single { ObserveBankAccountDetailsUseCase(get()) }
    single { ObserveBankAccountsListUseCase(get()) }
    single { ObservePaymentsUseCase(get()) }
    single { GetLinkTokenUseCase(get()) }
    single { CreateBankAccountUseCase(get()) }
    single { CreateBankAccountPaymentUseCase(get()) }
    single { LinkBankAccountUseCase(get()) }
    single { GetVerificationInfoUseCase(get()) }
    single { GetVerificationDetailsUseCase(get()) }
    single { GetVerificationFieldsUseCase(get()) }
    single { GetVerificationCountryListUseCase(get()) }
    single { SendVerificationIdentityUseCase(get()) }
    single {
        SendVerificationDocumentUseCase(
            get(),
            androidApplication(),
            get(named(VERIFICATION_STORAGE)),
            get()
        )
    }
    single { SaveUserAuthedUseCase(get()) }
    single {
        SendVerificationBlankUseCase(
            get(),
            androidApplication(),
            get(named(VERIFICATION_STORAGE))
        )
    }
    single {
        SendVerificationVipUseCase(
            get(),
            androidApplication(),
            get(named(VERIFICATION_STORAGE))
        )
    }
    single { SwapUseCase(get(), get(), get()) }
    single { CreateTransactionUseCase(get()) }
    single { CreateTransactionToAddressUseCase(get()) }
    single { SendGiftTransactionCreateUseCase(get(), get(), get()) }
    single { OldSendSmsToDeviceUseCase(get()) }
    single { OldVerifySmsCodeUseCase(get()) }
    single { SendSmsToDeviceUseCase(get()) }
    single { VerifySmsCodeUseCase(get()) }
    single { SellPreSubmitUseCase(get()) }
    single { SellUseCase(get()) }
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
    single { ServiceInfoProvider(get()) }
    single {
        GetAtmsUseCase(
            get(), mapOf(
                Calendar.SUNDAY to "Sunday",
                Calendar.MONDAY to "Monday",
                Calendar.TUESDAY to "Tuesday",
                Calendar.WEDNESDAY to "Wednesday",
                Calendar.THURSDAY to "Thursday",
                Calendar.FRIDAY to "Friday",
                Calendar.SATURDAY to "Saturday"
            ),
            get(), get(named(MILES_FORMATTER_QUALIFIER)), get()
        )
    }
    single { ObserveTransactionDetailsUseCase(get()) }
    single { GetCoinListUseCase(get()) }
    single { VerifyPhoneUseCase(get()) }
    single { StakeDetailsGetUseCase(get()) }
    single { StakeCreateUseCase(get(), get(), get()) }
    single { StakeCancelUseCase(get(), get(), get()) }
    single { StakeWithdrawUseCase(get(), get(), get()) }
    single { ConnectToWalletUseCase(get()) }
    single { DisconnectFromWalletUseCase(get()) }
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
    single { CreateTradeUseCase(get(), get(), get()) }
    single { CheckTradeCreationAvailabilityUseCase(get(), get()) }
    single { LoadFilterDataUseCase(get()) }
    single { ResetFilterUseCase(get()) }
    single { ApplyFilterUseCase(get(), get()) }
    single { GetTradeDetailsUseCase(get(), get()) }
    single { ObserveTradeDetailsUseCase(get(), get()) }
    single { DeleteTradeUseCase(get()) }
    single { CancelTradeUseCase(get()) }
    single { CancelOrderUseCase(get()) }
    single { EditTradeUseCase(get()) }
    single { CreateOrderUseCase(get(), get(), get()) }
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
            get(), get(),
            get(named(FirebaseCloudStorage.CHAT_STORAGE)),
            get(), get()
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
    single { DisconnectFromTransactionsUseCase(get()) }
    single { ConnectToBankAccountsUseCase(get()) }
    single { DisconnectFromBankAccountsUseCase(get()) }
    single { ConnectToPaymentsUseCase(get()) }
    single { DisconnectFromPaymentsUseCase(get()) }
    single { ConnectToSocketUseCase(get()) }
    single { DisconnectFromSocketUseCase(get()) }
    single { LoadReferralUseCase(get()) }
    single { GetExistedPhoneNumbersUseCase(get(), get()) }
    single { SearchAvailableContactsUseCase(get()) }
    single { CreateRecipientsUseCase() }
    single { GetTransactionPlanUseCase(get()) }
    single { GetSignedTransactionPlanUseCase(get()) }
    single { GetFakeSignedTransactionPlanUseCase(get(), get()) }
    single { GetMaxValueBySignedTransactionUseCase(get()) }
    single { GetTransferAddressUseCase(get()) }
    single { ReceiverAccountActivatedUseCase(get()) }
    single { ConnectToServicesUseCase(get()) }
    single { DisconnectFromServicesUseCase(get()) }
    factory { TradePaymentOptionMapper() }
    factory { GetVerificationStatusUseCase(get()) }
    factory { CoinCodeMapper() }
    factory { TradesDataToTradeListMapper(get()) }
    factory { UpdateBalanceUseCase(get()) }
    factory { UpdateReservedBalanceUseCase(get()) }
    factory { GetNeedToShowRestrictions(get()) }
    factory { SetNeedToShowRestrictionsUseCase(get()) }
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
    factory {
        SupportChatInteractor(
            supportChatHelper = get()
        )
    }

}
