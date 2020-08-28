import Foundation
import RxSwift
import RxCocoa

class WalletPresenter: ModulePresenter, WalletModule {
  
  typealias Store = ViewStore<WalletAction, WalletState>
  
  struct Input {
    var refresh: Driver<Void>
    var manageWallets: Driver<Void>
    var coinSelected: Driver<IndexPath>
  }
  
  private let usecase: WalletUsecase
  private let store: Store
  private let fetchCoinsBalanceRelay = PublishRelay<Void>()
  
  var state: Driver<WalletState> {
    return store.state
  }
  
  weak var delegate: WalletModuleDelegate?
  
  init(usecase: WalletUsecase,
       store: Store = WalletStore()) {
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
      .map { WalletAction.finishFetchingCoinsBalance($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.manageWallets
      .drive(onNext: { [unowned self] in self.delegate?.showManageWallets(from: self) })
      .disposed(by: disposeBag)
    
    input.coinSelected
      .asObservable()
      .withLatestFrom(state) { indexPath, state in state.coins[indexPath.item] }
      .flatMap { [unowned self] coinBalance in
        return self.track(self.usecase.getCoinSettings(for: coinBalance.type))
        .map { (coinBalance, $0) }
      }
      .flatMap { [unowned self] coinBalance, coinSettings in
        return self.track(self.usecase.getPriceChartData(for: coinBalance.type))
          .map { (coinSettings, $0) }
      }
      .withLatestFrom(state) { ($1, $0.0, $0.1) }
      .subscribe(onNext: { [delegate] in delegate?.showCoinDetails(coinBalances: $0.coinsBalance.coins,
                                                                   coinSettings: $1,
                                                                   data: $2) })
      .disposed(by: disposeBag)
    
    setupBindings()
    fetchCoinsBalance()
  }
  
  private func setupBindings() {
    fetchCoinsBalanceRelay
      .throttle(2.0, scheduler: MainScheduler.instance)
      .flatMap { [unowned self] in self.track(self.usecase.getCoinsBalance()) }
      .map { WalletAction.finishFetchingCoinsBalance($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
}
