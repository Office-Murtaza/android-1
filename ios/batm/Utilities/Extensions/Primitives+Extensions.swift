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

extension String {
  var numberOfFractionDigits: Int {
    guard let _ = Double(self) else { return 0 }
    
    var numberOfDigits = 0
    var isDotVisited = false
    
    for i in self {
      if i == "." { isDotVisited = true }
      else if isDotVisited {
        numberOfDigits += 1
      }
    }
    
    return numberOfDigits
  }
  
  func withMaxFractionDigits(_ maxFractionDigits: Int) -> String {
    guard let _ = Double(self) else { return self }
    
    var newString = ""
    var numberOfDigits = 0
    var isDotVisited = false
    
    for i in self {
      if i == "." { isDotVisited = true }
      else if isDotVisited {
        numberOfDigits += 1
      }
      
      if (!isDotVisited || maxFractionDigits > 0) && numberOfDigits <= maxFractionDigits {
        newString.append(i)
      }
    }
    
    return newString
  }
  
  var fiatFormatted: String {
    return withMaxFractionDigits(2)
  }
  
  var fiatSellFormatted: String {
    return withMaxFractionDigits(0)
  }
  
  var coinFormatted: String {
    return withMaxFractionDigits(5)
  }
}

extension Double {
  var numberOfFractionDigits: Int {
    return String(self).numberOfFractionDigits
  }
  
  var fiatFormatted: String {
    return String(self).fiatFormatted
  }
  
  var fiatSellFormatted: String {
    return String(self).fiatSellFormatted
  }
  
  var coinFormatted: String {
    return String(self).coinFormatted
  }
  
  var multipleOfTwentyOrFifty: Double {
    let integer = Int(self)
    
    guard integer >= 0 else { return 0 }
    
    let multipleOfTwenty = integer / 20 * 20
    let multipleOfFifty = integer / 50 * 50
    let multipleOfTwentyOrFifty = max(multipleOfTwenty, multipleOfFifty)
    
    return Double(multipleOfTwentyOrFifty)
  }
}
