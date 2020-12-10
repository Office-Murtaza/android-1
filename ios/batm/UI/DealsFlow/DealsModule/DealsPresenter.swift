import Foundation
import RxSwift
import RxCocoa

class DealsPresenter: ModulePresenter, DealsModule {
    typealias Store = ViewStore<DealsAction, DealsState>
    
    weak var delegate: DealsModuleDelegate?

    private let usecase: DealsUsecase
    private let store: Store
    
    init(usecase: DealsUsecase,
         store: Store = DealsStore()) {
        self.usecase = usecase
        self.store = store
    }
}
