import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class UpdatePasswordFormView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let oldPasswordTextField = MDCTextField.password
  let newPasswordTextField = MDCTextField.newPassword
  let confirmNewPasswordTextField = MDCTextField.password
  
  let oldPasswordTextFieldController: ThemedTextInputControllerOutlined
  let newPasswordTextFieldController: ThemedTextInputControllerOutlined
  let confirmNewPasswordTextFieldController: ThemedTextInputControllerOutlined
  
  override init(frame: CGRect) {
    oldPasswordTextFieldController = ThemedTextInputControllerOutlined(textInput: oldPasswordTextField)
    newPasswordTextFieldController = ThemedTextInputControllerOutlined(textInput: newPasswordTextField)
    confirmNewPasswordTextFieldController = ThemedTextInputControllerOutlined(textInput: confirmNewPasswordTextField)
    
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
    stackView.addArrangedSubviews(oldPasswordTextField,
                                  newPasswordTextField,
                                  confirmNewPasswordTextField)
    
    oldPasswordTextFieldController.placeholderText = localize(L.UpdatePassword.Form.OldPassword.placeholder)
    newPasswordTextFieldController.placeholderText = localize(L.UpdatePassword.Form.NewPassword.placeholder)
    confirmNewPasswordTextFieldController.placeholderText = localize(L.UpdatePassword.Form.ConfirmNewPassword.placeholder)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == UpdatePasswordFormView {
  var oldPasswordText: ControlProperty<String?> {
    return base.oldPasswordTextField.rx.text
  }
  var newPasswordText: ControlProperty<String?> {
    return base.newPasswordTextField.rx.text
  }
  var confirmNewPasswordText: ControlProperty<String?> {
    return base.confirmNewPasswordTextField.rx.text
  }
  var oldPasswordErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.oldPasswordTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var newPasswordErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.newPasswordTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var confirmNewPasswordErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.confirmNewPasswordTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
}
