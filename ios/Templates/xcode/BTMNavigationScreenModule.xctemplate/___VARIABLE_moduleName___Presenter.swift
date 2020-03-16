import Foundation
import RxSwift
import RxCocoa

final class ___FILEBASENAME___: ModulePresenter, ___VARIABLE_moduleName___Module {

  struct Input {
    var back: Driver<Void>
  }

  weak var delegate: ___VARIABLE_moduleName___ModuleDelegate?

  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinish___VARIABLE_moduleName___() })
      .disposed(by: disposeBag)
  }
}
