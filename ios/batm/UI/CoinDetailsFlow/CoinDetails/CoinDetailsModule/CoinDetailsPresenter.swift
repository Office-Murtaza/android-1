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
        var transactionSelected: Driver<IndexPath>
        var updateSelectedPeriod: Driver<SelectedPeriod>
        var recall: Driver<Void>
        var reserve: Driver<Void>
    }
    
    weak var delegate: CoinDetailsModuleDelegate?
    var state: Driver<CoinDetailsState> { return store.state }
    let updateScreenRelay = PublishRelay<Void>()
    
    private var transactionDetails: TransactionDetails?
    private var coinType: CustomCoinType = .bitcoin
    private let usecase: CoinDetailsUsecase
    private let store: Store
    private let walletUsecase: WalletUsecase
    
    init(usecase: CoinDetailsUsecase,
         walletUsecase: WalletUsecase,
         store: Store = CoinDetailsStore()) {
        self.usecase = usecase
        self.store = store
        self.walletUsecase = walletUsecase
    }
    
    func setup(with type: CustomCoinType) {
        coinType = type
    }
    
    func setup(transactionDetails: TransactionDetails?) {
        self.transactionDetails = transactionDetails
        store.action.accept(.finishFetchingTransactionDetails(transactionDetails))
    }

    func setup(predefinedData: CoinDetailsPredefinedDataConfig) {
        store.action.accept(.setupPredefinedData(predefinedData))
        store.action.accept(.setupCoinBalances([predefinedData.balance], coinType))
    }
    
    func bind(input: Input) {
        updateScreenRelay
            .flatMap { [unowned self] _ in
                return self.track(Observable.combineLatest(self.usecase.getCoinsBalance()
                                                            .do(onNext: { [weak self, store] in
                                                                store.action.accept(.setupCoinBalances($0.coins, self?.coinType ?? .bitcoin))
                                                            }),
                                                           self.usecase.getCoinDetails(for: self.coinType)
                                                            .do(onNext: { [store] in
                                                                store.action.accept(.setupCoinDetails($0))
                                                            }),
                                                           self.usecase.getTransactionDetails(for: .doge)
                                                            .do(onNext: { [store] in
                                                                store.action.accept(.finishFetchingTransactionDetails($0))
                                                            }),
                                                           self.walletUsecase.getPriceChartDetails(for: self.coinType, period: .oneDay)
                                                            .do(onSuccess: { [store] in
                                                                store.action.accept(.setupPriceChartData($0))
                                                            }).asObservable(),
                                                           self.getTransactions(for: self.coinType,
                                                                                transactionDetails: self.transactionDetails)
                                                            .asObservable()),
                trackers: [self.errorTracker])
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        input.refresh
            .asObservable()
            .flatFilter(activity.not())
            .withLatestFrom(state)
            .map { $0.coin?.type ?? .bitcoin }
            .filterNil()
            .doOnNext { [store] _ in store.action.accept(.startFetching) }
            .flatMap { [unowned self] _ in
                self.track(self.getTransactions(for: self.coinType), trackers: [self.errorTracker])
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
            .flatMap { [unowned self] _ in
                self.track(self.getTransactions(for: self.coinType), trackers: [self.errorTracker])
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        input.deposit
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [weak self, delegate] _ in delegate?.showDepositScreen(with: self?.coinType ?? .bitcoin) })
            .disposed(by: disposeBag)
        
        input.transactionSelected
            .asObservable()
            .withLatestFrom(state) { indexPath, state -> (TransactionDetails, CustomCoinType)? in
                if let type = state.coin?.type, let transaction = state.transactions?.transactions[indexPath.item] { return (transaction, type) }
                
                return nil
            }
            .filter { $0 != nil }
            .map { $0! }
            .flatMap { [unowned self] in
                return self.track(Signal.just($0).asObservable())
            }
            .subscribe { [delegate] in delegate?.showTransactionDetails(with: $0.0, coinType: $0.1) }
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
    
    func setupTransactionDetailsNotification() {
        let notificationName = Notification.Name(TransactionDetailsNotification.connectTransaction)
        NotificationCenter
            .default
            .post(Notification(name: notificationName))
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
            .map { $0.coinBalance?.type }
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
        
        predefinedTypeObservable
            .flatMap { [unowned self] in
                self.track(self.getTransactions(for: $0), trackers: [self.errorTracker])
            }
            .subscribe()
            .disposed(by: disposeBag)
    }
    
    private func proceedWithRecall(with input: Input) {
        input.recall
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [weak self, delegate] _ in delegate?.showRecall(with: self?.coinType ?? .bitcoin) })
            .disposed(by: disposeBag)
    }
    
    private func proceedWithReserve(with input: Input) {
        input.reserve
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [weak self, delegate] _ in delegate?.showReserve(with: self?.coinType ?? .bitcoin) })
            .disposed(by: disposeBag)
    }
    
    private func proceedWithWithdraw(with input: Input) {
        input.withdraw
            .withLatestFrom(state)
            .filter { $0.coin != nil }
            .drive(onNext: { [weak self, delegate] _ in delegate?.showWithdrawScreen(with: self?.coinType ?? .bitcoin) })
            .disposed(by: disposeBag)
    }
    
    private func getTransactions(for type: CustomCoinType,
                                 transactionDetails: TransactionDetails? = nil,
                                 from index: Int = 0) -> Single<Transactions> {
        
        return usecase.getTransactions(for: type, from: index)
            .do(onSuccess: { [weak self, store] in
                store.action.accept(.finishFetchingTransactions($0, transactionDetails))
                store.action.accept(.updatePage(index))
            },
            onError: { [store] _ in
                store.action.accept(.finishFetching)
            })
    }
}
