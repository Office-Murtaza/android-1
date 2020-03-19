import Foundation
import RxSwift
import RxCocoa

class CoinsBalancePresenter: ModulePresenter, CoinsBalanceModule {
  
  typealias Store = ViewStore<CoinsBalanceAction, CoinsBalanceState>
  
  struct Input {
    var refresh: Driver<Void>
    var filterCoinsTap: Driver<Void>
    var coinTap: Driver<CoinBalance>
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
  }
  
  func fetchCoinsBalance() {
    fetchCoinsBalanceRelay.accept(())
  }
  
  func bind(input: Input) {
    input.refresh
      .asObservable()
      .doOnNext { [store] in store.action.accept(.startFetching) }
      .flatMap { [unowned self] in
        self.track(self.usecase.getCoinsBalance())
          .do(onCompleted: { self.store.action.accept(.finishFetching) })
      }
      .map { CoinsBalanceAction.finishFetchingCoinsBalance($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.filterCoinsTap
      .drive(onNext: { [unowned self] in self.delegate?.showFilterCoins(from: self) })
      .disposed(by: disposeBag)
    
    input.coinTap
      .asObservable()
      .flatMap { [unowned self] coinBalance in
        return self.track(self.usecase.getPriceChartData(for: coinBalance.type))
          .map { (coinBalance, $0) }
      }
      .subscribe(onNext: { [delegate] in delegate?.showCoinDetails(with: $0, and: $1) })
      .disposed(by: disposeBag)
    
    setupBindings()
    fetchCoinsBalance()
  }
  
  private func setupBindings() {
    fetchCoinsBalanceRelay
      .throttle(2.0, scheduler: MainScheduler.instance)
      .flatMap { [unowned self] in self.track(self.usecase.getCoinsBalance()) }
      .map { CoinsBalanceAction.finishFetchingCoinsBalance($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
}
