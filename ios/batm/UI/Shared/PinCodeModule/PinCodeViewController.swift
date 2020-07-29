import UIKit
import RxSwift
import RxCocoa
import SnapKit

class PinCodeViewController: ModuleViewController<PinCodePresenter>, UITextFieldDelegate {
  
  let didDisappearRelay = PublishRelay<Void>()
  
  let logoImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_logo"))
    imageView.contentMode = .scaleAspectFit
    return imageView
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .systemFont(ofSize: 20, weight: .bold)
    return label
  }()
  
  let dotsView = PinCodeDotsView()
  
  let keyboardView = PinCodeKeyboardView()
  
  var shouldShowNavBar = false
  
  override var shouldShowNavigationBar: Bool {
    return shouldShowNavBar
  }
  
  override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    
    didDisappearRelay.accept(())
  }
  
  override func setupUI() {
    view.addSubviews(logoImageView,
                     titleLabel,
                     dotsView,
                     keyboardView)
  }
  
  override func setupLayout() {
    logoImageView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide).offset(60)
      $0.centerX.equalToSuperview()
      $0.keepRatio(for: logoImageView)
    }
    titleLabel.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalTo(dotsView.snp.top).offset(-45)
    }
    dotsView.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalTo(keyboardView.snp.top).offset(-28)
    }
    keyboardView.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview()
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .map { $0.stage == .confirmation }
      .drive(onNext: { [unowned self] in self.shouldShowNavBar = $0 })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.title }
      .bind(to: titleLabel.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.code.count }
      .bind(to: dotsView.rx.currentCount)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let addDigitDriver = keyboardView.rx.digitTapped
    let removeDigitDriver = keyboardView.rx.backTapped
    let didDisappearDriver = didDisappearRelay.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: PinCodePresenter.Input(addDigit: addDigitDriver,
                                                 removeDigit: removeDigitDriver,
                                                 didDisappear: didDisappearDriver))
  }
}
