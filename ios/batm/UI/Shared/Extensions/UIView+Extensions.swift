import UIKit

extension UIView {
  func addSubviews(_ subviews: UIView...) {
    subviews.forEach(addSubview)
  }
  
  func addSubviews(_ subviews: [UIView]) {
    subviews.forEach(addSubview)
  }
  
  func shake() {
    self.transform = CGAffineTransform(translationX: 20, y: 0)
    UIView.animate(withDuration: 0.4, delay: 0, usingSpringWithDamping: 0.2, initialSpringVelocity: 1, options: .curveEaseInOut, animations: {
      self.transform = CGAffineTransform.identity
    }, completion: nil)
  }
}
