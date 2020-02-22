import UIKit
import RxSwift
import RxCocoa

enum MainTextFieldType {
  case oldPassword
  case newPassword
  case confirmNewPassword
  case password
  case confirmPassword
  case smsCode
  case oldPin
  case newPin
  case confirmNewPin
  case message
}

class MainTextField: UITextField, HasDisposeBag {
  
  let imageView = UIImageView(image: nil)
  let tapRecognizer = UITapGestureRecognizer()
  
  private var padding = UIEdgeInsets(top: 0, left: 18, bottom: 0, right: 18)
  
  override var isEnabled: Bool {
    didSet {
      backgroundColor = isEnabled ? .white : .whiteTwo
    }
  }
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
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
    imageView.addGestureRecognizer(tapRecognizer)
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
  
  private func setupBindings() {
    tapRecognizer.rx.event
      .asDriver()
      .drive(onNext: { [unowned self] _ in self.togglePasswordVisibility() })
      .disposed(by: disposeBag)
  }
  
  private func togglePasswordVisibility() {
    let imageName = isSecureTextEntry ? "login_password_hidden" : "login_password"
    setImage(UIImage(named: imageName))
    isSecureTextEntry.toggle()
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
  
  private func setImage(_ image: UIImage?) {
    imageView.image = image
    padding = padding.byUpdating(right: image == nil ? 18 : 35)
  }
  
  private func setUpForPassword(new: Bool = false) {
    setImage(UIImage(named: "login_password"))
    imageView.isUserInteractionEnabled = true
    isSecureTextEntry = true
    
    if #available(iOS 12.0, *), new {
      textContentType = .newPassword
    } else {
      textContentType = .password
    }
  }
  
  private func setUpForPinCode() {
    setImage(UIImage(named: "settings_pin"))
    imageView.tintColor = .whiteFive
    keyboardType = .numberPad
    isSecureTextEntry = true
  }
  
  func configure(for type: MainTextFieldType) {
    let attributes: [NSAttributedString.Key: Any] = [.foregroundColor: UIColor.warmGrey,
                                                     .font: UIFont.poppinsMedium12]
    let placeholder: String
    
    switch type {
    case .oldPassword:
      placeholder = localize(L.ChangePassword.Form.OldPassword.placeholder)
      setUpForPassword()
    case .newPassword:
      placeholder = localize(L.ChangePassword.Form.NewPassword.placeholder)
      setUpForPassword(new: true)
    case .confirmNewPassword:
      placeholder = localize(L.ChangePassword.Form.ConfirmNewPassword.placeholder)
      setUpForPassword()
    case .password:
      placeholder = localize(L.CreateWallet.Form.Password.placeholder)
      setUpForPassword(new: true)
    case .confirmPassword:
      placeholder = localize(L.CreateWallet.Form.ConfirmPassword.placeholder)
      setUpForPassword()
    case .smsCode:
      placeholder = localize(L.CreateWallet.Code.placeholder)
      setImage(UIImage(named: "login_sms_code"))
      keyboardType = .numberPad
      if #available(iOS 12.0, *) {
        textContentType = .oneTimeCode
      }
    case .oldPin:
      placeholder = localize(L.ChangePin.Form.OldPin.placeholder)
      setUpForPinCode()
    case .newPin:
      placeholder = localize(L.ChangePin.Form.NewPin.placeholder)
      setUpForPinCode()
    case .confirmNewPin:
      placeholder = localize(L.ChangePin.Form.ConfirmNewPin.placeholder)
      setUpForPinCode()
    case .message:
      placeholder = localize(L.CoinSendGift.Form.Message.placeholder)
      textAlignment = .center
    }
    
    attributedPlaceholder = NSAttributedString(string: placeholder, attributes: attributes)
  }
}
