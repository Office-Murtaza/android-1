import UIKit

extension UIStackView {
  func addArrangedSubviews(_ subviews: UIView...) {
    subviews.forEach(addArrangedSubview)
  }
  
  func addArrangedSubviews(_ subviews: [UIView]) {
    subviews.forEach(addArrangedSubview)
  }
}
