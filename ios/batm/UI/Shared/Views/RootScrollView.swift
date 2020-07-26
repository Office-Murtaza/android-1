import Foundation
import UIKit
import SnapKit
import RxSwift

class RootScrollView: UIScrollView, HasDisposeBag, UIGestureRecognizerDelegate {
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let contentView = UIView()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func registerForKeyboardNotifications() {
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(adjustForKeyboard),
                                           name: UIResponder.keyboardWillShowNotification,
                                           object: nil)
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(adjustForKeyboard),
                                           name: UIResponder.keyboardWillHideNotification,
                                           object: nil)
  }
  
  @objc private func adjustForKeyboard(notification: Notification) {
     guard let keyboardValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue else { return }
     
     let keyboardHeight = keyboardValue.cgRectValue.size.height
     
     if notification.name == UIResponder.keyboardWillHideNotification {
       contentInset = .zero
     } else {
       contentInset = UIEdgeInsets(top: 0, left: 0, bottom: keyboardHeight, right: 0)
     }
     
     scrollIndicatorInsets = contentInset
   }
  
  private func setupUI() {
    registerForKeyboardNotifications()
    
    bounces = false
    keyboardDismissMode = .interactive
    
    addSubview(contentView)
    addGestureRecognizer(tapRecognizer)
    tapRecognizer.delegate = self
  }
  
  private func setupLayout() {
    contentView.snp.makeConstraints {
      $0.edges.equalToSuperview()
      $0.width.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    tapRecognizer.rx.event.asDriver().map { _ in () }
      .drive(onNext: { [unowned self] in self.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
    if touch.view == gestureRecognizer.view || touch.view == contentView {
      return true
    }
    
    var view = touch.view
    while view != gestureRecognizer.view  {
      if view?.gestureRecognizers != nil || view is UIControl {
        return false
      } else {
        view = view?.superview
      }
    }
    
    return true
  }
}
