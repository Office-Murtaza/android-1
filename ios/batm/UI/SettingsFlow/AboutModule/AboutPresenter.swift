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
      .subscribe(onNext: {
        switch $0 {
        case .termsAndConditions: UIApplication.shared.open(URL.termsAndConditions)
        case .support: UIApplication.shared.open(URL.support)
        default: break
        }
      })
      .disposed(by: disposeBag)
  }
}
