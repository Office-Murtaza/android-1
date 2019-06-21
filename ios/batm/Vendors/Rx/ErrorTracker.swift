import Foundation
import RxSwift
import RxCocoa

final class ErrorTracker: SharedSequenceConvertibleType {
	
	typealias SharingStrategy = DriverSharingStrategy
	private let _subject = PublishSubject<Error>()
	
	func trackError<O: ObservableConvertibleType>(from source: O) -> Observable<O.E> {
		return source.asObservable().do(onError: { [weak self] in
			self?.onError($0)
		})
	}
	
	func asSharedSequence() -> SharedSequence<SharingStrategy, Error> {
		return _subject.asObservable().asDriver(onErrorDriveWith: .empty())
	}
	
	func asObservable() -> Observable<Error> {
		return _subject.asObservable()
	}
	
	private func onError(_ error: Error) {
		_subject.onNext(error)
	}
	
	deinit {
		_subject.onCompleted()
	}
}

extension ErrorTracker: Tracker {
	func track<O: ObservableConvertibleType>(_ source: O) -> Observable<O.E>  {
		return source.asObservable().do(onError: onError)
	}
}
