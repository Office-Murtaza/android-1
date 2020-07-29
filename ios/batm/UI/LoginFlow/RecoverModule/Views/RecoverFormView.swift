import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class RecoverFormView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let phoneNumberTextField = MDCTextField.phone
  let passwordTextField = MDCTextField.password
  
  let phoneNumberTextFieldController: ThemedTextInputControllerOutlined
  let passwordTextFieldController: ThemedTextInputControllerOutlined
  
  override init(frame: CGRect) {
    phoneNumberTextFieldController = ThemedTextInputControllerOutlined(textInput: phoneNumberTextField)
    passwordTextFieldController = ThemedTextInputControllerOutlined(textInput: passwordTextField)
    
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
                                  passwordTextField)
    
    phoneNumberTextFieldController.placeholderText = localize(L.CreateWallet.Form.Phone.placeholder)
    passwordTextFieldController.placeholderText = localize(L.CreateWallet.Form.Password.placeholder)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == RecoverFormView {
  var phoneNumberText: ControlProperty<String?> {
    return base.phoneNumberTextField.rx.text
  }
  var passwordText: ControlProperty<String?> {
    return base.passwordTextField.rx.text
  }
}
