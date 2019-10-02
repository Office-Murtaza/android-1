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
    var copy: Driver<String?>
    var showMore: Driver<Void>
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
  
  func setup(with coinBalance: CoinBalance) {
    store.action.accept(.setupCoinBalance(coinBalance))
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishCoinDetails() })
      .disposed(by: disposeBag)
    
    input.refresh
      .asObservable()
      .flatFilter(activity.not())
      .withLatestFrom(state)
      .map { $0.coinBalance }
      .filterNil()
      .flatMap { [unowned self] in self.usecase.getTransactions(for: $0.type, from: 0) }
      .subscribe(onNext: { [store] in
        store.action.accept(.finishFetchingTransactions($0))
        store.action.accept(.updatePage(0))
      })
      .disposed(by: disposeBag)
    
    input.withdraw
      .withLatestFrom(state)
      .filter { $0.coin != nil && $0.coinBalance != nil }
      .drive(onNext: { [delegate] in delegate?.showWithdrawScreen(for: $0.coin!, and: $0.coinBalance!) })
      .disposed(by: disposeBag)
    
    input.sendGift
      .drive(onNext: { print("SEND GIFT CLICKED") })
      .disposed(by: disposeBag)
    
    input.sell
      .drive(onNext: { print("SELL CLICKED") })
      .disposed(by: disposeBag)
    
    input.copy
      .drive(onNext: { UIPasteboard.general.string = $0 })
      .disposed(by: disposeBag)
    
    input.showMore
      .drive(onNext: { [fetchTransactionsRelay] _ in fetchTransactionsRelay.accept(()) })
      .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func setupBindings() {
    let combinedObservable = state
      .map { $0.coinBalance }
      .filterNil()
      .asObservable()
      .take(1)
      .flatMap { [usecase] in
        return Observable.combineLatest(usecase.getTransactions(for: $0.type, from: 0).asObservable(),
                                        usecase.getCoin(for: $0.type).asObservable())
      }
    
    track(combinedObservable)
      .drive(onNext: { [store] in
        store.action.accept(.finishFetchingTransactions($0))
        store.action.accept(.finishFetchingCoin($1))
      })
      .disposed(by: disposeBag)
    
    fetchTransactionsRelay
      .flatFilter(activity.not())
      .withLatestFrom(state)
      .filter { !$0.isLastPage }
      .flatMap { [unowned self] in self.usecase.getTransactions(for: $0.coinBalance!.type, from: $0.page + 1) }
      .doOnNext { [store] in store.action.accept(.finishFetchingNextTransactions($0)) }
      .withLatestFrom(state)
      .subscribe(onNext: { [store] in store.action.accept(.updatePage($0.page + 1)) })
      .disposed(by: disposeBag)
  }
}
