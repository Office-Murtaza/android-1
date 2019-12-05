import Foundation
import RxSwift
import RxCocoa

final class CoinSellDetailsCurrentAddressPresenter: ModulePresenter, CoinSellDetailsCurrentAddressModule {

  struct Input {
    var back: Driver<Void>
    var done: Driver<Void>
  }

  weak var delegate: CoinSellDetailsCurrentAddressModuleDelegate?
  
  var details: SellDetailsForCurrentAddress!
  
  var title: String {
    return String(format: localize(L.CoinSellDetails.title), details.coin.type.code)
  }
  
  func setup(with details: SellDetailsForCurrentAddress) {
    self.details = details
  }

  func bind(input: Input) {
    Driver.merge(input.back, input.done)
      .drive(onNext: { [delegate] in delegate?.didFinishCoinSellDetailsCurrentAddress() })
      .disposed(by: disposeBag)
  }
}
