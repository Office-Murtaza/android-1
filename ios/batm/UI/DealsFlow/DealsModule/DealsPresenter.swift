import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

class DealsPresenter: ModulePresenter, DealsModule {
    typealias Store = ViewStore<DealsAction, DealsState>
    struct Input {
        var select: Driver<IndexPath>
    }
    
    var state: Driver<DealsState> {
        return store.state
    }
    weak var delegate: DealsModuleDelegate?
    let types = DealsCellType.allCases
    private let usecase: DealsUsecase
    private let store: Store
    
    init(usecase: DealsUsecase, store: Store = DealsStore()) {
        self.usecase = usecase
        self.store = store
    }
    
    func bind(input: Input) {
        input.select
            .asObservable()
            .map { [types] in types[$0.item] }
            .filter { $0 == .staking }
            .flatMap { [unowned self] _ in
                return self.track(Observable.combineLatest(self.usecase.getStakeDetails(for: .catm).asObservable(),
                                                           self.usecase.getCoinDetails(for: .catm).asObservable(),
                                                           self.usecase.getCoin(for: .catm).asObservable(),
                                                           self.usecase.getCoinsBalance(by: .catm).asObservable()))
            }.withLatestFrom(state) { ($1, $0.0, $0.1, $0.2, $0.3) }
            .subscribe(onNext: { [delegate] (dealsState, stakeDetails, coinDetails, coin, coinBalances) in
                delegate?.didSelectStaking(coin: coin,
                                           coinBalances: coinBalances.coins,
                                           coinDetails: coinDetails,
                                           stakeDetails: stakeDetails)
            })
            .disposed(by: disposeBag)
        
        input.select
            .asObservable()
            .map { [types] in types[$0.item] }
            .filter { $0 == .swap }
            .subscribe(onNext: { [delegate] _ in
                delegate?.didSelectSwap()
            })
            .disposed(by: disposeBag)
        
        input.select
            .asObservable()
            .map { [types] in types[$0.item] }
            .filter { $0 == .transfer }
            .subscribe(onNext: { [delegate] _ in
                delegate?.didSelectTransfer()
            })
            .disposed(by: disposeBag)
    }
}
