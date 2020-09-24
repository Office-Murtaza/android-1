import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class UpdatePasswordViewController: ModuleViewController<UpdatePasswordPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let formView = UpdatePasswordFormView()
  
  let updateButton = MDCButton.update
  
  override func setupUI() {
    title = localize(L.UpdatePassword.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(formView,
                                           updateButton)
    
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
    updateButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.oldPassword }
      .bind(to: formView.rx.oldPasswordText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.newPassword }
      .bind(to: formView.rx.newPasswordText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.confirmNewPassword }
      .bind(to: formView.rx.confirmNewPasswordText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.oldPasswordError }
      .bind(to: formView.rx.oldPasswordErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.newPasswordError }
      .bind(to: formView.rx.newPasswordErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.confirmNewPasswordError }
      .bind(to: formView.rx.confirmNewPasswordErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .drive(onNext: { [unowned self] in self.updateButton.isEnabled = $0.isAllFieldsNotEmpty })
      .disposed(by: disposeBag)
    
    updateButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let updateOldPasswordDriver = formView.rx.oldPasswordText.asDriver()
    let updateNewPasswordDriver = formView.rx.newPasswordText.asDriver()
    let updateConfirmNewPasswordDriver = formView.rx.confirmNewPasswordText.asDriver()
    let updateDriver = updateButton.rx.tap.asDriver()
    
    presenter.bind(input: UpdatePasswordPresenter.Input(updateOldPassword: updateOldPasswordDriver,
                                                        updateNewPassword: updateNewPasswordDriver,
                                                        updateConfirmNewPassword: updateConfirmNewPasswordDriver,
                                                        update: updateDriver))
  }
}
