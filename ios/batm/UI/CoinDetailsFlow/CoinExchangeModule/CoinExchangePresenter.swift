import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class CoinExchangePresenter: ModulePresenter, CoinExchangeModule {
    
    typealias Store = ViewStore<CoinExchangeAction, CoinExchangeState>
    
    struct Input {
        var updateFromCoinAmount: Driver<String?>
        var updateToCoinAmount: Driver<String?>
        var updateToPickerItem: Driver<CustomCoinType>
        var updateFromPickerItem: Driver<CustomCoinType>
        var toCoinType: Driver<CustomCoinType>
        var fromCoinType: Driver<CustomCoinType>
        var maxFrom: Driver<Void>
        var maxTo: Driver<Void>
        var submit: Driver<Void>
        var swap: Driver<Void>
    }
    
    weak var delegate: CoinExchangeModuleDelegate?
    let didViewAppear = PublishRelay<Void>()
    var coinTypeDidChange = PublishRelay<Void>()
    var alertShouldAppear = PublishRelay<Void>()
    
    private let usecase: CoinDetailsUsecase
    private let store: Store
    private let walletUseCase: WalletUsecase
    private var sortedCoins: [BTMCoin] = []
    
    
    var state: Driver<CoinExchangeState> {
        return store.state
    }
    
    init(usecase: CoinDetailsUsecase,
         walletUseCase: WalletUsecase,
         store: Store = CoinExchangeStore()) {
        self.usecase = usecase
        self.store = store
        self.walletUseCase = walletUseCase
    }
    
    func setup() {}
    
    func bind(input: Input) {
        didViewAppear
            .asObservable()
            .flatMap { [unowned self]  in
                return self.track(Observable.combineLatest(self.walletUseCase.getCoinsBalance().single().asObservable(),
                                                           self.walletUseCase.getCoinsList().asObservable()))
            }.subscribe({ [weak self] in
                guard let coins = $0.element?.1, coins.count >= 2 else {
                    self?.alertShouldAppear.accept(())
                    return
                }
                guard let coinBalance = $0.element?.0 else { return }
                self?.store.action.accept(.finishFetchingCoinsData(coinBalance, coins))
            })
            .disposed(by: disposeBag)
        
        input.updateFromCoinAmount
            .asObservable()
            .map { CoinExchangeAction.updateFromCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.updateToCoinAmount
            .asObservable()
            .map { CoinExchangeAction.updateToCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.toCoinType
            .asObservable()
            .distinctUntilChanged()
            .observeOn(MainScheduler.instance)
            .flatMap { [unowned self] type in
                self.track(Observable.combineLatest(self.usecase.getCoinsBalance(),
                                                    Signal.just(type).asObservable()))
            }
            .subscribe { [unowned self] result in
                switch result {
                case let .next((balances, type)):
                    let details = balances.coins.first { $0.type == type }?.details ?? .empty
                    self.store.action.accept(.updateToCoinDetails(details))
                default: break
                }
            }.disposed(by: disposeBag)
        
        
        input.fromCoinType
            .asObservable()
            .distinctUntilChanged()
            .observeOn(MainScheduler.instance)

            .flatMap { [unowned self] type in
                self.track(Observable.combineLatest(self.usecase.getCoinsBalance(),
                                                    Signal.just(type).asObservable()))
            }
            .subscribe { [unowned self] result in
                switch result {
                case let .next((balances, type)):
                    let details = balances.coins.first { $0.type == type }?.details ?? .empty
                    self.store.action.accept(.updateFromCoinDetails(details))
                default: break
                }
            }.disposed(by: disposeBag)
        
        input.updateToPickerItem
            .drive(onNext: { [unowned self, store] in
                store.action.accept(.updateToCoinType($0))
                self.coinTypeDidChange.accept(())
            })
            .disposed(by: disposeBag)
        
        input.updateToPickerItem
            .asObservable()
            .observeOn(MainScheduler.instance)
            .filter{ $0 == .ripple}
            .flatMap { [unowned self] type in
                self.track( self.usecase.getCoin(for: type))
            }.flatMap {
                self.track(self.usecase.getCoinActivatedState(for: $0))
            }.subscribe { [unowned self] result in
                switch result {
                case let .next(isActivated):
                    self.store.action.accept(.isCoinActivated(isActivated))
                default: break
                }
            }.disposed(by: disposeBag)
        
        input.updateFromPickerItem
            .asObservable()
            .observeOn(MainScheduler.instance)
            .flatMap { [unowned self] type in
                self.track(self.usecase.getCoin(for: type))
            }
            .subscribe { [unowned self] result in
                switch result {
                case let .next(coin):
                    self.store.action.accept(.updateFromCoin(coin))
                    self.store.action.accept(.updateFromCoinType(coin.type))
                    self.coinTypeDidChange.accept(())
                default: break
                }
            }.disposed(by: disposeBag)
        
        input.maxFrom
            .asObservable()
            .withLatestFrom(state)
            .map { $0.maxFromValue.coinFormatted }
            .map { CoinExchangeAction.updateFromCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.maxTo
            .asObservable()
            .withLatestFrom(state)
            .map { $0.maxFromValue.coinFormatted }
            .map { CoinExchangeAction.updateFromCoinAmount($0) }
            .bind(to: store.action)
            .disposed(by: disposeBag)
        
        input.submit
            .asObservable()
            .doOnNext { [store] in store.action.accept(.updateValidationState) }
            .withLatestFrom(state)
            .filter { $0.validationState.isValid }
            .flatMap { [unowned self] in self.track(self.exchange(for: $0).asCompletable()) }
            .subscribe(onNext: { [delegate] in delegate?.didFinishCoinExchange() })
            .disposed(by: disposeBag)
        
        input.swap
            .asObservable()
            .map{ CoinExchangeAction.swap }
            .bind(to: store.action)
            .disposed(by: disposeBag)
    }
    
    func handleError() {
        delegate?.handleError()
    }
    
    private func exchange(for state: CoinExchangeState) -> Single<TransactionDetails> {
        return usecase.exchange(from: state.fromCoin!,
                                with: state.coinDetails!,
                                to: state.toCoinType!,
                                amount: state.fromCoinAmount.decimalValue ?? 0.0,
                                toCoinAmount: state.toCoinAmount.decimalValue ?? 0.0)
            .catchError { [store] in
                if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
                    store.action.accept(.updateFromCoinAmountError(error.message))
                }
                
                throw $0
            }
    }
}
