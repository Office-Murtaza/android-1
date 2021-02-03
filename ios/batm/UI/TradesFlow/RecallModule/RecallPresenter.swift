import Foundation
import RxSwift
import RxCocoa

final class RecallPresenter: ModulePresenter, RecallModule {
  
  typealias Store = ViewStore<RecallAction, RecallState>

  struct Input {
    var updateCoinAmount: Driver<String?>
    var max: Driver<Void>
    var recall: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store
  weak var delegate: RecallModuleDelegate?
  
  var state: Driver<RecallState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = RecallStore()) {
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
      .map { RecallAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .withLatestFrom(state)
      .map { $0.maxValue.coinFormatted }
      .map { RecallAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.recall
        .asObservable()
        .doOnNext { [store] in store.action.accept(.updateValidationState) }
        .withLatestFrom(state)
        .filter { $0.validationState.isValid }
        .flatMap { [unowned self] in self.track(self.recall(for: $0)) }
        .subscribe(onNext: { [weak self] in self?.proceedRecall(with: L.CoinDetails.Success.transactionCreated) })
        .disposed(by: disposeBag)
  }
    
    private func recall(for state: RecallState) -> Completable {
        return usecase.recall(from: state.coin!,
                              amount: state.coinAmount.decimalValue ?? 0.0)
            .catchError { [weak self, store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
                    if error.code == 2 {
                        self?.proceedRecall(with: L.CoinDetails.Error.transactionError)
                    } else {
                        store.action.accept(.makeInvalidState(error.message))
                    }
                }

                throw $0
            }
    }
    
    private func proceedRecall(with result: String) {
        let transactionResult = String(format: localize(result),
                                       localize(L.CoinDetails.recall))
        delegate?.didFinishRecall(with: transactionResult)
    }
}
