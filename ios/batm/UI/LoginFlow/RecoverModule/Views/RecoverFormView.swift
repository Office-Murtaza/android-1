import UIKit
import RxSwift
import RxCocoa

class RecoverFormView: UIView {
  
  let phoneNumberTextField = PhoneNumberTextField()
  
  let passwordTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .password)
    return textField
  }()
  
  lazy var formView: MainFormView = {
    let view = MainFormView()
    view.configure(for: [phoneNumberTextField,
                         passwordTextField])
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

extension Reactive where Base == RecoverFormView {
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
