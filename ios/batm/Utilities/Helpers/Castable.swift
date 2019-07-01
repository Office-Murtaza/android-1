import Foundation

@discardableResult
func castable<Wrapped, Result>(_ value: Wrapped) -> Castable<Wrapped, Result> {
  return Castable.value(value)
}

func cast<Type, Result>(_ value: Type) -> Result? {
  return value as? Result
}

enum Castable<Wrapped, Result> {
  case value(Wrapped)
  case transformed(Result)
  
  @discardableResult
  func map<Subject>(as type: Subject.Type, using: (Subject) throws -> (Result?)) rethrows -> Castable<Wrapped, Result> {
    switch self {
    case let .value(value):
      if let casted: Result = try cast(value).flatMap(using) {
        return .transformed(casted)
      } else {
        return .value(value)
      }
    case .transformed:
      return self
    }
  }
  
  @discardableResult
  func map<Subject>(_ using: (Subject) throws -> (Result?)) rethrows -> Castable<Wrapped, Result> {
    return try map(as: Subject.self, using: using)
  }
  
  func extract() -> Result? {
    switch self {
    case let .transformed(result):
      return .some(result)
    default:
      return .none
    }
  }
  
  func extract(_ defaultValue: @autoclosure () -> Result) -> Result {
    return extract() ?? defaultValue()
  }
  
  func tryExtract(_ error: Error) throws -> Result {
    guard let result = extract() else { throw error }
    return result
  }
}
