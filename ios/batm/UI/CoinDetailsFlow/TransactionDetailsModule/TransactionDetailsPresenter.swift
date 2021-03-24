import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class TransactionDetailsPresenter: ModulePresenter, TransactionDetailsModule {
    struct Input {}
    
    weak var delegate: TransactionDetailsModuleDelegate?
    
    private(set) var details: TransactionDetails?
    
    func setup(with details: TransactionDetails) {
        self.details = details
    }
    
    func bind(input: Input) {}
}
