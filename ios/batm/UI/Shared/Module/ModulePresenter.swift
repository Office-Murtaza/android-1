import UIKit
import RxSwift
import RxCocoa

protocol Tracker {
  func track<O: ObservableConvertibleType>(_ source: O) -> Observable<O.E>
}

extension ObservableConvertibleType {
  func apply(trackers: [Tracker]) -> Observable<E> {
    return trackers.reduce(self.asObservable()) { signal, tracker in
      return tracker.track(signal)
    }
  }
}

class ModulePresenter {
  final let errorTracker = ErrorTracker()
  final let activityTracker = ActivityTracker()
  
  final let disposeBag = DisposeBag()
  final let visible = BehaviorRelay<Bool>(value: false)
  
  final var errors: Driver<Error> {
    return errorTracker.asDriver()
  }
  
  final var activity: Driver<Bool> {
    return activityTracker.asDriver()
  }
  
  final func track(_ completable: Completable) -> Driver<Void> {
    return track(
      completable.andThen(Observable.just( () ))
    )
  }
  
  final func track(_ completable: Completable, trackers: [Tracker]) -> Driver<Void> {
    return track(
      completable.andThen(Observable.just( () )),
      trackers: trackers
    )
  }
  
  func track<E, O: ObservableConvertibleType>(_ source: O) -> Driver<E> where O.E == E {
    return track(source, trackers: [errorTracker, activityTracker])
  }
  
  final func track<E, O: ObservableConvertibleType>(_ signal: O, trackers: [Tracker]) -> Driver<E> where O.E == E {
    return signal
      .apply(trackers: trackers)
      .asDriver(onErrorDriveWith: .empty())
  }
  
  final func trackWithError<E, O: ObservableConvertibleType>(_ signal: O,
                                                             trackers: [Tracker]) -> Observable<E> where O.E == E {
    return signal.apply(trackers: trackers)
  }
  
  func viewIsReady() {
    // Extension point
  }
}
