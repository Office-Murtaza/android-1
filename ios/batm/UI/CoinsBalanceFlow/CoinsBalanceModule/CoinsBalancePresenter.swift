import Foundation
import RxSwift
import RxCocoa

class CoinsBalancePresenter: ModulePresenter, CoinsBalanceModule {
  
  typealias Store = ViewStore<CoinsBalanceAction, CoinsBalanceState>
  
  struct Input {
    var refresh: Driver<Void>
    var filterCoinsTap: Driver<Void>
  }
  
  private let usecase: CoinsBalanceUsecase
  private let store: Store
  private let fetchCoinsBalanceRelay = PublishRelay<Void>()
  
  var state: Driver<CoinsBalanceState> {
    return store.state
  }
  
  weak var delegate: CoinsBalanceModuleDelegate?
  
  init(usecase: CoinsBalanceUsecase,
       store: Store = CoinsBalanceStore()) {
    self.usecase = usecase
    self.store = store
    
    super.init()
    
    setupBindings()
    fetchCoinsBalance()
  }
  
  func fetchCoinsBalance() {
    fetchCoinsBalanceRelay.accept(())
  }
  
  func bind(input: Input) {
    input.refresh
      .asObservable()
      .doOnNext { [store] in store.action.accept(.startFetching) }
      .flatMap { [unowned self] in self.track(self.usecase.getCoinsBalance()) }
      .map { CoinsBalanceAction.finishFetching($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.filterCoinsTap
      .drive(onNext: { [unowned self] in self.delegate?.showFilterCoins(from: self) })
      .disposed(by: disposeBag)
  }
  
  private func setupBindings() {
    fetchCoinsBalanceRelay
      .throttle(2.0, scheduler: MainScheduler.instance)
      .flatMap { [unowned self] in self.track(self.usecase.getCoinsBalance()) }
      .map { CoinsBalanceAction.finishFetching($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
}
