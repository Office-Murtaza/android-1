import RxSwift
import ObjectMapper
import Moya

public extension PrimitiveSequence where TraitType == SingleTrait, ElementType == Response {
  
  /// Maps data received from the signal into an object
  /// which implements the ImmutableMappable protocol and returns the result back
  /// If the conversion fails, the signal errors.
  public func mapObject<T: ImmutableMappable>(_ type: T.Type, context: MapContext? = nil) -> Single<T> {
    return flatMap { response -> Single<T> in
      return Single.just(try response.mapObject(type, context: context))
    }
  }
  
  /// Maps data received from the signal into an array of objects
  /// which implement the ImmutableMappable protocol and returns the result back
  /// If the conversion fails, the signal errors.
  public func mapArray<T: ImmutableMappable>(_ type: T.Type, context: MapContext? = nil) -> Single<[T]> {
    return flatMap { response -> Single<[T]> in
      return Single.just(try response.mapArray(type, context: context))
    }
  }
}
