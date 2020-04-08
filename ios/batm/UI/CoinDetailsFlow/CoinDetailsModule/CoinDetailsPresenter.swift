import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class CoinDetailsPresenter: ModulePresenter, CoinDetailsModule {
  
  typealias Store = ViewStore<CoinDetailsAction, CoinDetailsState>

  struct Input {
    var back: Driver<Void>
    var refresh: Driver<Void>
    var withdraw: Driver<Void>
    var sendGift: Driver<Void>
    var sell: Driver<Void>
    var exchange: Driver<Void>
    var copy: Driver<String?>
    var showMore: Driver<Void>
    var transactionSelected: Driver<IndexPath>
    var updateSelectedPeriod: Driver<SelectedPeriod>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store
  private let fetchTransactionsRelay = PublishRelay<Void>()

  weak var delegate: CoinDetailsModuleDelegate?
  
  var state: Driver<CoinDetailsState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = CoinDetailsStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func setup(coinBalances: [CoinBalance], coinSettings: CoinSettings, data: PriceChartData) {
    store.action.accept(.setupCoinBalances(coinBalances))
    store.action.accept(.setupCoinSettings(coinSettings))
    store.action.accept(.setupPriceChartData(data))
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishCoinDetails() })
      .disposed(by: disposeBag)
    
    input.refresh
      .asObservable()
      .flatFilter(activity.not())
      .withLatestFrom(state)
      .map { $0.coinSettings?.type }
      .filterNil()
      .doOnNext { [store] _ in store.action.accept(.startFetching) }
      .flatMap { [unowned self] in
        self.track(self.getTransactions(for: $0), trackers: [self.errorTracker])
      }
      .subscribe()
      .disposed(by: disposeBag)
    
    input.withdraw
      .withLatestFrom(state)
      .filter { $0.coin != nil && $0.coinBalance != nil }
      .drive(onNext: { [delegate] in delegate?.showWithdrawScreen(coin: $0.coin!,
                                                                  coinBalance: $0.coinBalance!,
                                                                  coinSettings: $0.coinSettings!) })
      .disposed(by: disposeBag)
    
    input.sendGift
      .withLatestFrom(state)
      .filter { $0.coin != nil && $0.coinBalance != nil }
      .drive(onNext: { [delegate] in delegate?.showSendGiftScreen(coin: $0.coin!,
                                                                  coinBalance: $0.coinBalance!,
                                                                  coinSettings: $0.coinSettings!) })
      .disposed(by: disposeBag)
    
    input.sell
      .asObservable()
      .withLatestFrom(state)
      .filter { $0.coin != nil && $0.coinBalance != nil }
      .map { ($0.coin!, $0.coinBalance!, $0.coinSettings!) }
      .flatMap { [unowned self] coin, coinBalance, coinSettings in
        return self.track(self.usecase.getSellDetails(for: coin.type))
          .map { (coin, coinBalance, coinSettings, $0) }
      }
      .subscribe(onNext: { [delegate] in delegate?.showSellScreen(coin: $0, coinBalance: $1, coinSettings: $2, details: $3) })
      .disposed(by: disposeBag)
    
    input.exchange
      .withLatestFrom(state)
      .filter { $0.coin != nil }
      .drive(onNext: { [delegate] in delegate?.showExchangeScreen(coin: $0.coin!,
                                                                  coinBalances: $0.coinBalances!,
                                                                  coinSettings: $0.coinSettings!) })
      .disposed(by: disposeBag)
    
    input.copy
      .drive(onNext: { UIPasteboard.general.string = $0 })
      .disposed(by: disposeBag)
    
    input.showMore
      .drive(onNext: { [fetchTransactionsRelay] _ in fetchTransactionsRelay.accept(()) })
      .disposed(by: disposeBag)
    
    input.transactionSelected
      .asObservable()
      .withLatestFrom(state) { indexPath, state in
        let transaction = state.transactions?.transactions[indexPath.item]
        let id = transaction?.txId ?? transaction?.txDbId
        
        if let type = state.coin?.type, let id = id {
          return (type, id)
        }
        
        return nil
      }
      .filter { $0 != nil }
      .map { $0! }
      .flatMap { [unowned self] type, id in
        return self.track(self.usecase.getTransactionDetails(for: type, by: id))
          .map { ($0, type) }
      }
      .subscribe(onNext: { [delegate] in delegate?.showTransactionDetails(with: $0, for: $1) })
      .disposed(by: disposeBag)
    
    input.updateSelectedPeriod
      .drive(onNext: { [store] in store.action.accept(.updateSelectedPeriod($0)) })
      .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func setupBindings() {
    let coinTypeObservable = state
      .map { $0.coinSettings?.type }
      .filterNil()
      .asObservable()
      .take(1)
    
    coinTypeObservable
      .flatMap { [unowned self] in self.track(self.usecase.getCoin(for: $0)) }
      .subscribe(onNext: { [store] in store.action.accept(.finishFetchingCoin($0)) })
      .disposed(by: disposeBag)
    
    coinTypeObservable
      .flatMap { [unowned self] in
        self.track(self.getTransactions(for: $0), trackers: [self.errorTracker])
      }
      .subscribe()
      .disposed(by: disposeBag)
    
    fetchTransactionsRelay
      .flatFilter(activity.not())
      .withLatestFrom(state)
      .filter { !$0.isLastPage }
      .flatMap { [unowned self] in
        self.track(self.getTransactions(for: $0.coinSettings!.type, from: $0.nextPage), trackers: [self.errorTracker])
      }
      .subscribe()
      .disposed(by: disposeBag)
  }
  
  private func getTransactions(for type: CoinType, from index: Int = 0) -> Single<Transactions> {
    return usecase.getTransactions(for: type, from: index)
      .do(onSuccess: { [store] in
        if index > 0 {
          store.action.accept(.finishFetchingNextTransactions($0))
        } else {
          store.action.accept(.finishFetchingTransactions($0))
        }
        store.action.accept(.updatePage(index))
      },
      onError: { [store] _ in
        store.action.accept(.finishFetching)
      })
  }
}
