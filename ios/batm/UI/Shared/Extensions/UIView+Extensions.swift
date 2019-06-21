import UIKit

extension UIView {
  func addSubviews(_ subviews: UIView...) {
    subviews.forEach(addSubview)
  }
  
  func addSubviews(_ subviews: [UIView]) {
    subviews.forEach(addSubview)
  }
}
