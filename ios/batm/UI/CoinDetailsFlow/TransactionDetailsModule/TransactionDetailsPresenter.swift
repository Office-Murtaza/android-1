import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class TransactionDetailsPresenter: ModulePresenter, TransactionDetailsModule {

  struct Input {
    var back: Driver<Void>
    var openTxIdLink: Driver<Void>
    var openRefTxIdLink: Driver<Void>
  }

  weak var delegate: TransactionDetailsModuleDelegate?
  
  var details: TransactionDetails!
  var type: CustomCoinType!
  
  func setup(with details: TransactionDetails, for type: CustomCoinType) {
    self.details = details
    self.type = type
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishTransactionDetails() })
      .disposed(by: disposeBag)
    
    input.openTxIdLink
      .map { [unowned self] in self.details.link }
      .filterNil()
      .map { URL(string: $0) }
      .filterNil()
      .drive(onNext: { UIApplication.shared.open($0) })
      .disposed(by: disposeBag)
    
    input.openRefTxIdLink
      .map { [unowned self] in self.details.refLink }
      .filterNil()
      .map { URL(string: $0) }
      .filterNil()
      .drive(onNext: { UIApplication.shared.open($0) })
      .disposed(by: disposeBag)
  }
}
