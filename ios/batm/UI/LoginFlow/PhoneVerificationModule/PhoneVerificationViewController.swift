import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class PhoneVerificationViewController: ModuleViewController<PhoneVerificationPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let errorView = ErrorView()
  
  let formView = PhoneVerificationFormView()
  
  let nextButton = MDCButton.next
  
  static let resendCodeRange = NSRange(location:21, length: 11)
  
  let resendCodeLabel: UILabel = {
    let label = UILabel()
    let title = localize(L.PhoneVerification.resendCode)
    let attributes: [NSAttributedString.Key: Any] = [.font: UIFont.systemFont(ofSize: 15, weight: .medium)]
    let attributedText = NSMutableAttributedString(string: title, attributes: attributes)
    attributedText.addAttribute(.foregroundColor,
                                value: UIColor.slateGrey,
                                range: NSRange(location:0,length:21))
    attributedText.addAttributes([.foregroundColor: UIColor.ceruleanBlue],
                                 range: resendCodeRange)
    label.attributedText = attributedText
    label.isUserInteractionEnabled = true
    return label
  }()
  
  let resendCodeTapRecognizer = UITapGestureRecognizer()
  
  override var shouldShowNavigationBar: Bool { return true }

  override func setupUI() {
    title = localize(L.PhoneVerification.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(errorView,
                                           formView,
                                           nextButton,
                                           resendCodeLabel)
    resendCodeLabel.addGestureRecognizer(resendCodeTapRecognizer)
    
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
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(5)
      $0.centerX.equalToSuperview()
    }
    formView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(40)
      $0.left.right.equalToSuperview().inset(40)
      $0.bottom.lessThanOrEqualTo(nextButton.snp.top).offset(-20)
      $0.bottom.lessThanOrEqualTo(view.safeAreaLayoutGuide).offset(-20)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalTo(resendCodeLabel.snp.top).offset(-25)
    }
    resendCodeLabel.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let codeDriver = formView.rx.codeText
    let nextDriver = nextButton.rx.tap.asDriver()
    let resendCodeDriver = resendCodeTapRecognizer.rx.event
      .asDriver()
      .filter { [unowned self] tapRecognizer in
        return tapRecognizer.didTapAttributedTextInLabel(label: self.resendCodeLabel, inRange: Self.resendCodeRange)
      }
      .map { _ in () }
    
    presenter.bind(input: PhoneVerificationPresenter.Input(code: codeDriver,
                                                           next: nextDriver,
                                                           resendCode: resendCodeDriver))
  }
}
