import Foundation
import RxSwift
import RxCocoa

class AboutPresenter: ModulePresenter, AboutModule, SendHelperProtocol {
  struct Input {
    var select: Driver<IndexPath>
  }
  
  let types = AboutCellType.allCases
  
  weak var delegate: AboutModuleDelegate?
  
  func bind(input: Input) {
    input.select
      .asObservable()
      .map { [types] in types[$0.item] }
      .subscribe(onNext: { [weak self] in
        switch $0 {
        case .privacyPolicy: self?.openWeblink($0.link)
        case .compliantPolicy: self?.openWeblink($0.link)
        case .termsAndConditions: self?.openWeblink($0.link)
        default: break
        }
      })
      .disposed(by: disposeBag)
  }
}
