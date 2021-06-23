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
    func startTrades()
    func startOrdersUpdates()
}

class PinCodeUsecaseImpl: PinCodeUsecase {
    let pinCodeStorage: PinCodeStorage
    let refreshService: RefreshCredentialsService
    let tradeService: TradeSocketService
    let mainSocketService: MainSocketService
    let ordersService: OrderSocketService
  
    init(pinCodeStorage: PinCodeStorage,
         refreshService: RefreshCredentialsService,
         tradeService: TradeSocketService,
         mainSocketService: MainSocketService,
         ordersService: OrderSocketService) {
        self.pinCodeStorage = pinCodeStorage
        self.refreshService = refreshService
        self.tradeService = tradeService
        self.mainSocketService = mainSocketService
        self.ordersService = ordersService
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

    func startOrdersUpdates() {
        ordersService.start()
    }
}
