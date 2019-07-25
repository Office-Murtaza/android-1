import Foundation
import RxSwift
import RxCocoa

class FilterCoinsPresenter: ModulePresenter, FilterCoinsModule {
  
  typealias Store = ViewStore<FilterCoinsAction, FilterCoinsState>
  
  struct Input {
    var back: Driver<Void>
    var changeVisibility: Driver<BTMCoin>
  }
  
  private let usecase: FilterCoinsUsecase
  private let store: Store
  
  var state: Driver<FilterCoinsState> {
    return store.state
  }
  
  weak var delegate: FilterCoinsModuleDelegate?
  
  init(usecase: FilterCoinsUsecase,
       store: Store = FilterCoinsStore()) {
    self.usecase = usecase
    self.store = store
    
    super.init()
    
    self.track(fetchCoins())
      .asObservable()
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
  
  private func fetchCoins() -> Observable<FilterCoinsAction> {
    return usecase.getCoins()
      .asObservable()
      .map { FilterCoinsAction.updateCoins($0) }
  }
  
  private func changeVisibility(of coin: BTMCoin) -> Observable<FilterCoinsAction> {
    return Observable.just(coin)
      .flatMap { [usecase] in usecase.changeVisibility(of: $0).andThen(Observable.just(())) }
      .doOnNext { [delegate] in delegate?.didChangeVisibility() }
      .flatMap { [unowned self] in self.fetchCoins() }
  }
  
  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishFiltering() })
      .disposed(by: disposeBag)
    
    input.changeVisibility
      .asObservable()
      .flatMap { [unowned self] in self.track(self.changeVisibility(of: $0)) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
}
