import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class PasswordTextField: MDCTextField, HasDisposeBag {
  
  let button = UIButton()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override var isSecureTextEntry: Bool {
    didSet {
      let imageName = isSecureTextEntry ? "login_password" : "login_password_hidden"
      button.setImage(UIImage(named: imageName), for: .normal)
    }
  }
  
  private func setupUI() {
    isSecureTextEntry = true
    
    setRightView(button)
  }
  
  private func setupBindings() {
    button.rx.tap.asDriver()
      .drive(onNext: { [unowned self] in self.isSecureTextEntry.toggle() })
      .disposed(by: disposeBag)
  }
  
}
