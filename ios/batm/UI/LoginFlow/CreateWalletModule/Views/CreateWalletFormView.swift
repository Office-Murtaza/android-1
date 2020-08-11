import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CreateWalletFormView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let phoneNumberTextField = MDCTextField.phone
  let passwordTextField = MDCTextField.password
  let confirmPasswordTextField = MDCTextField.password
  
  let phoneNumberTextFieldController: ThemedTextInputControllerOutlined
  let passwordTextFieldController: ThemedTextInputControllerOutlined
  let confirmPasswordTextFieldController: ThemedTextInputControllerOutlined
  
  override init(frame: CGRect) {
    phoneNumberTextFieldController = ThemedTextInputControllerOutlined(textInput: phoneNumberTextField)
    passwordTextFieldController = ThemedTextInputControllerOutlined(textInput: passwordTextField)
    confirmPasswordTextFieldController = ThemedTextInputControllerOutlined(textInput: confirmPasswordTextField)
    
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(stackView)
    stackView.addArrangedSubviews(phoneNumberTextField,
                                  passwordTextField,
                                  confirmPasswordTextField)
    
    phoneNumberTextFieldController.placeholderText = localize(L.CreateWallet.Form.Phone.placeholder)
    passwordTextFieldController.placeholderText = localize(L.CreateWallet.Form.Password.placeholder)
    confirmPasswordTextFieldController.placeholderText = localize(L.CreateWallet.Form.ConfirmPassword.placeholder)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == CreateWalletFormView {
  var phoneNumberText: ControlProperty<String?> {
    return base.phoneNumberTextField.rx.text
  }
  var passwordText: ControlProperty<String?> {
    return base.passwordTextField.rx.text
  }
  var confirmPasswordText: ControlProperty<String?> {
    return base.confirmPasswordTextField.rx.text
  }
  var phoneNumberErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.phoneNumberTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var passwordErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.passwordTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var confirmPasswordErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.confirmPasswordTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
}
