import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class CoinDetailsPresenter: ModulePresenter, CoinDetailsModule {
    typealias Store = ViewStore<CoinDetailsAction, CoinDetailsState>
    
    struct Input {
        var refresh: Driver<Void>
        var deposit: Driver<Void>
        var withdraw: Driver<Void>
        var sendGift: Driver<Void>
        var sell: Driver<Void>
        var exchange: Driver<Void>
        var trades: Driver<Void>
        var showMore: Driver<Void>
        var transactionSelected: Driver<IndexPath>
        var updateSelectedPeriod: Driver<SelectedPeriod>
        var recall: Driver<Void>
        var reserve: Driver<Void>
    }
    
    let updateScreenRelay = PublishRelay<Void>()
    
    private let usecase: CoinDetailsUsecase
    private let store: Store
    private let fetchTransactionsRelay = PublishRelay<Void>()
    private let walletUsecase: WalletUsecase
    private let balanceService: BalanceService
    
    weak var delegate: CoinDetailsModuleDelegate?
    
    var state: Driver<CoinDetailsState> {
        return store.state
    }
    
    init(usecase: CoinDetailsUsecase,
         walletUsecase: WalletUsecase,
         balanceService: BalanceService,
         store: Store = CoinDetailsStore()) {
        self.usecase = usecase
        self.store = store
        self.walletUsecase = walletUsecase
        self.balanceService = balanceService
    }
    
    func setup(coinBalances: [CoinBalance], coinDetails: CoinDetails, data: PriceChartDetails) {
        let balance = coinBalances.first(where:{$0.type == coinDetails.type})
      
        store.action.accept(.setupCoinBalances(coinBalances))
        store.action.accept(.setupCoinDetails(coinDetails))
        store.action.accept(.setupPriceChartData(balance, data))
    }
  
    func setup(predefinedData: CoinDetailsPredefinedDataConfig) {
      store.action.accept(.setupPredefinedData(predefinedData))
      store.action.accept(.setupCoinBalances([predefinedData.balance]))
      store.action.accept(.setupCoinDetails(predefinedData.coinDetails))
    }
      
    func bind(input: Input) {
        updateScreenRelay
            .withLatestFrom(state)
            .map { $0.coinDetails?.type }
            .filterNil()
            .flatMap { [unowned self] type in
                return self.track(Completable.concat(self.balanceService.getCoinsBalance()
                                                        .asSingle()
                                                        .do(onSuccess: { [store] in store.action.accept(.setupCoinBalances($0.coins)) })
                                                        .asCompletable(),
                                                     self.getTransactions(for: type).asCompletable()),
                                  trackers: [self.errorTracker])
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        input.refresh
            .asObservable()
            .flatFilter(activity.not())
            .withLatestFrom(state)
            .map { $0.coinDetails?.type }
            .filterNil()
            .doOnNext { [store] _ in store.action.accept(.startFetching) }
            .flatMap { [unowned self] in
                self.track(self.getTransactions(for: $0), trackers: [self.errorTracker])
            }
            .subscribe()
            .disposed(by: disposeBag)
      
      input.refresh
          .asObservable()
          .flatFilter(activity.not())
          .withLatestFrom(state)
          .map { $0.predefinedData?.balance.type }
          .filterNil()
          .doOnNext { [store] _ in store.action.accept(.startFetching) }
          .flatMap { [unowned self] in
              self.track(self.getTransactions(for: $0), trackers: [self.errorTracker])
          }
          .subscribe()
          .disposed(by: disposeBag)
        
        input.deposit
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [delegate] in delegate?.showDepositScreen(coin: $0.coin!) })
            .disposed(by: disposeBag)
        
        input.sendGift
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [delegate] in delegate?.showSendGiftScreen(coin: $0.coin!,
                                                                        coinBalances: $0.coinBalances!,
                                                                        coinDetails: $0.coinDetails!) })
            .disposed(by: disposeBag)
        
        input.sell
            .asObservable()
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .map { ($0.coin!, $0.coinBalances!, $0.coinDetails!) }
            .flatMap { [unowned self] coin, coinBalances, coinDetails in
                return self.track(self.usecase.getSellDetails())
                    .map { (coin, coinBalances, coinDetails, $0) }
            }
            .subscribe(onNext: { [delegate] in delegate?.showSellScreen(coin: $0, coinBalances: $1, coinDetails: $2, details: $3) })
            .disposed(by: disposeBag)
        
        input.exchange
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [delegate] in delegate?.showExchangeScreen(coin: $0.coin!,
                                                                        coinBalances: $0.coinBalances!,
                                                                        coinDetails: $0.coinDetails!) })
            .disposed(by: disposeBag)
        
        input.trades
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [delegate] in delegate?.showTradesScreen(coin: $0.coin!,
                                                                      coinBalances: $0.coinBalances!,
                                                                      coinDetails: $0.coinDetails!) })
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
            .subscribe(onNext: { [delegate] in delegate?.showTransactionDetails(with: $0.0) })
            .disposed(by: disposeBag)
      
      input.updateSelectedPeriod
        .asObservable()
        .flatMap{ period in
          return self.track(self.updateChartDetails(period: period))
        }.subscribe()
        .disposed(by: disposeBag)
      
        proceedWithRecall(with: input)
        proceedWithReserve(with: input)
        proceedWithWithdraw(with: input)
        
      setupBindings()
    }
  
  private func updateChartDetails(period: SelectedPeriod) -> Completable {
    return Completable.create { [unowned self] completable -> Disposable in
      guard let type = self.store.currentState.coin?.type ?? self.store.currentState.predefinedData?.balance.type else {
        completable(.completed)
        return Disposables.create {}
      }
      
      if let predefinedData = self.store.currentState.predefinedData {
        store.action.accept(.updateSelectedPeriod(period, PriceChartDetails(prices: predefinedData.chartData)))
      } else {
        self.walletUsecase
          .getPriceChartDetails(for: type, period: period)
          .subscribe(onSuccess: { [store] (details) in
            store.action.accept(.updateSelectedPeriod(period, details))
          }).disposed(by: self.disposeBag)
      }
      
      completable (.completed)
      return Disposables.create {}
    }
  }
  
    private func setupBindings() {
        let coinTypeObservable = state
            .map { $0.coinDetails?.type }
            .filterNil()
            .asObservable()
            .take(1)
      
     let predefinedTypeObservable = state
        .map { $0.predefinedData?.balance.type }
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
      
      predefinedTypeObservable
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
              self.track(self.getTransactions(for: ($0.coinDetails?.type ?? $0.predefinedData!.balance.type),
                                              from: $0.nextPage),
                         trackers: [self.errorTracker])
            }
            .subscribe()
            .disposed(by: disposeBag)
    }
    
    private func proceedWithRecall(with input: Input) {
        input.recall
          .withLatestFrom(state)
          .filter { $0.coin != nil }
          .drive(onNext: { [delegate] in delegate?.showRecall(coin: $0.coin!,
                                                              coinBalances: $0.coinBalances!,
                                                              coinDetails: $0.coinDetails!) })
          .disposed(by: disposeBag)
    }
    
    private func proceedWithReserve(with input: Input) {
        input.reserve
          .withLatestFrom(state)
          .filter { $0.coin != nil }
          .drive(onNext: { [delegate] in delegate?.showReserve(coin: $0.coin!,
                                                               coinBalances: $0.coinBalances!,
                                                               coinDetails: $0.coinDetails!) })
          .disposed(by: disposeBag)
    }
    
    private func proceedWithWithdraw(with input: Input) {
        input.withdraw
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [delegate] in delegate?.showWithdrawScreen(coin: $0.coin!,
                                                                        coinBalances: $0.coinBalances!,
                                                                        coinDetails: $0.coinDetails!) })
            .disposed(by: disposeBag)
    }
    
    private func getTransactions(for type: CustomCoinType, from index: Int = 0) -> Single<Transactions> {
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
