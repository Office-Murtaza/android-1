import Foundation
import RxSwift
import RxCocoa

final class CoinWithdrawPresenter: ModulePresenter, CoinWithdrawModule {
  
  typealias Store = ViewStore<CoinWithdrawAction, CoinWithdrawState>

  struct Input {
    var updateAddress: Driver<String?>
    var updateCoinAmount: Driver<String?>
    var pasteAddress: Driver<Void>
    var max: Driver<Void>
    var submit: Driver<Void>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store

  weak var delegate: CoinWithdrawModuleDelegate?
  
  var state: Driver<CoinWithdrawState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = CoinWithdrawStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    store.action.accept(.setupCoin(coin))
    store.action.accept(.setupCoinBalances(coinBalances))
    store.action.accept(.setupCoinDetails(coinDetails))
  }

  func bind(input: Input) {
    input.updateAddress
      .asObservable()
      .withLatestFrom(state) { ($0, $1) }
      .filter { $0 != $1.address }
      .map { $0.0 }
      .map { CoinWithdrawAction.updateAddress($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateCoinAmount
      .asObservable()
      .map { CoinWithdrawAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.pasteAddress
      .asObservable()
      .map { UIPasteboard.general.string }
      .map { CoinWithdrawAction.updateAddress($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.max
      .asObservable()
      .withLatestFrom(state)
      .map { $0.maxValue.coinFormatted }
      .map { CoinWithdrawAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.submit
        .asObservable()
        .doOnNext { [store] in store.action.accept(.updateValidationState) }
        .withLatestFrom(state)
        .filter { $0.validationState.isValid }
        .flatMap { [unowned self] in self.track(self.withdraw(for: $0)) }
        .subscribe(onNext: { [weak self] in self?.proceedWithdraw(with: L.CoinDetails.Success.transactionCreated)
        })
        .disposed(by: disposeBag)
  }
  
    private func withdraw(for state: CoinWithdrawState) -> Completable {
        return usecase.withdraw(from: state.coin!,
                                with: state.coinDetails!,
                                to: state.address,
                                amount: state.coinAmount.decimalValue ?? 0.0)
            .catchError { [weak self, store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
                    if code == 2 {
                        self?.proceedWithdraw(with: L.CoinDetails.Error.transactionError)
                    } else if code == 3 {
                        store.action.accept(.updateCoinAmountError(error.message))
                    } else {
                        store.action.accept(.updateAddressError(error.message))
                    }
                }
                throw $0
            }
    }
    
    private func proceedWithdraw(with result: String) {
        let transactionResult = String(format: localize(result),
                                       localize(L.CoinDetails.withdraw))
        delegate?.didFinishCoinWithdraw(with: transactionResult)
    }
}
