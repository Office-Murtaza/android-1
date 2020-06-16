import Foundation
import RxSwift
import RxCocoa
import RxFlow

struct KeyboardState: Equatable {
  let frame: CGRect
  let animationDuration: Double
  
  init(frame: CGRect = .zero, duration: Double = 0.0) {
    self.frame = frame
    self.animationDuration = duration
  }
}

final class KeyboardHandler {
  
  static let `default` = KeyboardHandler()
  
  private let disposeBag = DisposeBag()
  
  let output = BehaviorRelay<KeyboardState>(value: KeyboardState())
  
  init() {
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
      .map(KeyboardState.init(frame: duration:))
      .bind(to: output)
      .disposed(by: disposeBag)
  }
}

extension UIViewController {
  func setupDefaultKeyboardHandling(animated: Bool = true) {
    Observable.combineLatest(KeyboardHandler.default.output, btmVisible)
      .filter { $1 }
      .map { $0.0 }
      .map({ [unowned self] state -> (CGFloat, Double) in
        let convertedFrame = self.view.convert(state.frame, from: nil)
        let keyboardFrameInView = self.view.bounds.intersection(convertedFrame)
        let safeAreaInset = self.view.safeAreaInsets.bottom
        return (keyboardFrameInView.height - safeAreaInset, state.animationDuration)
      })
      .subscribeOn(MainScheduler.instance)
      .subscribe(onNext: { [weak self] (height, animationDuration) in
        guard let self = self else { return }
        self.additionalSafeAreaInsets.bottom = height
        let duration = animated ? animationDuration : 0
        UIView.animate(withDuration: duration) { [weak self] in
          self?.view.layoutIfNeeded()
        }
      }).disposed(by: disposeBag)
  }
}

fileprivate extension Presentable where Self: UIViewController {
  var btmVisible: Observable<Bool> {
    let didAppearObservable = self.rx.sentMessage(#selector(UIViewController.viewDidAppear)).map { _ in true }
    let didDisappearObservable = self.rx.sentMessage(#selector(UIViewController.viewDidDisappear)).map { _ in false }
    
    let initialState = Observable.just(false)
    
    return initialState.concat(Observable<Bool>.merge(didAppearObservable, didDisappearObservable))
  }
}
