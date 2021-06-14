import Foundation

extension Double {
    func formatted() -> String? {
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.maximumFractionDigits = 20
        
        return numberFormatter.string(from: NSNumber(value: self))
    }
  
    func round(to places: Int) -> Double {
       let divisor = pow(10.0, Double(places))
       return (self * divisor).rounded() / divisor
    }
}

extension String {
    func formattedStringToDouble() -> Double? {
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.maximumFractionDigits = 20
        
        return numberFormatter.number(from: self)?.doubleValue
    }
}
