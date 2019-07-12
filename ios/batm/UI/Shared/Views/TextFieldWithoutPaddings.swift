import UIKit

class TextFieldWithoutPaddings: UITextField {
  
  override func textRect(forBounds bounds: CGRect) -> CGRect {
    return bounds.inset(by: .zero)
  }
  
  override func placeholderRect(forBounds bounds: CGRect) -> CGRect {
    return bounds.inset(by: .zero)
  }
  
  override func editingRect(forBounds bounds: CGRect) -> CGRect {
    return bounds.inset(by: .zero)
  }
  
}
