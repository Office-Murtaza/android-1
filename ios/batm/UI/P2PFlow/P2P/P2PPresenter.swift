import Foundation
import RxSwift
import RxCocoa
import CoreLocation

class P2PPresenter: ModulePresenter, P2PModule {
    weak var delegate: P2PModuleDelegate?
    var currentLocation = BehaviorRelay<CLLocation?>(value: nil)
    var trades = BehaviorRelay<Trades?>(value: nil)
    
    private let locationService = GeolocationService()
    
    func setup(trades: Trades) {
        self.trades.accept(trades)
    }
    
    func checkLocation() {
        locationService.requestLocation { [weak self] (result) in
            self?.currentLocation.accept(result)
        }
    }
}
