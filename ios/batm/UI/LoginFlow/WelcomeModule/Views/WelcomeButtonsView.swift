import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class WelcomeButtonsView: UIView, HasDisposeBag {
  
  let createButton = MDCButton.createNewWallet
  let recoverButton = MDCButton.recoverMyWallet
  
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
                recoverButton)
  }
  
  private func setupLayout() {
    createButton.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(48)
    }
    recoverButton.snp.makeConstraints {
      $0.top.equalTo(createButton.snp.bottom).offset(20)
      $0.left.right.bottom.equalToSuperview()
      $0.height.equalTo(48)
    }
  }
  
  private func setupBindings() {
    
  }
}

extension Reactive where Base == WelcomeButtonsView {
  var createTap: Driver<Void> {
    return base.createButton.rx.tap.asDriver()
  }
  
  var recoverTap: Driver<Void> {
    return base.recoverButton.rx.tap.asDriver()
  }
}

