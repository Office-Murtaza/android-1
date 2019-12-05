import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class TransactionDetailsPresenter: ModulePresenter, TransactionDetailsModule {

  struct Input {
    var back: Driver<Void>
    var openLink: Driver<Void>
  }

  weak var delegate: TransactionDetailsModuleDelegate?
  
  var details: TransactionDetails!
  var type: CoinType!
  
  func setup(with details: TransactionDetails, for type: CoinType) {
    self.details = details
    self.type = type
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishTransactionDetails() })
      .disposed(by: disposeBag)
    
    input.openLink
      .map { [unowned self] in URL(string: self.details.link) }
      .filterNil()
      .drive(onNext: { UIApplication.shared.open($0) })
      .disposed(by: disposeBag)
  }
}
