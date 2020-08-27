import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class EnterPasswordFormView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let passwordTextField = MDCTextField.password
  
  let passwordTextFieldController: ThemedTextInputControllerOutlined
  
  override init(frame: CGRect) {
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
    
    addSubview(passwordTextField)
    
    passwordTextFieldController.placeholderText = localize(L.EnterPassword.Form.Password.placeholder)
  }
  
  private func setupLayout() {
    passwordTextField.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == EnterPasswordFormView {
  var passwordText: ControlProperty<String?> {
    return base.passwordTextField.rx.text
  }
  var passwordErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.passwordTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
}
