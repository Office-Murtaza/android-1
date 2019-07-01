import Foundation
import RxSwift
import RxCocoa

public class ActivityTracker: SharedSequenceConvertibleType {
	public typealias E = Bool
	public typealias SharingStrategy = DriverSharingStrategy
	
	private let _lock = NSRecursiveLock()
  private let _subject = BehaviorRelay<Bool>(value: false)
	private let _loading: SharedSequence<SharingStrategy, Bool>
	
	public init() {
		_loading = _subject.asDriver()
			.distinctUntilChanged()
	}
	
	fileprivate func trackActivityOfObservable<O: ObservableConvertibleType>(_ source: O) -> Observable<O.E> {
		return source.asObservable()
			.do(onNext: { [weak self] _ in
				self?.sendStopLoading()
				}, onError: { [weak self] _ in
					self?.sendStopLoading()
				}, onCompleted: { [weak self] in
					self?.sendStopLoading()
				}, onSubscribe: subscribed)
	}
	
	private func subscribed() {
		_lock.lock()
		_subject.accept(true)
		_lock.unlock()
	}
	
	private func sendStopLoading() {
		_lock.lock()
		_subject.accept(false)
		_lock.unlock()
	}
	
	public func asSharedSequence() -> SharedSequence<SharingStrategy, E> {
		return _loading
	}
}

extension ActivityTracker: Tracker {
	func track<O>(_ source: O) -> Observable<O.E> where O : ObservableConvertibleType {
		return trackActivityOfObservable(source)
	}
}

