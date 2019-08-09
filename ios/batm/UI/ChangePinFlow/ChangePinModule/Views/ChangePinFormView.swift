import UIKit
import RxSwift
import RxCocoa

class ChangePinFormView: UIView {
  
  let oldPinTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .oldPin)
    return textField
  }()
  
  let newPinTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .newPin)
    return textField
  }()
  
  let confirmNewPinTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .confirmNewPin)
    return textField
  }()
  
  lazy var formView: MainFormView = {
    let view = MainFormView(flat: true)
    view.configure(for: [oldPinTextField,
                         newPinTextField,
                         confirmNewPinTextField])
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

extension Reactive where Base == ChangePinFormView {
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
