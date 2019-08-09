import UIKit
import RxSwift
import RxCocoa

class ChangePasswordFormView: UIView {
  
  let oldPasswordTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .oldPassword)
    return textField
  }()
  
  let newPasswordTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .newPassword)
    return textField
  }()
  
  let confirmNewPasswordTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .confirmNewPassword)
    return textField
  }()
  
  lazy var formView: MainFormView = {
    let view = MainFormView(flat: true)
    view.configure(for: [oldPasswordTextField,
                         newPasswordTextField,
                         confirmNewPasswordTextField])
    return view
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubview(formView)
  }
  
  private func setupLayout() {
    formView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

extension Reactive where Base == ChangePasswordFormView {
  var cancelTap: Driver<Void> {
    return base.formView.rx.cancelTap
  }
  var nextTap: Driver<Void> {
    return base.formView.rx.nextTap
  }
  var error: Binder<String?> {
    return base.formView.rx.error
  }
}
