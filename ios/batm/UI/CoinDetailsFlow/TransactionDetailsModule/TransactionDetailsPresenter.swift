import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class TransactionDetailsPresenter: ModulePresenter, TransactionDetailsModule {

  struct Input {}

  weak var delegate: TransactionDetailsModuleDelegate?
  
  var details: TransactionDetails!
  var type: CustomCoinType!
  
  func setup(with details: TransactionDetails, for type: CustomCoinType) {
    self.details = details
    self.type = type
  }

  func bind(input: Input) {}
}
