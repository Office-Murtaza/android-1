import Foundation
import RxSwift
import RxCocoa
import RxFlow

struct KeyboardState: Equatable {
  let height: CGFloat
  let animationDuration: Double
  
  init(height: CGFloat = 0.0, duration: Double = 0.0) {
    self.height = height
    self.animationDuration = duration
  }
}

final class KeyboardHandler {
  private let _input = BehaviorSubject<Bool>(value: true)
  private let disposeBag = DisposeBag()
  
  var input: AnyObserver<Bool> {
    return _input.asObserver()
  }
  
  var output: Driver<KeyboardState>
  
  init(with view: UIView) {
    let stateRelay = ReplaySubject<KeyboardState>.create(bufferSize: 1)
    
    let willChangeFrame = NotificationCenter.default.rx.notification(UIResponder.keyboardWillChangeFrameNotification)
    let willHide = NotificationCenter.default.rx.notification(UIResponder.keyboardWillHideNotification)
    
    Observable.merge([willChangeFrame, willHide])
      .map({ notification -> (CGRect, Double) in
        let rectValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue
        let frame = rectValue?.cgRectValue ?? CGRect.null
        let animationDuratonValue = notification
          .userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? NSNumber
        let animationDuration = animationDuratonValue?.doubleValue ?? 0.0
        return (frame, animationDuration)
      })
      .map({ [unowned view] (frame, animationDuration) -> (CGFloat, Double) in
        let convertedFrame = view.convert(frame, from: nil)
        let keyboardFrameInView = view.bounds.intersection(convertedFrame)
        let safeAreaInset = view.safeAreaInsets.bottom
        return (keyboardFrameInView.height - safeAreaInset, animationDuration)
      })
      .map(KeyboardState.init(height: duration:))
      .bind(to: stateRelay)
      .disposed(by: disposeBag)
    
    output = _input
      .flatMapLatest { isVisible -> Observable<KeyboardState> in
        if isVisible {
          return stateRelay.asObservable()
        }
        return .empty()
      }
      .asDriver(onErrorDriveWith: .empty())
  }
}

extension UIViewController {
  func setupDefaultKeyboardHandling(with handler: KeyboardHandler, animated: Bool = true) {
    handler.output.drive(onNext: { [weak self] state in
      guard let self = self else { return }
      self.additionalSafeAreaInsets.bottom = state.height
      let duration = animated ? state.animationDuration : 0
      UIView.animate(withDuration: duration) { [weak self] in
        self?.view.layoutIfNeeded()
      }
    }).disposed(by: disposeBag)
    
    btmVisible.bind(to: handler.input).disposed(by: disposeBag)
  }
}

fileprivate extension Presentable where Self: UIViewController {
  var btmVisible: Observable<Bool> {
    let willAppearObservable = self.rx.sentMessage(#selector(UIViewController.viewDidAppear)).map { _ in true }
    let didDisappearObservable = self.rx.sentMessage(#selector(UIViewController.viewDidDisappear)).map { _ in false }
    
    let initialState = Observable.just(false)
    
    return initialState.concat(Observable<Bool>.merge(willAppearObservable, didDisappearObservable))
  }
}
