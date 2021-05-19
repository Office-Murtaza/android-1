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
    
    weak var delegate: WalletModuleDelegate?
    var state: Driver<WalletState> { return store.state }
    
    init(usecase: WalletUsecase,
         store: Store = WalletStore()) {
        self.usecase = usecase
        self.store = store
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
            .filter { !$0.type.isETHBasedWithoutUSDC }
            .withLatestFrom(state) { coinBalance, state in return (coinBalance, state) }
            .subscribe(onNext: { [delegate] coinBalance, _ in delegate?.showCoinDetails(for: coinBalance.type) })
            .disposed(by: disposeBag)
        
        
        input.coinSelected
            .asObservable()
            .withLatestFrom(state) { indexPath, state in state.coins[indexPath.item] }
            .filter { $0.type.isETHBasedWithoutUSDC }
            .flatMap { [unowned self] coinBalance in
                return self.track(Signal.just(coinBalance).asObservable())
            }
            .subscribe { [delegate, unowned self] result in
                switch result {
                case let .next(balance):
                    let catmPredefinedData = self.catmPredefinedData(balance: balance)
                    delegate?.showCoinDetail(predefinedConfig: catmPredefinedData)
                default: break;
                }
            }
            .disposed(by: disposeBag)
        
        setupBindings()
        fetchCoinsBalance()
    }
    
    func disconnectAndRemoveTransactionDetailsNotification() {
        let notificationName = Notification.Name(TransactionDetailsNotification.disconnectTransaction)
        NotificationCenter
            .default
            .post(Notification(name: notificationName))
        
    }
    
    func removeCoinDetails() {
        usecase.removeCoinDetails()
    }
    
    private func catmPredefinedData(balance: CoinBalance) -> CoinDetailsPredefinedDataConfig {
        let horizontalLineData: [[Double]] = [[0, 50], [100, 50]]
        return CoinDetailsPredefinedDataConfig(price: NSDecimalNumber(decimal: balance.price).doubleValue,
                                               rate: 0.00,
                                               rateToDisplay: "0.00 %",
                                               balance: balance,
                                               selectedPrediod: .oneDay,
                                               chartData: horizontalLineData)
        
    }
    
    private func setupBindings() {
        fetchCoinsBalanceRelay
            .flatMap { [unowned self] in self.track(self.usecase.getCoinsBalance()) }
            .map { WalletAction.finishFetchingCoinsBalance($0)}
            .bind(to: store.action)
            .disposed(by: disposeBag)
    }
}
