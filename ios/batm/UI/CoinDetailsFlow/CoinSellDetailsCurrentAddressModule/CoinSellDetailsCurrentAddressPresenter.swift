import Foundation
import RxSwift
import RxCocoa

final class CoinSellDetailsCurrentAddressPresenter: ModulePresenter, CoinSellDetailsCurrentAddressModule {

  struct Input {
    var back: Driver<Void>
    var done: Driver<Void>
  }

  weak var delegate: CoinSellDetailsCurrentAddressModuleDelegate?

  func bind(input: Input) {
    Driver.merge(input.back, input.done)
      .drive(onNext: { [delegate] in delegate?.didFinishCoinSellDetailsCurrentAddress() })
      .disposed(by: disposeBag)
  }
}
