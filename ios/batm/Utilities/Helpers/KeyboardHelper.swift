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
    KeyboardHandler.default.output
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
        
        guard self.view.window != nil else { return }
        
        if animated && animationDuration > 0 {
          UIView.animate(withDuration: animationDuration) { [weak self] in
            self?.view.layoutIfNeeded()
          }
        } else {
          self.view.layoutIfNeeded()
        }
      }).disposed(by: disposeBag)
  }
}
