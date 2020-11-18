import Foundation
import RxSwift
import RxCocoa

class ManageWalletsPresenter: ModulePresenter, ManageWalletsModule {
  
  typealias Store = ViewStore<ManageWalletsAction, ManageWalletsState>
  
  struct Input {
    var changeVisibility: Driver<BTMCoin>
  }
  
  private let usecase: ManageWalletsUsecase
  private let store: Store
  
  var state: Driver<ManageWalletsState> {
    return store.state
  }
  
  weak var delegate: ManageWalletsModuleDelegate?
  
  init(usecase: ManageWalletsUsecase,
       store: Store = ManageWalletsStore()) {
    self.usecase = usecase
    self.store = store
    
    super.init()
    
    self.track(fetchCoins())
      .asObservable()
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
    
    
  func bind(input: Input) {
    input.changeVisibility
      .asObservable()
      .flatMap { [unowned self] in self.track(self.changeVisibility(of: $0)) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
  
  private func fetchCoins() -> Observable<ManageWalletsAction> {
    return usecase.getCoins()
      .asObservable()
      .map { ManageWalletsAction.updateCoins($0) }
  }
  
  private func changeVisibility(of coin: BTMCoin) -> Observable<ManageWalletsAction> {
    return Observable.just(coin)
      .flatMap { [usecase] in usecase.changeVisibility(of: $0).andThen(Observable.just(())) }
      .doOnNext { [delegate] in delegate?.didChangeVisibility() }
      .flatMap { [unowned self] in self.fetchCoins() }
  }
}
