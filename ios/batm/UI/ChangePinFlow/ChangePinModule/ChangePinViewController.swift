import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class ChangePinViewController: ModuleViewController<ChangePinPresenter> {
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()
  
  let safeAreaContainer = UIView()
  
  let backButton: UIButton = {
    let button = UIButton()
    button.setImage(UIImage(named: "back"), for: .normal)
    return button
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.ChangePin.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  let formView = ChangePinFormView()
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.backgroundColor = .white
    
    view.addSubviews(backgroundImageView,
                     safeAreaContainer,
                     formView)
    safeAreaContainer.addSubviews(backButton,
                                  titleLabel)
  }

  override func setupLayout() {
    backgroundImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.bottom.equalTo(view.safeAreaLayoutGuide.snp.top).offset(44)
    }
    safeAreaContainer.snp.makeConstraints {
      $0.left.right.bottom.equalTo(backgroundImageView)
      $0.top.equalTo(view.safeAreaLayoutGuide)
    }
    backButton.snp.makeConstraints {
      $0.centerY.equalTo(titleLabel)
      $0.left.equalToSuperview().offset(15)
      $0.size.equalTo(45)
    }
    titleLabel.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom)
      $0.left.right.equalToSuperview()
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.validationState }
      .map { validationState -> String? in
        switch validationState {
        case .valid, .unknown: return nil
        case let .invalid(message): return message
        }
      }
      .bind(to: formView.rx.error)
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
    let updateOldPinDriver = formView.oldPinTextField.rx.text.asDriver()
    let updateNewPinDriver = formView.newPinTextField.rx.text.asDriver()
    let updateConfirmNewPinDriver = formView.confirmNewPinTextField.rx.text.asDriver()
    let cancelDriver = formView.rx.cancelTap
    let changePinDriver = formView.rx.nextTap
    
    presenter.bind(input: ChangePinPresenter.Input(back: backDriver,
                                                   updateOldPin: updateOldPinDriver,
                                                   updateNewPin: updateNewPinDriver,
                                                   updateConfirmNewPin: updateConfirmNewPinDriver,
                                                   cancel: cancelDriver,
                                                   changePin: changePinDriver))
  }
}
