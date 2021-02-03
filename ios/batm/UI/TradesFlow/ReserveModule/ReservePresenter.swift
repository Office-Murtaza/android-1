import Foundation
import RxSwift
import RxCocoa

final class ReservePresenter: ModulePresenter, ReserveModule {
  
  typealias Store = ViewStore<ReserveAction, ReserveState>

  struct Input {
    var updateCoinAmount: Driver<String?>
    var max: Driver<Void>
    var reserve: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store

  weak var delegate: ReserveModuleDelegate?
  
  var state: Driver<ReserveState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = ReserveStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    store.action.accept(.setupCoin(coin))
    store.action.accept(.setupCoinBalances(coinBalances))
    store.action.accept(.setupCoinDetails(coinDetails))
  }

  func bind(input: Input) {
    input.updateCoinAmount
      .asObservable()
      .map { ReserveAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .withLatestFrom(state)
      .map { $0.maxValue.coinFormatted }
      .map { ReserveAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.reserve
        .asObservable()
        .doOnNext { [store] in store.action.accept(.updateValidationState) }
        .withLatestFrom(state)
        .filter { $0.validationState.isValid }
        .flatMap { [unowned self] in self.track(self.reserve(for: $0)) }
        .subscribe(onNext: { [weak self] in self?.proceedReserve(with: L.CoinDetails.Success.transactionCreated) })
        .disposed(by: disposeBag)
  }
  
    private func reserve(for state: ReserveState) -> Completable {
        return usecase.reserve(from: state.coin!,
                               with: state.coinDetails!,
                               amount: state.coinAmount.decimalValue ?? 0.0)
            .catchError { [weak self, store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
                    if error.code == 2 {
                        self?.proceedReserve(with: L.CoinDetails.Error.transactionError)
                    } else {
                        store.action.accept(.makeInvalidState(error.message))
                    }
                }

                throw $0
            }
    }
    
    private func proceedReserve(with result: String) {
        let transactionResult = String(format: localize(result),
                                       localize(L.CoinDetails.reserve))
        delegate?.didFinishReserve(with: transactionResult)
    }
}
