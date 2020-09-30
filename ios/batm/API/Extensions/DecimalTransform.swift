import ObjectMapper

class DecimalDoubleTransform: TransformType {
  public typealias Object = Decimal
  public typealias JSON = Double

  open func transformFromJSON(_ value: Any?) -> Decimal? {
    guard let double = value as? Double else { return nil }
    return Decimal(double)
  }

  open func transformToJSON(_ value: Decimal?) -> Double? {
    if let decimal = value {
      return NSDecimalNumber(decimal: decimal).doubleValue
    }
    return nil
  }
}

class DecimalIntTransform: TransformType {
  public typealias Object = Decimal
  public typealias JSON = Int

  open func transformFromJSON(_ value: Any?) -> Decimal? {
    guard let int = value as? Int else { return nil }
    return Decimal(int)
  }

  open func transformToJSON(_ value: Decimal?) -> Int? {
    if let decimal = value {
      return NSDecimalNumber(decimal: decimal).intValue
    }
    return nil
  }
}
