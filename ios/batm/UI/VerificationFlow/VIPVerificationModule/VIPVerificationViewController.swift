import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class VIPVerificationViewController: NavigationScreenViewController<VIPVerificationPresenter> {
  
  let errorView = ErrorView()
  
  let scanLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.VIPVerification.idSelfie)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold16
    return label
  }()
  
  let pickerView = VerificationFilePickerView()
  
  let formView = VIPVerificationFormView()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    customView.setTitle(localize(L.VIPVerification.title))
    customView.contentView.addSubviews(errorView,
                                       scanLabel,
                                       pickerView,
                                       formView)
  }
  
  override func setupLayout() {
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(10)
      $0.centerX.equalToSuperview()
    }
    scanLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(35)
      $0.centerX.equalToSuperview()
    }
    pickerView.snp.makeConstraints {
      $0.top.equalTo(scanLabel.snp.bottom).offset(15)
      $0.left.greaterThanOrEqualToSuperview().offset(30)
      $0.right.lessThanOrEqualToSuperview().offset(-30)
      $0.centerX.equalToSuperview()
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(pickerView.snp.bottom).offset(35)
      $0.left.right.equalToSuperview().inset(30)
      $0.bottom.equalToSuperview().offset(-35)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.selectedImage }
      .bind(to: pickerView.rx.image)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let selectDriver = pickerView.rx.select
    let removeDriver = pickerView.rx.remove
    let updateSSNDriver = formView.ssnTextField.rx.text.asDriver()
    let sendDriver = formView.sendButton.rx.tap.asDriver()
    
    presenter.bind(input: VIPVerificationPresenter.Input(back: backDriver,
                                                        select: selectDriver,
                                                        remove: removeDriver,
                                                        updateSSN: updateSSNDriver,
                                                        send: sendDriver))
  }
}
