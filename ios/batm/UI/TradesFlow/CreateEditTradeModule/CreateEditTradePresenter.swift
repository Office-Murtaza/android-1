import Foundation
import RxSwift
import RxCocoa

final class CreateEditTradePresenter: ModulePresenter, CreateEditTradeModule {
  
  typealias Store = ViewStore<CreateEditTradeAction, CreateEditTradeState>

  struct Input {
    var updateSelectedType: Driver<TradeType>
    var updatePayment: Driver<String?>
    var updateMargin: Driver<String?>
    var updateMinLimit: Driver<String?>
    var updateMaxLimit: Driver<String?>
    var updateTerms: Driver<String?>
    var create: Driver<Void>
  }
  
  private let store: Store
  private let usecase: TradesUsecase

  weak var delegate: CreateEditTradeModuleDelegate?
  
  var state: Driver<CreateEditTradeState> {
    return store.state
  }
  
  init(store: Store = CreateEditTradeStore(),
       usecase: TradesUsecase) {
    self.store = store
    self.usecase = usecase
  }
  
  func setup(coinBalance: CoinBalance) {
    store.action.accept(.setupCoinBalance(coinBalance))
  }

  func bind(input: Input) {
    input.updateSelectedType
      .drive(onNext: { [store] in store.action.accept(.updateSelectedType($0)) })
      .disposed(by: disposeBag)
    
    input.updatePayment
    .drive(onNext: { [store] in store.action.accept(.updatePayment($0)) })
    .disposed(by: disposeBag)
    
    input.updateMargin
    .drive(onNext: { [store] in store.action.accept(.updateMargin($0)) })
    .disposed(by: disposeBag)
    
    input.updateMinLimit
    .drive(onNext: { [store] in store.action.accept(.updateMinLimit($0)) })
    .disposed(by: disposeBag)
    
    input.updateMaxLimit
    .drive(onNext: { [store] in store.action.accept(.updateMaxLimit($0)) })
    .disposed(by: disposeBag)
    
    input.updateTerms
    .drive(onNext: { [store] in store.action.accept(.updateTerms($0)) })
    .disposed(by: disposeBag)

    input.create
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .map { $0.data }
      .filterNil()
      .flatMap { [unowned self] in self.track(self.submitTrade(for: $0)) }
      .subscribe(onNext: { [delegate] in delegate?.didFinishCreateEditTrade() })
      .disposed(by: disposeBag)
  }
  
  private func submitTrade(for data: SubmitTradeData) -> Completable {
    return usecase.submitTrade(for: data)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError {
          store.action.accept(.makeInvalidState(error.message))
        }
        
        throw $0
      }
  }
}
