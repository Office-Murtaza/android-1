import Foundation
import RxSwift
import RxCocoa
import TrustWalletCore

final class TradesPresenter: ModulePresenter, TradesModule {
  
  typealias Store = ViewStore<TradesAction, TradesState>

  struct Input {
    var back: Driver<Void>
    var refreshBuyTrades: Driver<Void>
    var refreshSellTrades: Driver<Void>
    var showMoreBuyTrades: Driver<Void>
    var showMoreSellTrades: Driver<Void>
  }
  
  private let store: Store
  private let usecase: TradesUsecase
  private let locationService: LocationService
  private let fetchBuyTradesRelay = PublishRelay<Void>()
  private let fetchSellTradesRelay = PublishRelay<Void>()

  weak var delegate: TradesModuleDelegate?
  
  var state: Driver<TradesState> {
    return store.state
  }
  
  init(store: Store = TradesStore(),
       usecase: TradesUsecase,
       locationService: LocationService) {
    self.store = store
    self.usecase = usecase
    self.locationService = locationService
  }
  
  func setup(coinBalance: CoinBalance) {
    store.action.accept(.setupCoinBalance(coinBalance))
  }

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishTrades() })
      .disposed(by: disposeBag)
    
    input.refreshBuyTrades
      .asObservable()
      .flatFilter(activity.not())
      .withLatestFrom(state)
      .map { $0.coinBalance?.type }
      .filterNil()
      .doOnNext { [store] _ in store.action.accept(.startFetchingBuyTrades) }
      .flatMap { [unowned self] in
        self.track(self.getBuyTrades(for: $0), trackers: [self.errorTracker])
      }
      .subscribe()
      .disposed(by: disposeBag)
    
    input.refreshSellTrades
      .asObservable()
      .flatFilter(activity.not())
      .withLatestFrom(state)
      .map { $0.coinBalance?.type }
      .filterNil()
      .doOnNext { [store] _ in store.action.accept(.startFetchingSellTrades) }
      .flatMap { [unowned self] in
        self.track(self.getSellTrades(for: $0), trackers: [self.errorTracker])
      }
      .subscribe()
      .disposed(by: disposeBag)
    
    input.showMoreBuyTrades
      .drive(onNext: { [fetchBuyTradesRelay] _ in fetchBuyTradesRelay.accept(()) })
      .disposed(by: disposeBag)
    
    input.showMoreSellTrades
      .drive(onNext: { [fetchSellTradesRelay] _ in fetchSellTradesRelay.accept(()) })
      .disposed(by: disposeBag)
    
    setupBindings()
  }
  
  private func setupBindings() {
    locationService.requestLocationIfNeeded()
    
    state
      .map { $0.coinBalance?.type }
      .filterNil()
      .asObservable()
      .take(1)
      .flatMap { [unowned self] in
        self.track(Completable.merge(self.getBuyTrades(for: $0),
                                     self.getSellTrades(for: $0)),
                   trackers: [self.errorTracker])
      }
      .subscribe()
      .disposed(by: disposeBag)
    
    fetchBuyTradesRelay
      .flatFilter(activity.not())
      .withLatestFrom(state)
      .filter { !$0.isLastBuyTradesPage }
      .flatMap { [unowned self] in
        self.track(self.getBuyTrades(for: $0.coinBalance!.type, from: $0.nextBuyTradesPage), trackers: [self.errorTracker])
      }
      .subscribe()
      .disposed(by: disposeBag)
    
    fetchSellTradesRelay
      .flatFilter(activity.not())
      .withLatestFrom(state)
      .filter { !$0.isLastSellTradesPage }
      .flatMap { [unowned self] in
        self.track(self.getSellTrades(for: $0.coinBalance!.type, from: $0.nextSellTradesPage), trackers: [self.errorTracker])
      }
      .subscribe()
      .disposed(by: disposeBag)
  }
  
  private func getBuyTrades(for type: CoinType, from index: Int = 0) -> Completable {
    return usecase.getBuyTrades(for: type, from: index)
      .do(onSuccess: { [store] in
        if index > 0 {
          store.action.accept(.finishFetchingNextBuyTrades($0))
        } else {
          store.action.accept(.finishFetchingBuyTrades($0))
        }
        store.action.accept(.updateBuyTradesPage(index))
      },
      onError: { [store] _ in
        store.action.accept(.finishFetchingBuyTradesWithError)
      })
      .toCompletable()
  }
  
  private func getSellTrades(for type: CoinType, from index: Int = 0) -> Completable {
    return usecase.getSellTrades(for: type, from: index)
      .do(onSuccess: { [store] in
        if index > 0 {
          store.action.accept(.finishFetchingNextSellTrades($0))
        } else {
          store.action.accept(.finishFetchingSellTrades($0))
        }
        store.action.accept(.updateSellTradesPage(index))
      },
      onError: { [store] _ in
        store.action.accept(.finishFetchingSellTradesWithError)
      })
      .toCompletable()
  }
}