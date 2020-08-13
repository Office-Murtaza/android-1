import Foundation
import RxSwift
import RxCocoa

class AboutPresenter: ModulePresenter, AboutModule {
  
  struct Input {
    var select: Driver<IndexPath>
  }
  
  let types = AboutCellType.allCases
  
  weak var delegate: AboutModuleDelegate?
  
  func bind(input: Input) {
    input.select
      .asObservable()
      .map { [types] in types[$0.item] }
      .subscribe(onNext: { [delegate] in
        switch $0 {
        case .termsAndConditions: delegate?.didSelectTermsAndConditions()
        case .support: delegate?.didSelectSupport()
        case .version: delegate?.didSelectVersion()
        }
      })
      .disposed(by: disposeBag)
  }
}
