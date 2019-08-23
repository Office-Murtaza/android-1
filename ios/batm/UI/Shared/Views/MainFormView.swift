import UIKit
import RxSwift
import RxCocoa

class MainFormView: UIView {
  
  let roundedView = RoundedView()
  
  let container = UIView()
  
  let errorView: ErrorView = {
    let view = ErrorView()
    view.isHidden = true
    return view
  }()
  
  let cancelButton: MainButton = {
    let button = MainButton()
    button.configure(for: .cancel)
    return button
  }()
  
  let nextButton: MainButton = {
    let button = MainButton()
    button.configure(for: .next)
    return button
  }()
  
  init(flat: Bool = false) {
    super.init(frame: .null)
    
    roundedView.isHidden = flat
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(roundedView,
                container,
                errorView)
    container.addSubviews(cancelButton,
                          nextButton)
  }
  
  private func setupLayout() {
    roundedView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(10)
      $0.centerX.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    container.snp.makeConstraints {
      $0.edges.equalToSuperview().inset(30)
    }
    cancelButton.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    nextButton.snp.makeConstraints {
      $0.top.width.equalTo(cancelButton)
      $0.right.bottom.equalToSuperview()
      $0.left.equalTo(cancelButton.snp.right).offset(13)
    }
  }
  
  func configure(for textFields: [UITextField]) {
    var lastField: UITextField?
    
    textFields.enumerated().forEach { index, field in
      container.addSubview(field)
      
      if index == 0 {
        field.snp.makeConstraints {
          $0.top.left.right.equalToSuperview()
        }
      } else {
        guard let lastField = lastField else { return }
        field.snp.makeConstraints {
          $0.top.equalTo(lastField.snp.bottom).offset(13)
          $0.left.right.equalToSuperview()
        }
      }
      
      lastField = field
    }
    
    cancelButton.snp.remakeConstraints {
      if let lastField = lastField {
        $0.top.equalTo(lastField.snp.bottom).offset(13)
      } else {
        $0.top.equalToSuperview()
      }
      $0.left.bottom.equalToSuperview()
    }
  }
}

extension Reactive where Base == MainFormView {
  var cancelTap: Driver<Void> {
    return base.cancelButton.rx.tap.asDriver()
  }
  var nextTap: Driver<Void> {
    return base.nextButton.rx.tap.asDriver()
  }
  var error: Binder<String?> {
    return Binder(base) { target, value in
      target.errorView.isHidden = value == nil
      target.errorView.configure(for: value)
    }
  }
}
