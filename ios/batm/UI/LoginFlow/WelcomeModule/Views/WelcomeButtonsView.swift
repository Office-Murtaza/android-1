import UIKit
import RxSwift
import RxCocoa

class WelcomeButtonsView: RoundedView, HasDisposeBag {
  
  let container = UIView()
  
  let errorView: ErrorView = {
    let view = ErrorView()
    view.configure(for: localize(L.Welcome.Error.title))
    view.isHidden = true
    return view
  }()
  
  let createButton: MainButton = {
    let button = MainButton()
    button.configure(for: .create)
    return button
  }()
  
  let loginButton: MainButton = {
    let button = MainButton()
    button.configure(for: .login)
    return button
  }()
  
  let termsAndConditionsView = TermsAndConditionsView()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(container,
                errorView)
    container.addSubviews(createButton,
                          loginButton,
                          termsAndConditionsView)
  }
  
  private func setupLayout() {
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(10)
      $0.centerX.equalToSuperview()
    }
    container.snp.makeConstraints {
      $0.edges.equalToSuperview().inset(30)
    }
    createButton.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    loginButton.snp.makeConstraints {
      $0.top.equalTo(createButton.snp.bottom).offset(13)
      $0.left.right.equalToSuperview()
    }
    termsAndConditionsView.snp.makeConstraints {
      $0.top.equalTo(loginButton.snp.bottom).offset(25)
      $0.centerX.equalToSuperview()
      $0.bottom.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    createButton.rx.tap
      .asObservable()
      .map { [termsAndConditionsView] in termsAndConditionsView.isAccepted }
      .bind(to: errorView.rx.isHidden)
      .disposed(by: disposeBag)
  }
}

extension Reactive where Base == WelcomeButtonsView {
  var createTap: Driver<Void> {
    return base.createButton.rx.tap
      .asDriver()
      .filter { [base] in base.termsAndConditionsView.isAccepted }
  }
}

