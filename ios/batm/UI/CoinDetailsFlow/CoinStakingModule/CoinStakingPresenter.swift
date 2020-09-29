import Foundation
import RxSwift
import RxCocoa

final class CoinStakingPresenter: ModulePresenter, CoinStakingModule {
  
  typealias Store = ViewStore<CoinStakingAction, CoinStakingState>

  struct Input {
    var updateCoinAmount: Driver<String?>
    var max: Driver<Void>
    var create: Driver<Void>
    var cancel: Driver<Void>
    var withdraw: Driver<Void>
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
    input.updateCoinAmount
      .asObservable()
      .map { CoinStakingAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .withLatestFrom(state)
      .map { $0.maxValue.coinFormatted }
      .map { CoinStakingAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    Driver.merge(input.create, input.cancel, input.withdraw)
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.proceedWithStaking(for: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishCoinStaking() })
      .disposed(by: disposeBag)
  }
  
  private func proceedWithStaking(for state: CoinStakingState) -> Completable {
    let coin = state.coin!
    let coinSettings = state.coinSettings!
    let coinAmount = state.coinAmount.decimalValue ?? 0.0
    let stakeDetails = state.stakeDetails!
    
    if stakeDetails.status == .created {
      return usecase.cancelStake(from: coin, with: coinSettings, stakeDetails: stakeDetails)
    }
    
    if stakeDetails.status == .canceled {
      return usecase.withdrawStake(from: coin, with: coinSettings, stakeDetails: stakeDetails)
    }
    
    return usecase.createStake(from: coin, with: coinSettings, amount: coinAmount)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
          store.action.accept(.updateCoinAmountError(error.message))
        }
        
        throw $0
      }
  }
}
