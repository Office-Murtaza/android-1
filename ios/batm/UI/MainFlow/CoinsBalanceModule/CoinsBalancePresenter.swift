import Foundation
import RxSwift
import RxCocoa

class CoinsBalancePresenter: ModulePresenter, CoinsBalanceModule {
  
  typealias Store = ViewStore<CoinsBalanceAction, CoinsBalanceState>
  
  struct Input {
    
  }
  
  private let usecase: CoinsBalanceUsecase
  private let store: Store
  
  var state: Driver<CoinsBalanceState> {
    return store.state
  }
  
  weak var delegate: CoinsBalanceModuleDelegate?
  
  init(usecase: CoinsBalanceUsecase,
       store: Store = CoinsBalanceStore()) {
    self.usecase = usecase
    self.store = store
    
    super.init()
    
    setupBindings()
  }
  
  func bind(input: Input) {
    
  }
  
  private func setupBindings() {
    usecase.getCoinsBalance()
      .subscribe(onSuccess: { [store] in store.action.accept(.updateCoinsBalance($0)) })
      .disposed(by: disposeBag)
  }
}
