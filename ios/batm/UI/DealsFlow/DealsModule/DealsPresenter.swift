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
    private let balanceService: BalanceService
    private let store: Store
    
    init(usecase: DealsUsecase,
         store: Store = DealsStore(),
         balanceService: BalanceService) {
        self.usecase = usecase
        self.store = store
        self.balanceService = balanceService
    }
    
    func bind(input: Input) {
        input.select
            .asObservable()
            .map { [types] in types[$0.item] }
            .filter { $0 == .staking }
            .subscribe(onNext: { [delegate] _ in
                delegate?.didSelectStaking()
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
        
        input.select
            .asObservable()
            .map { [types] in types[$0.item] }
            .filter { $0 == .p2pTrades }
            .subscribe(onNext: { [delegate] _ in
                delegate?.didSelectedP2p()
            })
            .disposed(by: disposeBag)
    }
}
