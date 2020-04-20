import UIKit
import RxSwift
import RxCocoa
import FlagPhoneNumber

class PhoneNumberTextField: FPNTextField, FPNTextFieldDelegate {
  
  let validatablePhoneNumberRelay = BehaviorRelay<ValidatablePhoneNumber>(value: ValidatablePhoneNumber())
  
  let imageViewContainer = UIView()
  
  let imageView = UIImageView(image: UIImage(named: "login_phone"))
  
  init() {
    super.init(frame: .null)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    delegate = self
    
    textColor = .warmGrey
    font = .poppinsMedium12
    
    if let usCountryCode = FPNCountryCode(rawValue: "US") {
      setFlag(countryCode: usCountryCode)
    }
    
    layer.cornerRadius = 16
    layer.borderWidth = 1
    layer.borderColor = UIColor.whiteTwo.cgColor
    
    rightView = imageViewContainer
    rightViewMode = .always
    
    imageViewContainer.addSubview(imageView)
  }
  
  private func setupLayout() {
    snp.makeConstraints {
      $0.height.equalTo(50)
    }
    imageView.snp.makeConstraints {
      $0.top.bottom.left.equalToSuperview()
      $0.right.equalToSuperview().offset(-15)
    }
  }
  
  func fpnDidValidatePhoneNumber(textField: FPNTextField, isValid: Bool) {
    let phone = textField.text ?? ""
    let phoneE164 = textField.getFormattedPhoneNumber(format: .E164) ?? ""
    
    validatablePhoneNumberRelay.accept(ValidatablePhoneNumber(phone: phone,
                                                              isValid: isValid,
                                                              phoneE164: phoneE164))
  }
  
  func fpnDidSelectCountry(name: String, dialCode: String, code: String) {}
  func fpnDisplayCountryList() {}
  
}

extension Reactive where Base == PhoneNumberTextField {
  var validatablePhoneNumber: Driver<ValidatablePhoneNumber> {
    return base.validatablePhoneNumberRelay.asDriver()
  }
}
