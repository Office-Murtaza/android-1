import Foundation
import RxSwift
import RxCocoa
import PhoneNumberKit

class ShowPhonePresenter: ModulePresenter, ShowPhoneModule {
  
  struct Input {
    var back: Driver<Void>
    var change: Driver<Void>
  }
  
  let phoneNumberRelay = BehaviorRelay<String?>(value: nil)
  
  weak var delegate: ShowPhoneModuleDelegate?
  
  func setup(with phoneNumber: PhoneNumber) {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(phoneNumber.phoneNumber) else { return }
    
    let phoneNumberString = PhoneNumberKit.default.format(phoneNumber, toType: .international)
    let formattedPhoneNumber = phoneNumberString
      .split { $0 == " " || $0 == "-" }
      .joined(separator: " - ")
    
    phoneNumberRelay.accept(formattedPhoneNumber)
  }
  
  func bind(input: Input) {
    input.back
      .drive(onNext: { [delegate] in delegate?.didFinishShowPhone() })
      .disposed(by: disposeBag)
    
    input.change
      .drive(onNext: { [delegate] in delegate?.didSelectChangePhone() })
      .disposed(by: disposeBag)
  }
}
