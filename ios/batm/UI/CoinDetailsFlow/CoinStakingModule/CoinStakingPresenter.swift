import Foundation
import RxSwift
import RxCocoa

final class CoinStakingPresenter: ModulePresenter, CoinStakingModule {
  
  typealias Store = ViewStore<CoinStakingAction, CoinStakingState>

  struct Input {
    var back: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store

  weak var delegate: CoinStakingModuleDelegate?
  
  var state: Driver<CoinStakingState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = CoinStakingStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinSettings: CoinSettings, stakeDetails: StakeDetails) {
    store.action.accept(.setupCoin(coin))
    store.action.accept(.setupCoinBalances(coinBalances))
    store.action.accept(.setupCoinSettings(coinSettings))
    store.action.accept(.setupStakeDetails(stakeDetails))
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishCoinStaking() })
      .disposed(by: disposeBag)
  }
}
