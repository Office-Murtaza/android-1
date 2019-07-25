import UIKit
import RxSwift
import RxCocoa

enum MainTextFieldType {
  case password
  case confirmPassword
  case smsCode
}

class MainTextField: UITextField {
  
  let imageView = UIImageView(image: nil)
  
  private let padding = UIEdgeInsets(top: 0, left: 18, bottom: 0, right: 35)
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    textColor = .warmGrey
    font = .poppinsMedium12
    autocorrectionType = .no
    autocapitalizationType = .none
    
    borderStyle = .none
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
  
  override open func textRect(forBounds bounds: CGRect) -> CGRect {
    return bounds.inset(by: padding)
  }
  
  override open func placeholderRect(forBounds bounds: CGRect) -> CGRect {
    return bounds.inset(by: padding)
  }
  
  override open func editingRect(forBounds bounds: CGRect) -> CGRect {
    return bounds.inset(by: padding)
  }
  
  func configure(for type: MainTextFieldType) {
    let attributes: [NSAttributedString.Key: Any] = [.foregroundColor: UIColor.warmGrey,
                                                     .font: UIFont.poppinsMedium12]
    let placeholder: String
    
    switch type {
    case .password:
      placeholder = localize(L.CreateWallet.Form.Password.placeholder)
      imageView.image = UIImage(named: "login_password")
      keyboardType = .default
      isSecureTextEntry = true
      if #available(iOS 12.0, *) {
        textContentType = .newPassword
      } else {
        textContentType = .password
      }
    case .confirmPassword:
      placeholder = localize(L.CreateWallet.Form.ConfirmPassword.placeholder)
      imageView.image = UIImage(named: "login_password")
      keyboardType = .default
      isSecureTextEntry = true
      if #available(iOS 12.0, *) {
        textContentType = .newPassword
      } else {
        textContentType = .password
      }
    case .smsCode:
      placeholder = localize(L.CreateWallet.Code.placeholder)
      imageView.image = UIImage(named: "login_sms_code")
      keyboardType = .numberPad
      isSecureTextEntry = false
      if #available(iOS 12.0, *) {
        textContentType = .oneTimeCode
      }
    }
    
    attributedPlaceholder = NSAttributedString(string: placeholder, attributes: attributes)
  }
}
