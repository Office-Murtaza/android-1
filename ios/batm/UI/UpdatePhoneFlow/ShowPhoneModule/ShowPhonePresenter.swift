import Foundation
import RxSwift
import RxCocoa

class ShowPhonePresenter: ModulePresenter, ShowPhoneModule {
  
  struct Input {
    var update: Driver<Void>
  }
  
  var phoneNumber: String!
  
  weak var delegate: ShowPhoneModuleDelegate?
  
  func setup(with phoneNumber: PhoneNumber) {
    self.phoneNumber = phoneNumber.phoneNumber.phoneFormatted
  }
  
  func bind(input: Input) {
    input.update
      .drive(onNext: { [delegate] in delegate?.didSelectUpdatePhone() })
      .disposed(by: disposeBag)
  }
}
