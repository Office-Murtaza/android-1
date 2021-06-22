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
  
  private func fetchCoins() -> Observable<ManageWalletsAction> {
    return usecase.getCoins()
      .asObservable()
      .map { ManageWalletsAction.updateCoins($0) }
  }
  
  func refreshCoins() {
    usecase.getCoins().subscribe(onSuccess: { [weak self] (coins) in
      self?.store.action.accept(.updateCoins(coins))
    }, onError: { (error) in
    }).disposed(by: disposeBag)
  }
  
  func changedVisibility(coin: BTMCoin ,cell: ManageWalletsCell) {
    let visibilityBeforeUpdate = coin.isVisible
    usecase.changeVisibility(of: coin).subscribeOn(MainScheduler()).subscribe { [weak self] (result) in
      if result == false {
        cell.visibilitySwitch.isOn = visibilityBeforeUpdate
      } else {
        self?.delegate?.didChangeVisibility()
        self?.refreshCoins()
      }
    } onError: { (error) in
    }.disposed(by: disposeBag)
  }
}
