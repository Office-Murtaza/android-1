import Foundation
import RxSwift
import RxCocoa

final class CoinDepositPresenter: ModulePresenter, CoinDepositModule {

  struct Input {
    var copy: Driver<Void>
  }
  
  var coin: BTMCoin!

  weak var delegate: CoinDepositModuleDelegate?
  
  func setup(coin: BTMCoin) {
    self.coin = coin
  }

  func bind(input: Input) {
    input.copy
      .drive(onNext: { [unowned self] in UIPasteboard.general.string = self.coin.address })
      .disposed(by: disposeBag)
  }
}
