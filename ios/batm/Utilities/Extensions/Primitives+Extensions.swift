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
    guard let _ = self.decimalValue else { return 0 }
    
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
    guard let _ = self.decimalValue else { return self }
    
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
    return withFractionDigits(max: Double.maxNumberOfFractionDigits, trailingZeros: false)
  }
  
  var fiatWithdrawFormatted: String {
    return withFractionDigits(max: Double.maxNumberOfFractionDigits)
  }
  
  var fiatSellFormatted: String {
    return withFractionDigits(max: 0)
  }
  
  var coinWithdrawFormatted: String {
    return withFractionDigits(max: CustomCoinType.maxNumberOfFractionDigits)
  }
  
  var coinFormatted: String {
    return withFractionDigits(max: CustomCoinType.maxNumberOfFractionDigits, trailingZeros: false)
  }
  
  var phoneFormatted: String {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(self) else { return self }
    
    return PhoneNumberKit.default.format(phoneNumber, toType: .international)
  }
  
  var doubleValue: Double? {
    if let double = Double(self) { return double }
    
    var newString = ""
    for i in self {
      newString.append(i.isDecimalSeparator ? "." : i)
    }
    
    return Double(newString)
  }
  
  var decimalValue: Decimal? {
    if let decimal = Decimal(string: self) { return decimal }
    
    var newString = ""
    for i in self {
      newString.append(i.isDecimalSeparator ? "." : i)
    }
    
    return Decimal(string: newString)
  }
  
  var withDollarSign: String {
    return self.appending(" $")
  }
  
  func withCoinType(_ type: CustomCoinType) -> String {
    return self.appending(" \(type.code)")
  }
  
  var intValue: Int? {
    return Int(self)
  }
  
  var separatedWords: [String] {
    return components(separatedBy: CharacterSet.letters.inverted).filter { $0.isNotEmpty }
  }
  
  func truncate(length: Int, trailing: String = "") -> String {
    if count > length {
      return String(prefix(length)) + trailing
    } else {
      return self
    }
  }
}

extension Decimal {
  var fiatFormatted: String {
    let number = NSDecimalNumber(decimal: self)
    let string = NumberFormatter.fiatFormatter.string(from: number) ?? ""
    return string.fiatFormatted
  }
  
  var fiatSellFormatted: String {
    return NSDecimalNumber(decimal: self).stringValue
  }
  
  var coinFormatted: String {
    let number = NSDecimalNumber(decimal: self)
    let string = NumberFormatter.coinFormatter.string(from: number) ?? ""
    return string.coinFormatted
  }
  
  var intValue: Int? {
    return Int(exactly: NSDecimalNumber(decimal: self))
  }
  
  var int64Value: Int64? {
    return Int64(exactly: NSDecimalNumber(decimal: self))
  }
  
  var nearestNumberThatCanBeGivenByTwentyAndFifty: Decimal {
    guard let integer = intValue else { return 0 }
    
    guard integer >= 20 else { return 0 }
    guard integer >= 40 else { return 20 }
    
    return Decimal(integer / 10 * 10)
  }
  
  func lessThanOrEqualTo(_ decimal: Decimal) -> Bool {
    return isLessThanOrEqualTo(decimal)
  }
  
  func greaterThanOrEqualTo(_ decimal: Decimal) -> Bool {
    return !isLessThanOrEqualTo(decimal) || isEqual(to: decimal)
  }
}

extension Double {
  static let maxNumberOfFractionDigits = 3
  
  var numberOfFractionDigits: Int {
    return String(self).numberOfFractionDigits
  }
  
  var fiatFormatted: String {
    let number = NSNumber(value: self)
    let string = NumberFormatter.fiatFormatter.string(from: number) ?? ""
    return string.fiatFormatted
  }
  
  var fiatSellFormatted: String {
    return String(self).fiatSellFormatted
  }
  
  var coinFormatted: String {
    let number = NSNumber(value: self)
    let string = NumberFormatter.coinFormatter.string(from: number) ?? ""
    return string.coinFormatted
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

extension NumberFormatter {
  static var defaultCurrencyFormatter: NumberFormatter {
    let formatter = NumberFormatter()
    formatter.numberStyle = .decimal
    formatter.groupingSeparator = ""
    formatter.decimalSeparator = "."
    formatter.roundingMode = .halfUp
    return formatter
  }
  
  static var fiatFormatter: NumberFormatter {
    let formatter = defaultCurrencyFormatter
    formatter.maximumFractionDigits = Double.maxNumberOfFractionDigits
    return formatter
  }
  
  static var coinFormatter: NumberFormatter {
    let formatter = defaultCurrencyFormatter
    formatter.maximumFractionDigits = CustomCoinType.maxNumberOfFractionDigits
    return formatter
  }
}
