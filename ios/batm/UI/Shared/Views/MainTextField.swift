import UIKit
import RxSwift
import RxCocoa

enum MainTextFieldType {
  case oldPassword
  case newPassword
  case confirmNewPassword
  case password
  case confirmPassword
  case message
  case idNumber
  case firstName
  case lastName
  case address
  case country
  case province
  case city
  case zipCode
  case ssn
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
  
  private func setUpForPicker() {
    setImage(UIImage(named: "arrow_down"))
  }
  
  func configure(for type: MainTextFieldType) {
    let attributes: [NSAttributedString.Key: Any] = [.foregroundColor: UIColor.warmGrey,
                                                     .font: UIFont.poppinsMedium12]
    let placeholder: String
    
    switch type {
    case .oldPassword:
      placeholder = localize(L.UpdatePassword.Form.OldPassword.placeholder)
      setUpForPassword()
    case .newPassword:
      placeholder = localize(L.UpdatePassword.Form.NewPassword.placeholder)
      setUpForPassword(new: true)
    case .confirmNewPassword:
      placeholder = localize(L.UpdatePassword.Form.ConfirmNewPassword.placeholder)
      setUpForPassword()
    case .password:
      placeholder = localize(L.CreateWallet.Form.Password.placeholder)
      setUpForPassword(new: true)
    case .confirmPassword:
      placeholder = localize(L.CreateWallet.Form.ConfirmPassword.placeholder)
      setUpForPassword()
    case .message:
      placeholder = localize(L.CoinSendGift.Form.Message.placeholder)
      textAlignment = .center
    case .idNumber:
      placeholder = localize(L.Verification.Form.IDNumber.placeholder)
      keyboardType = .namePhonePad
      autocorrectionType = .no
    case .firstName:
      placeholder = localize(L.Verification.Form.FirstName.placeholder)
      keyboardType = .alphabet
    case .lastName:
      placeholder = localize(L.Verification.Form.LastName.placeholder)
      keyboardType = .alphabet
    case .address:
      placeholder = localize(L.Verification.Form.Address.placeholder)
      keyboardType = .namePhonePad
    case .country:
      placeholder = localize(L.Verification.Form.Country.placeholder)
      setUpForPicker()
    case .province:
      placeholder = localize(L.Verification.Form.Province.placeholder)
      setUpForPicker()
    case .city:
      placeholder = localize(L.Verification.Form.City.placeholder)
      setUpForPicker()
    case .zipCode:
      placeholder = localize(L.Verification.Form.ZipCode.placeholder)
      keyboardType = .numberPad
    case .ssn:
      placeholder = localize(L.VIPVerification.Form.SSN.placeholder)
      keyboardType = .numberPad
    }
    
    attributedPlaceholder = NSAttributedString(string: placeholder, attributes: attributes)
  }
}
