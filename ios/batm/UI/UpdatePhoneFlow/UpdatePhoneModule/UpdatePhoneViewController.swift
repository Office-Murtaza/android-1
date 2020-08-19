import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class UpdatePhoneViewController: NavigationScreenViewController<UpdatePhonePresenter> {
  
  let formView = UpdatePhoneFormView()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.contentView.addSubview(formView)
    customView.setTitle(localize(L.UpdatePhone.title))
    
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
    
    let updatePhoneDriver = formView.phoneNumberTextField.rx.validatablePhoneNumber
    let nextDriver = formView.rx.nextTap
    
    presenter.bind(input: UpdatePhonePresenter.Input(updatePhone: updatePhoneDriver,
                                                     next: nextDriver))
  }
}
