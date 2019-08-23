import Foundation
import RxSwift
import RxCocoa

class ATMPresenter: ModulePresenter, ATMModule {
  
  typealias Store = ViewStore<ATMAction, ATMState>
  
  struct Input {
    
  }
  
  private let usecase: ATMUsecase
  private let store: Store
  
  var state: Driver<ATMState> {
    return store.state
  }
  
  weak var delegate: ATMModuleDelegate?
  
  init(usecase: ATMUsecase,
       store: Store = ATMStore()) {
    self.usecase = usecase
    self.store = store
  }
  
  func bind(input: Input) {
    setupBindings()
  }
  
  private func setupBindings() {
    track(usecase.getMapAddresses())
      .asObservable()
      .map { ATMAction.updateMapAddresses($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
  }
}
