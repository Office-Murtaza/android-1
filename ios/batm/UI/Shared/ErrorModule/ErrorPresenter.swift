import Foundation
import RxSwift
import RxCocoa

enum ErrorType {
  case serverError
  case somethingWentWrong
  case noConnection
}

final class ErrorPresenter: ModulePresenter, ErrorModule {

  struct Input {
    var action: Driver<Void>
  }

  weak var delegate: ErrorModuleDelegate?
  
  var type: ErrorType!
  
  func setup(with type: ErrorType) {
    self.type = type
  }

  func bind(input: Input) {
    input.action
      .drive(onNext: { [delegate] in delegate?.didFinishError() })
      .disposed(by: disposeBag)
  }
}
