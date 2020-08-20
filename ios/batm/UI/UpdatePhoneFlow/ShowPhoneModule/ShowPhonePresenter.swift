import Foundation
import RxSwift
import RxCocoa

class ShowPhonePresenter: ModulePresenter, ShowPhoneModule {
  
  struct Input {
    var update: Driver<Void>
  }
  
  var phoneNumber: String!
  
  var formattedPhoneNumber: String {
    return phoneNumber.phoneFormatted
  }
  
  weak var delegate: ShowPhoneModuleDelegate?
  
  func setup(with phoneNumber: PhoneNumber) {
    self.phoneNumber = phoneNumber.phoneNumber
  }
  
  func bind(input: Input) {
    input.update
      .drive(onNext: { [unowned self] in self.delegate?.didSelectUpdatePhone(phoneNumber: self.phoneNumber) })
      .disposed(by: disposeBag)
  }
}
