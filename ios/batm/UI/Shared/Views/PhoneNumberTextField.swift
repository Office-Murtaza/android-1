import UIKit
import RxSwift
import RxCocoa
import FlagPhoneNumber

class PhoneNumberTextField: FPNTextField, FPNTextFieldDelegate {
  
  let phoneNumberRelay = BehaviorRelay<String?>(value: nil)
  
  let imageView = UIImageView(image: UIImage(named: "login_phone"))
  
  private let padding = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 35)
  
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
    flagSize = CGSize(width: 20, height: 20)
    setCountries(including: [FPNCountryCode.US])
    
    layer.cornerRadius = 16
    layer.borderWidth = 1
    layer.borderColor = UIColor.whiteTwo.cgColor
    
    addSubview(imageView)
  }
  
  private func setupLayout() {
    snp.makeConstraints {
      $0.height.equalTo(50)
    }
    imageView.snp.makeConstraints {
      $0.centerY.equalToSuperview()
      $0.right.equalToSuperview().offset(-17)
    }
  }
  
  func fpnDidValidatePhoneNumber(textField: FPNTextField, isValid: Bool) {
    guard isValid else {
      phoneNumberRelay.accept(textField.text)
      return
    }
    
    phoneNumberRelay.accept(textField.getFormattedPhoneNumber(format: .E164))
  }
  
  func fpnDidSelectCountry(name: String, dialCode: String, code: String) {}
  
}

extension Reactive where Base == PhoneNumberTextField {
  var phoneNumber: Driver<String?> {
    return base.phoneNumberRelay.asDriver()
  }
}
