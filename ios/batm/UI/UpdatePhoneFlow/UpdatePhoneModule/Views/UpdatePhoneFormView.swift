import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class UpdatePhoneFormView: UIView, HasDisposeBag {
  
  let phoneNumberTextField = MDCTextField.phone
  
  let phoneNumberTextFieldController: ThemedTextInputControllerOutlined
  
  override init(frame: CGRect) {
    phoneNumberTextFieldController = ThemedTextInputControllerOutlined(textInput: phoneNumberTextField)
    
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubview(phoneNumberTextField)
    
    phoneNumberTextFieldController.placeholderText = localize(L.UpdatePhone.Form.Phone.placeholder)
  }
  
  private func setupLayout() {
    phoneNumberTextField.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == UpdatePhoneFormView {
  var phoneNumberText: ControlProperty<String?> {
    return base.phoneNumberTextField.rx.text
  }
  var phoneNumberErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.phoneNumberTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
}
