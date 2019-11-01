import Foundation
import RxSwift
import RxCocoa

final class CoinSellDetailsAnotherAddressPresenter: ModulePresenter, CoinSellDetailsAnotherAddressModule {

  struct Input {
    var back: Driver<Void>
    var copy: Driver<Void>
    var done: Driver<Void>
  }

  weak var delegate: CoinSellDetailsAnotherAddressModuleDelegate?
  
  var details: SellDetailsForAnotherAddress!
  
  var amountString: String {
    return "\(details.amount) \(details.coin.type.code)"
  }
  
  func setup(with details: SellDetailsForAnotherAddress) {
    self.details = details
  }

  func bind(input: Input) {
    Driver.merge(input.back, input.done)
      .drive(onNext: { [delegate] in delegate?.didFinishCoinSellDetailsAnotherAddress() })
      .disposed(by: disposeBag)
    input.copy
      .drive(onNext: { [unowned self] in UIPasteboard.general.string = self.details.address })
      .disposed(by: disposeBag)
  }
}
