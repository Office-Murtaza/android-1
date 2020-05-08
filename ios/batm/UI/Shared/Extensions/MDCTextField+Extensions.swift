import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

extension MDCTextField {
  
  static var `default`: MDCTextField {
    let textField = MDCTextField()
    textField.backgroundColor = .white
    return textField
  }
  
  static var dropdown: MDCTextField {
    let textField = MDCTextField.default
    textField.setRightView(UIImageView(image: UIImage(named: "dropdown")))
    return textField
  }
  
  static var dialCode: MDCTextField {
    let textField = MDCTextField.dropdown
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

extension MDCOutlinedTextArea {
  
  static var `default`: MDCOutlinedTextArea {
    let textField = MDCOutlinedTextArea()
    textField.applyTheme(withScheme: MDCContainerScheme.default)
    textField.setOutlineColor(MDCContainerScheme.default.colorScheme.onSurfaceColor.withAlphaComponent(0.6), for: .normal)
    
    textField.snp.makeConstraints {
      $0.height.equalTo(textField.subviews.last!)
    }
    
    return textField
  }
  
  static var address: MDCOutlinedTextArea {
    let textField = MDCOutlinedTextArea.default
    textField.minimumNumberOfVisibleRows = 1
    textField.maximumNumberOfVisibleRows = 3
    textField.textView.autocapitalizationType = .none
    textField.textView.autocorrectionType = .no
    return textField
  }
  
  func setRightView(_ rightView: UIView) {
    addSubview(rightView)
    
    let rightPadding: CGFloat = 16
    
    rightView.snp.makeConstraints {
      $0.right.equalToSuperview().inset(rightPadding)
      $0.centerY.equalToSuperview()
    }
    
    rightView.layoutIfNeeded()
    
    let rightInset = rightView.bounds.width + rightPadding
    
    textView.textContainerInset = UIEdgeInsets(top: textView.contentInset.top,
                                               left: textView.contentInset.left,
                                               bottom: textView.contentInset.bottom,
                                               right: textView.contentInset.right + rightInset)
  }
  
}

extension Reactive where Base: MDCOutlinedTextArea {
  
  var text: ControlProperty<String?> {
    return base.textView.rx.text
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
