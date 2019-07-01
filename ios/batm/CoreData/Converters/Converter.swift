import Foundation

protocol ConverterType {
  associatedtype FromType
  associatedtype ToType
  
  func convert(model: FromType) throws -> ToType
}

enum ConverterErrors: Error, Equatable {
  case error(String)
}

class Converter<FromType, ToType>: ConverterType {
  
  func convert(model: FromType) throws -> ToType {
    fatalError("Not implemented")
  }
}
