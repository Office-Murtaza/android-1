import Foundation
import RxSwift
import RxCocoa
import CoreLocation

class P2PPresenter: ModulePresenter, P2PModule {
    weak var delegate: P2PModuleDelegate?
    var currentLocation = BehaviorRelay<CLLocation?>(value: nil)
    var trades = BehaviorRelay<Trades?>(value: nil)
    var accountStorage: AccountStorage?
    var walletUseCase: WalletUsecase?
    var userId: Int?
    
    var balance = PublishRelay<CoinsBalance>()
    private let locationService = GeolocationService()
    private let fetchDataRelay = PublishRelay<Void>()
    
    
    func setup(trades: Trades, userId: Int) {
        self.userId = userId
        self.trades.accept(trades)
        
        guard let wallet = walletUseCase else {
            return
        }
        
        fetchDataRelay
            .asObservable()
          .flatMap { [unowned self]  in
            return self.track(wallet.getCoinsBalance(filteredByActive: true).asObservable())
          }.subscribe(onNext: { [weak self] result in
            self?.balance.accept(result)
          })
          .disposed(by: disposeBag)
        
        fetchDataRelay.accept(())
        
    }
    
    func checkLocation() {
        locationService.requestLocation { [weak self] (result) in
            self?.currentLocation.accept(result)
        }
    }
    
 
    
}
