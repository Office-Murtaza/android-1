import Foundation
import RxSwift
import RxCocoa

class DealsPresenter: ModulePresenter, DealsModule {
    struct Input {
        var select: Driver<IndexPath>
    }
    
    weak var delegate: DealsModuleDelegate?
    let types = DealsCellType.allCases
    private let usecase: DealsUsecase
    
    init(usecase: DealsUsecase) {
        self.usecase = usecase
    }
    
    func bind(input: Input) {
        input.select
            .asObservable()
            .map { [types] in types[$0.item] }
            .subscribe(onNext: { [delegate] in
                switch $0 {
                case .staking: delegate?.didSelectStaking()
                case .swap: delegate?.didSelectSwap()
                }
            })
            .disposed(by: disposeBag)
    }
    
    
}
