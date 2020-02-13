import Foundation
import RxSwift
import RxCocoa

class ShowPhonePresenter: ModulePresenter, ShowPhoneModule {
  
  struct Input {
    var back: Driver<Void>
    var change: Driver<Void>
  }
  
  let phoneNumberRelay = BehaviorRelay<String?>(value: nil)
  
  weak var delegate: ShowPhoneModuleDelegate?
  
  func setup(with phoneNumber: PhoneNumber) {
    phoneNumberRelay.accept(phoneNumber.phoneNumber.phoneFormatted)
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
