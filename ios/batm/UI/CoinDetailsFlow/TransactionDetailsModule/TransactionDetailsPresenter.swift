import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class TransactionDetailsPresenter: ModulePresenter, TransactionDetailsModule {
    typealias Store = ViewStore<TransactionDetailsAction, TransactionDetailsState>
    struct Input {}
    
    weak var delegate: TransactionDetailsModuleDelegate?
    var state: Driver<TransactionDetailsState> { store.state }
    let didViewLoadRelay = PublishRelay<Void>()
    
    private let usecase: CoinDetailsUsecase
    private let store: Store
    
    init(usecase: CoinDetailsUsecase,
         store: Store = TransactionDetailsStore()) {
        self.usecase = usecase
        self.store = store
    }
    
    func setup(with transactionDetails: TransactionDetails, coinType: CustomCoinType) {
        store.action.accept(.setupTransactionDetails(transactionDetails: transactionDetails, coinType: coinType))
    }
}
