import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class TransactionDetailsPresenter: ModulePresenter, TransactionDetailsModule {
    struct Input {}
    
    weak var delegate: TransactionDetailsModuleDelegate?
    
    private(set) var transactionDetails: (details: TransactionDetails, coinType: CustomCoinType)?
    
    func setup(with details: TransactionDetails, coinType: CustomCoinType) {
        transactionDetails = (details, coinType)
    }
    
    func bind(input: Input) {}
}
