import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class EnterPasswordViewController: ModuleViewController<EnterPasswordPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let formView = EnterPasswordFormView()
  
  let nextButton = MDCButton.next

  override func setupUI() {
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(formView,
                                           nextButton)
    
    setupDefaultKeyboardHandling()
  }

  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
    formView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(20)
      $0.left.right.equalToSuperview().inset(15)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.password }
      .bind(to: formView.rx.passwordText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.passwordError }
      .bind(to: formView.rx.passwordErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.password.count > 0 }
      .bind(to: nextButton.rx.isEnabled)
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)

  }

  override func setupBindings() {
    setupUIBindings()
    
    let updatePasswordDriver = formView.rx.passwordText.asDriver()
    let verifyPasswordDriver = nextButton.rx.tap.asDriver()
    
    presenter.bind(input: EnterPasswordPresenter.Input(updatePassword: updatePasswordDriver,
                                                       verifyPassword: verifyPasswordDriver))
  }
}
