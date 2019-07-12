import UIKit
import RxSwift
import RxCocoa
import SnapKit

class PinCodeViewController: ModuleViewController<PinCodePresenter>, UITextFieldDelegate {
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    return imageView
  }()
  
  let logoImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_logo"))
    imageView.contentMode = .scaleAspectFit
    return imageView
  }()
  
  let taglineLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Welcome.tagline)
    label.textColor = .warmGreyTwo
    label.font = .poppinsBold12
    return label
  }()
  
  let mainImageView = UIImageView(image: UIImage(named: "pin_code_main"))
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .white
    label.font = .poppinsSemibold22
    return label
  }()
  
  let pinCodeView = PinCodeView()
  
  lazy var pinCodeTextField: UITextField = {
    let textField = UITextField()
    textField.keyboardType = .numberPad
    textField.delegate = self
    return textField
  }()
  
  var handler: KeyboardHandler!
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  private func setupKeyboardHandling() {
    handler = KeyboardHandler(with: view)
    setupDefaultKeyboardHandling(with: handler, animated: false)
  }
  
  override func setupUI() {
    setupKeyboardHandling()
    
    view.backgroundColor = .whiteTwo
    view.addSubviews(pinCodeTextField,
                     backgroundImageView,
                     logoImageView,
                     taglineLabel,
                     mainImageView,
                     titleLabel,
                     pinCodeView)
    
    pinCodeTextField.becomeFirstResponder()
  }
  
  override func setupLayout() {
    backgroundImageView.snp.makeConstraints {
      $0.left.right.equalToSuperview()
      $0.centerY.equalToSuperview()
      $0.height.equalToSuperview().multipliedBy(0.66)
    }
    logoImageView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide).offset(10)
      $0.centerX.equalToSuperview()
      $0.keepRatio(for: logoImageView)
    }
    logoImageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
    logoImageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
    taglineLabel.snp.makeConstraints {
      $0.top.equalTo(logoImageView.snp.bottom).offset(5)
      $0.centerX.equalToSuperview()
      $0.bottom.lessThanOrEqualTo(backgroundImageView.snp.top).offset(-5).priority(.required)
    }
    mainImageView.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalTo(titleLabel.snp.top).offset(-15)
      $0.top.greaterThanOrEqualTo(backgroundImageView).offset(50).priority(.required)
      $0.keepRatio(for: mainImageView)
    }
    mainImageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
    mainImageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
    titleLabel.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalTo(pinCodeView.snp.top).offset(-25)
    }
    pinCodeView.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-40)
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.title }
      .bind(to: titleLabel.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.code.count }
      .bind(to: pinCodeView.rx.currentCount)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let updateCodeDriver = pinCodeTextField.rx.text.asDriver()
    
    presenter.bind(input: PinCodePresenter.Input(updateCode: updateCodeDriver))
  }
  
  func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
    if let text = textField.text, let textRange = Range(range, in: text) {
      let updatedText = text.replacingCharacters(in: textRange, with: string)
      let hasValidLength = updatedText.count <= PinCodeView.numberOfDots
      let textCharacterSet = CharacterSet(charactersIn: updatedText)
      let containsDigitsOnly = CharacterSet.decimalDigits.isSuperset(of: textCharacterSet)
      
      return hasValidLength && containsDigitsOnly
    }
    
    return true
  }
}
