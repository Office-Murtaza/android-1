import UIKit

extension UIEdgeInsets {
  static func top(_ value: CGFloat) -> UIEdgeInsets {
    return UIEdgeInsets(top: value, left: 0, bottom: 0, right: 0)
  }
  static func left(_ value: CGFloat) -> UIEdgeInsets {
    return UIEdgeInsets(top: 0, left: value, bottom: 0, right: 0)
  }
  static func right(_ value: CGFloat) -> UIEdgeInsets {
    return UIEdgeInsets(top: 0, left: 0, bottom: 0, right: value)
  }
  static func bottom(_ value: CGFloat) -> UIEdgeInsets {
    return UIEdgeInsets(top: 0, left: 0, bottom: value, right: 0)
  }
  
  func byUpdating(top: CGFloat) -> UIEdgeInsets {
    return UIEdgeInsets(top: top, left: self.left, bottom: self.bottom, right: self.right)
  }
  func byUpdating(bottom: CGFloat) -> UIEdgeInsets {
    return UIEdgeInsets(top: self.top, left: self.left, bottom: bottom, right: self.right)
  }
  func byUpdating(left: CGFloat) -> UIEdgeInsets {
    return UIEdgeInsets(top: self.top, left: left, bottom: self.bottom, right: self.right)
  }
  func byUpdating(right: CGFloat) -> UIEdgeInsets {
    return UIEdgeInsets(top: self.top, left: self.left, bottom: self.bottom, right: right)
  }
}

extension Double {
  var fiatFormatted: String {
    let formatter = NumberFormatter()
    formatter.maximumFractionDigits = 2
    return formatter.string(from: NSNumber(value: self)) ?? ""
  }
  
  var coinFormatted: String {
    let formatter = NumberFormatter()
    formatter.maximumFractionDigits = 5
    return formatter.string(from: NSNumber(value: self)) ?? ""
  }
}
