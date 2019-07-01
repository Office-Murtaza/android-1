import UIKit
import RxSwift
import RxCocoa
import SnapKit

class WelcomeViewController: ModuleViewController<WelcomePresenter>, UIScrollViewDelegate {
  
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
  
  let sliderView = WelcomeSliderView()
  
  let buttonsView = WelcomeButtonsView()
  
  let recoverLabel: UILabel = {
    let label = UILabel()
    let attributes: [NSAttributedString.Key : Any] = [
      NSAttributedString.Key.underlineStyle: NSUnderlineStyle.single.rawValue,
      NSAttributedString.Key.foregroundColor: UIColor.warmGrey,
      NSAttributedString.Key.font: UIFont.poppinsSemibold11,
      ]
    let attributedText = NSAttributedString(string: localize(L.Welcome.recoverWallet), attributes: attributes)
    label.attributedText = attributedText
    return label
  }()
  
  let supportLabel: UILabel = {
    let label = UILabel()
    let attributes: [NSAttributedString.Key : Any] = [
      NSAttributedString.Key.underlineStyle: NSUnderlineStyle.single.rawValue,
      NSAttributedString.Key.foregroundColor: UIColor.warmGrey,
      NSAttributedString.Key.font: UIFont.poppinsSemibold11,
      ]
    let attributedText = NSAttributedString(string: localize(L.Welcome.contactSupport), attributes: attributes)
    label.attributedText = attributedText
    return label
  }()
  
  let supportDummyButton = UIButton(type: .system)
  
  let backgroundDarkView: BackgroundDarkView = {
    let view = BackgroundDarkView()
    view.alpha = 0
    return view
  }()
  
  let supportView: WelcomeSupportView = {
    let view = WelcomeSupportView()
    view.alpha = 0
    return view
  }()
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override func setupUI() {
    view.backgroundColor = .whiteTwo
    
    view.addSubviews(backgroundImageView,
                     logoImageView,
                     taglineLabel,
                     sliderView,
                     buttonsView,
                     recoverLabel,
                     supportLabel,
                     supportDummyButton,
                     backgroundDarkView,
                     supportView)
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
    sliderView.snp.makeConstraints {
      $0.left.right.equalToSuperview()
      $0.bottom.equalTo(buttonsView.snp.top)
      $0.top.greaterThanOrEqualTo(backgroundImageView).offset(50).priority(.required)
    }
    buttonsView.snp.makeConstraints {
      $0.bottom.equalTo(recoverLabel.snp.top).offset(-15)
      $0.left.right.equalToSuperview().inset(30)
      $0.centerX.equalToSuperview()
    }
    recoverLabel.snp.makeConstraints {
      $0.bottom.equalTo(supportLabel.snp.top).offset(-10)
      $0.centerX.equalToSuperview()
    }
    supportLabel.snp.makeConstraints {
      $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-10)
      $0.centerX.equalToSuperview()
    }
    supportDummyButton.snp.makeConstraints {
      $0.edges.equalTo(supportLabel)
    }
    backgroundDarkView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    supportView.snp.makeConstraints {
      $0.left.right.equalTo(buttonsView)
      $0.bottom.equalToSuperview().offset(-50)
    }
  }
  
  private func hideSupportView() {
    UIView.animate(withDuration: 0.3) {
      self.backgroundDarkView.alpha = 0
      self.supportView.alpha = 0
    }
  }
  
  private func showSupportView() {
    UIView.animate(withDuration: 0.3) {
      self.backgroundDarkView.alpha = 1
      self.supportView.alpha = 1
    }
  }
  
  private func setupUIBindings() {
    supportDummyButton.rx.tap
      .asDriver()
      .drive(onNext: { [unowned self] in self.showSupportView() })
      .disposed(by: disposeBag)
    
    Driver.merge(supportView.closeButton.rx.tap.asDriver(),
                 backgroundDarkView.rx.tap)
      .drive(onNext: { [unowned self] in self.hideSupportView() })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let openTermsAndConditionsDriver = buttonsView.termsAndConditionsView.rx.termsAndConditionsTap
    let createDriver = buttonsView.rx.createTap
    presenter.bind(input: WelcomePresenter.Input(openTermsAndConditions: openTermsAndConditionsDriver,
                                                 create: createDriver))
  }
}
