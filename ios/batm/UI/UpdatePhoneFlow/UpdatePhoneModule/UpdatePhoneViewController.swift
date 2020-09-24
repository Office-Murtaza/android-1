import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class UpdatePhoneViewController: ModuleViewController<UpdatePhonePresenter> {
  
  let rootScrollView = RootScrollView()
  
  let formView = UpdatePhoneFormView()
  
  let nextButton = MDCButton.next

  override func setupUI() {
    title = localize(L.UpdatePhone.title)
    
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
      .map { $0.phoneNumber }
      .bind(to: formView.rx.phoneNumberText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.phoneNumberError }
      .bind(to: formView.rx.phoneNumberErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.phoneE164.count > 0 }
      .bind(to: nextButton.rx.isEnabled)
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let updatePhoneNumberDriver = formView.rx.phoneNumberText.asDriver()
    let nextDriver = nextButton.rx.tap.asDriver()
    
    presenter.bind(input: UpdatePhonePresenter.Input(updatePhoneNumber: updatePhoneNumberDriver,
                                                     next: nextDriver))
  }
}
