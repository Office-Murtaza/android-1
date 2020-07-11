import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class WelcomeButtonsView: UIView, HasDisposeBag {
  
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
    
    addSubviews(createButton,
                recoverButton,
                termsAndConditionsView)
  }
  
  private func setupLayout() {
    createButton.snp.makeConstraints {
      $0.top.equalToSuperview()
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
    termsAndConditionsView.rx.isAccepted
      .drive(onNext: { [unowned self] in
        self.createButton.isEnabled = $0
        self.recoverButton.isEnabled = $0
      })
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

