import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

extension MDCTextField {
  
  static var `default`: MDCTextField {
    let textField = MDCTextField()
    textField.backgroundColor = .white
    textField.font = .systemFont(ofSize: 18)
    return textField
  }
  
  static var dialCode: MDCTextField {
    let textField = MDCTextField.default
    textField.setRightView(UIImageView(image: UIImage(named: "dropdown")))
    textField.keyboardType = .phonePad
    return textField
  }
  
  static var phone: MDCTextField {
    let textField = MDCTextField.default
    textField.keyboardType = .numberPad
    return textField
  }
  
  static var amount: MDCTextField {
    let textField = MDCTextField.default
    textField.keyboardType = .decimalPad
    return textField
  }
  
  func setLeftView(_ leftView: UIView) {
    self.leftView = leftView
    self.leftViewMode = .always
  }
  
  func setRightView(_ rightView: UIView) {
    self.rightView = rightView
    self.rightViewMode = .always
  }
  
}

extension MDCMultilineTextField {
  
  static var `default`: MDCMultilineTextField {
    let textField = MDCMultilineTextField()
    textField.backgroundColor = .white
    textField.expandsOnOverflow = false
    return textField
  }
  
}

extension Reactive where Base: MDCMultilineTextField {
  
  var text: ControlProperty<String?> {
    return base.textView!.rx.text
  }
  
}
