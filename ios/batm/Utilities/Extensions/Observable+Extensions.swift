import Foundation
import RxSwift
import RxCocoa
import RxOptional

extension Observable {
  
  public func anyway(_ action: @escaping (() -> Void)) -> Observable<E> {
    return self.do(onNext: { _ in action() }, onError: { _ in action() })
  }
  
  public func doOnNext(_ action: @escaping ((E) throws -> Void)) -> Observable<E> {
    return self.do(onNext: action)
  }
  
}

extension Observable where E == Bool {
  
  public func filterTrue() -> Observable<E> {
    return self.filter { $0 }
  }
  
  public func filterFalse() -> Observable<E> {
    return self.filter { !$0 }
  }
  
}

extension ObservableType {
  
  func replace<V>(_ value: V) -> Observable<V> {
    return self.map { _ in value }
  }
  
  func toVoid() -> Observable<Void> {
    return self.replace(Void())
  }
  
  func delayed(_ dueTime: RxTimeInterval = 1) -> Observable<E> {
    return self.delay(dueTime, scheduler: MainScheduler.instance)
  }
}

extension PrimitiveSequence where Trait == SingleTrait {
  func delayed(_ dueTime: RxTimeInterval = 1) -> Single<E> {
    return self.asObservable()
      .delayed(dueTime)
      .asSingle()
  }
  
  func replace<V>(_ value: V) -> Single<V> {
    return self.map { _ in value }
  }
}

extension ObservableConvertibleType {
  func toCompletable() -> Completable {
    return asObservable()
      .flatMap { _ in Observable<Never>.empty() }
      .asCompletable()
  }
}

extension ObservableConvertibleType where E == Bool {
  
  func not() -> Observable<Bool> {
    return self.asObservable().map { !$0 }
  }
}

extension ObservableType where E: OptionalType {
  
  func isNil() -> Observable<Bool> {
    return map { element in
      guard element.value != nil else { return true }
      return false
    }
  }
  
  func isNotNil() -> Observable<Bool> {
    return isNil().not()
  }
}

extension ObservableType {
  func compactMap<T>() -> Observable<[T]> where E == [T?] {
    return map { $0.compactMap { $0 } }
  }
}

extension ObservableType {
  func anyway<V>(return value: V) -> Observable<V> {
    return replace(value).catchErrorJustReturn(value)
  }
}

extension PrimitiveSequence where Trait == SingleTrait {
  public func catchErrorJustReturn(_ element: E) -> Single<E> {
    return catchError { _ in .just(element) }
  }
}

extension PrimitiveSequence where Trait == SingleTrait {
  func anyway<V>(return value: V) -> Single<V> {
    return replace(value).catchErrorJustReturn(value)
  }
}

extension PrimitiveSequence where Trait == CompletableTrait, Element == Never {
  func anyway<V>(return value: V) -> Single<V> {
    return andThen(.just(value)).catchErrorJustReturn(value)
  }
  
  func toVoid() -> Single<Void> {
    return andThen(.just(()))
  }
}

extension BehaviorRelay where E == Bool {
  func toggle() {
    accept(!value)
  }
}

extension ObservableType where E: Equatable {
  func ignore(_ element: E) -> Observable<E> {
    return filter { $0 != element }
  }
}

extension ObservableType {
  func flatFilter<T>(_ signal: T) -> Observable<E> where T: ObservableConvertibleType, T.E == Bool {
    return withLatestFrom(signal) { ($0, $1) }
      .filter { $0.1 }
      .map { $0.0 }
  }
}

extension SharedSequence {
  func flatFilter<T>(_ signal: T) -> SharedSequence<S, Element> where T: ObservableConvertibleType, T.E == Bool {
    return flatMap { value in
      return signal.asObservable()
        .map { $0 ? value : nil }
        .take(1)
        .asSharedSequence(onErrorJustReturn: nil)
      }
      .filter { $0 != nil }
      .map { $0! }
  }
}

extension PrimitiveSequence where Trait == SingleTrait {
  func flatFilter<T>(_ signal: T) -> Maybe<Element> where T: ObservableConvertibleType, T.E == Bool {
    return flatMap { value in
      return signal.asObservable()
        .map { $0 ? value : nil }
        .take(1)
        .asSingle()
      }
      .filter { $0 != nil }
      .map { $0! }
  }
}
