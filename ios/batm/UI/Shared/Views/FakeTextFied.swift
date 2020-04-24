import UIKit

class FakeTextField: UITextField, UITextFieldDelegate {
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    backgroundColor = .clear
    tintColor = .clear
    textColor = .clear
    delegate = self
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override func canPerformAction(_ action: Selector, withSender sender: Any?) -> Bool {
    return false
  }
  
  func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
    return false
  }
  
}
