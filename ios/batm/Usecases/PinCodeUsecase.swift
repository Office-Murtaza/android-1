import Foundation
import RxSwift

enum PinCodeError: Error {
    case notMatch
}

protocol PinCodeUsecase {
    func get() -> Single<String>
    func save(pinCode: String) -> Completable
    func verify(pinCode: String) -> Completable
    func refresh() -> Completable
    func startTransactionDetails()
    func startTrades()
}

class PinCodeUsecaseImpl: PinCodeUsecase {
    let pinCodeStorage: PinCodeStorage
    let refreshService: RefreshCredentialsService
    let tradeService: TradeSocketService
    let transactionDetailsService: TransactionDetailsService
    
    init(pinCodeStorage: PinCodeStorage,
         refreshService: RefreshCredentialsService,
         tradeService: TradeSocketService,
         transactionDetailsService: TransactionDetailsService) {
        self.pinCodeStorage = pinCodeStorage
        self.refreshService = refreshService
        self.tradeService = tradeService
        self.transactionDetailsService = transactionDetailsService
    }
    
    func get() -> Single<String> {
        return pinCodeStorage.get()
    }
    
    func save(pinCode: String) -> Completable {
        return pinCodeStorage.save(pinCode: pinCode)
    }
    
    func verify(pinCode: String) -> Completable {
        return pinCodeStorage.get()
            .map { savedPin -> Void in
                if savedPin != pinCode {
                    throw PinCodeError.notMatch
                }
            }
            .toCompletable()
    }
    
    func refresh() -> Completable {
        return refreshService.refresh()
    }
    
    func startTrades() {
        tradeService.start()
    }
    
    func startTransactionDetails() {
        transactionDetailsService.start()
    }
}
