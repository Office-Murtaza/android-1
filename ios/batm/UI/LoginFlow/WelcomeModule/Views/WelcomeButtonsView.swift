import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class WelcomeButtonsView: UIView, HasDisposeBag {
  
  let errorView: ErrorView = {
    let view = ErrorView()
    view.configure(for: localize(L.Welcome.Error.title))
    view.isHidden = true
    return view
  }()
  
  let createButton = MDCButton.createNewWallet
  let recoverButton = MDCButton.recoverMyWallet
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
    
    addSubviews(errorView,
                createButton,
                recoverButton,
                termsAndConditionsView)
  }
  
  private func setupLayout() {
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview()
      $0.centerX.equalToSuperview()
    }
    createButton.snp.makeConstraints {
      $0.top.equalToSuperview().offset(15)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(48)
    }
    recoverButton.snp.makeConstraints {
      $0.top.equalTo(createButton.snp.bottom).offset(20)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(48)
    }
    termsAndConditionsView.snp.makeConstraints {
      $0.top.equalTo(recoverButton.snp.bottom).offset(20)
      $0.left.equalToSuperview()
      $0.bottom.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    createButton.rx.tap
      .asObservable()
      .withLatestFrom(termsAndConditionsView.rx.isAccepted)
      .bind(to: errorView.rx.isHidden)
      .disposed(by: disposeBag)
    
    recoverButton.rx.tap
      .asObservable()
      .withLatestFrom(termsAndConditionsView.rx.isAccepted)
      .bind(to: errorView.rx.isHidden)
      .disposed(by: disposeBag)
  }
}

extension Reactive where Base == WelcomeButtonsView {
  var createTap: Driver<Void> {
    return base.createButton.rx.tap
      .asDriver()
      .flatFilter(base.termsAndConditionsView.rx.isAccepted)
  }
  
  var recoverTap: Driver<Void> {
    return base.recoverButton.rx.tap
      .asDriver()
      .flatFilter(base.termsAndConditionsView.rx.isAccepted)
  }
}

