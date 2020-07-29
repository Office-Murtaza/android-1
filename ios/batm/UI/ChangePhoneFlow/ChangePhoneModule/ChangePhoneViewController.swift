import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class ChangePhoneViewController: NavigationScreenViewController<ChangePhonePresenter> {
  
  let formView = ChangePhoneFormView()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.contentView.addSubview(formView)
    customView.setTitle(localize(L.ChangePhone.title))
    
    setupDefaultKeyboardHandling()
  }

  override func setupLayout() {
    formView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.validationState }
      .mapToErrorMessage()
      .bind(to: formView.rx.error)
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updatePhoneDriver = formView.phoneNumberTextField.rx.validatablePhoneNumber
    let cancelDriver = formView.rx.cancelTap
    let changePhoneDriver = formView.rx.nextTap
    
    presenter.bind(input: ChangePhonePresenter.Input(back: backDriver,
                                                     updatePhone: updatePhoneDriver,
                                                     cancel: cancelDriver,
                                                     changePhone: changePhoneDriver))
  }
}
