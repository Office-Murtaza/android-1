import UIKit
import PhoneNumberKit
import TrustWalletCore

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

extension Array {
  func chunked(into size: Int) -> [[Element]] {
    return stride(from: 0, to: count, by: size).map {
      Array(self[$0 ..< Swift.min($0 + size, count)])
    }
  }
}

extension String {
  
  func nilIfEmpty() -> String? {
    return self.isEmpty ? nil : self
  }
  
}

extension String {
  func leadingZeros(_ maxNumberOfDigits: Int) -> String {
    let numberOfLeadingZeros = maxNumberOfDigits - count
    
    guard numberOfLeadingZeros > 0 else { return self }
    
    var newString = ""
    
    (0..<numberOfLeadingZeros).forEach { _ in
      newString.append("0")
    }
    
    newString.append(contentsOf: self)
    
    return newString
  }
}

extension Character {
  var isDecimalSeparator: Bool {
    var matchString = ".,"
    
    if let separator = Locale.current.decimalSeparator {
      matchString.append(separator)
    }
    
    return matchString.contains(self)
  }
}

extension String {
  var numberOfFractionDigits: Int {
    guard let _ = self.doubleValue else { return 0 }
    
    var numberOfFractionDigits = 0
    var isDecimalSeparatorVisited = false
    
    for i in self {
      if i.isDecimalSeparator {
        isDecimalSeparatorVisited = true
      } else if isDecimalSeparatorVisited {
        numberOfFractionDigits += 1
      }
    }
    
    return numberOfFractionDigits
  }
  
  func withFractionDigits(min minFractionDigits: Int = 0, max maxFractionDigits: Int = Int.max, trailingZeros: Bool = true) -> String {
    guard let _ = self.doubleValue else { return self }
    
    var newString = ""
    var numberOfFractionDigits = 0
    var isDecimalSeparatorVisited = false
    
    for i in self {
      if i.isDecimalSeparator {
        isDecimalSeparatorVisited = true
      } else if isDecimalSeparatorVisited {
        numberOfFractionDigits += 1
      }
      
      if (!isDecimalSeparatorVisited || maxFractionDigits > 0) && numberOfFractionDigits <= maxFractionDigits {
        newString.append(i)
      }
      
      if isDecimalSeparatorVisited && numberOfFractionDigits == maxFractionDigits { break }
    }
    
    if numberOfFractionDigits < minFractionDigits {
      if !isDecimalSeparatorVisited { newString.append(".") }
      
      (numberOfFractionDigits..<minFractionDigits).forEach { _ in newString.append("0")}
    }
    
    if !trailingZeros {
      while numberOfFractionDigits > minFractionDigits && newString.last == "0" {
        newString.removeLast()
        numberOfFractionDigits -= 1
      }
      
      if newString.last?.isDecimalSeparator ?? false {
        newString.removeLast()
      }
    }
    
    return newString
  }
  
  var fiatFormatted: String {
    return withFractionDigits(min: 2, max: 2, trailingZeros: false)
  }
  
  var fiatWithdrawFormatted: String {
    return withFractionDigits(max: 2)
  }
  
  var fiatSellFormatted: String {
    return withFractionDigits(max: 0)
  }
  
  var coinWithdrawFormatted: String {
    return withFractionDigits(max: CoinType.maxNumberOfFractionDigits)
  }
  
  var coinFormatted: String {
    return withFractionDigits(max: CoinType.maxNumberOfFractionDigits, trailingZeros: false)
  }
  
  var phoneFormatted: String {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(self) else { return self }
    
    let phoneNumberString = PhoneNumberKit.default.format(phoneNumber, toType: .international)
    let formattedPhoneNumber = phoneNumberString
      .split { $0 == " " || $0 == "-" }
      .joined(separator: " - ")
    
    return formattedPhoneNumber
  }
  
  var doubleValue: Double? {
    if let double = Double(self) { return double }
    
    var newString = ""
    for i in self {
      newString.append(i.isDecimalSeparator ? "." : i)
    }
    
    return Double(newString)
  }
  
  var withUSD: String {
    return self.appending(" USD")
  }
}

extension Double {
  var numberOfFractionDigits: Int {
    return String(self).numberOfFractionDigits
  }
  
  var fiatFormatted: String {
    return NSNumber(value: self).decimalValue.description.fiatFormatted
  }
  
  var fiatWithdrawFormatted: String {
    return String(self).fiatWithdrawFormatted
  }
  
  var fiatSellFormatted: String {
    return String(self).fiatSellFormatted
  }
  
  var coinFormatted: String {
    return NSNumber(value: self).decimalValue.description.coinFormatted
  }
  
  var coinWithdrawFormatted: String {
    return String(self).coinWithdrawFormatted
  }
  
  var nearestNumberThatCanBeGivenByTwentyAndFifty: Double {
    let integer = Int(self)
    
    guard integer >= 20 else { return 0 }
    guard integer >= 40 else { return 20 }
    
    return Double(integer / 10 * 10)
  }
  
  func equalTo(_ double: Double) -> Bool {
    return fabs(self - double) < 1e-10
  }
  
  func lessThanOrEqualTo(_ double: Double) -> Bool {
    return self < double || equalTo(double)
  }
  
  func greaterThanOrEqualTo(_ double: Double) -> Bool {
    return self > double || equalTo(double)
  }
}

extension Int {
  func pow(_ n: Int) -> Int {
    guard n >= 0 else { return self }
    if n == 0 { return 1 }
    if n == 1 { return self }
    
    var a = self
    (2...n).forEach { _ in a *= self }
    return a
  }
}
