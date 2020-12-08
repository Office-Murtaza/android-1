import Foundation
import RxSwift
import RxCocoa

class WalletPresenter: ModulePresenter, WalletModule {
  
  typealias Store = ViewStore<WalletAction, WalletState>
  
  struct Input {
    var refresh: Driver<Void>
    var coinSelected: Driver<IndexPath>
  }
  
  private let usecase: WalletUsecase
  private let store: Store
  private let fetchCoinsBalanceRelay = PublishRelay<Void>()
  private let balanceService: BalanceService
  
  var state: Driver<WalletState> {
    return store.state
  }
  
  weak var delegate: WalletModuleDelegate?
  
  init(usecase: WalletUsecase,
       balanceService: BalanceService,
       store: Store = WalletStore()) {
    self.usecase = usecase
    self.store = store
    self.balanceService = balanceService
  }

  func fetchCoinsBalance() {
    fetchCoinsBalanceRelay.accept(())
  }
  
  func bind(input: Input) {
    usecase.getCoins()
        .throttle(2.0, scheduler: MainScheduler.instance)
        .flatMap { [unowned self] in self.track(self.usecase.getCoinsBalance()) }
        .map { WalletAction.finishFetchingCoinsBalance($0) }
        .bind(to: store.action)
        .disposed(by: disposeBag)
    
    input.refresh
      .asObservable()
      .doOnNext { [store] in store.action.accept(.startFetching) }
      .flatMap { [unowned self] in
        self.track(self.usecase.getCoinsBalance())
          .do(onCompleted: { self.store.action.accept(.finishFetching) })
      }
      .map { WalletAction.finishFetchingCoinsBalance($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)

    input.coinSelected
      .asObservable()
      .withLatestFrom(state) { indexPath, state in state.coins[indexPath.item] }
      .flatMap{ [delegate, unowned self] coinBalance -> Signal<CoinBalance?> in
        if coinBalance.type.isETHBased {
          delegate?.showCoinDetail(predefinedConfig: self.catmPredefinedData())
          return Signal<CoinBalance?>.just(nil)
        } else {
          return Signal.just(coinBalance)
        }
      }
      .filterNil()
      .flatMap { [unowned self] coinBalance in
        return self.track(Observable.combineLatest(self.usecase.getCoinDetails(for: coinBalance.type).asObservable(),
                                                   self.usecase.getPriceChartDetails(for: coinBalance.type, period: .oneDay).asObservable()))
      }.withLatestFrom(state) { ($1, $0.0, $0.1) }
      .subscribe(onNext: { [delegate] in delegate?.showCoinDetails(coinBalances: $0.coinsBalance.coins,
                                                                   coinDetails: $1,
                                                                   data: $2) })
      .disposed(by: disposeBag)
    
    setupBindings()
    fetchCoinsBalance()
  }
  
  private func catmPredefinedData() -> CoinDetailsPredefinedDataConfig {
    let catmDataBalance = CoinBalance(type: .catm,
                                      address: "",
                                      balance: 0,
                                      fiatBalance: 0,
                                      reservedBalance: 0,
                                      reservedFiatBalance: 0,
                                      price: 0.1,
                                      index: 0)
    let horizontalLineData: [[Double]] = [[0, 50], [100, 50]]
    return CoinDetailsPredefinedDataConfig(price: 0.1,
                                           rate: 0.00,
                                           rateToDisplay: "0.00 %",
                                           balance: catmDataBalance,
                                           selectedPrediod: .oneDay,
                                           chartData: horizontalLineData)
  }
  
  private func setupBindings() {
    fetchCoinsBalanceRelay
      .observeOn(MainScheduler.instance)
      .flatMap { [unowned self] in self.track(self.balanceService.getCoinsBalance()) }
      .map { WalletAction.finishFetchingCoinsBalance($0)}
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
}
