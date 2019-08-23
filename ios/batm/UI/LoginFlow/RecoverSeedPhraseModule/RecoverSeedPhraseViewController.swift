import UIKit
import RxSwift
import RxCocoa
import SnapKit

class RecoverSeedPhraseViewController: ModuleViewController<RecoverSeedPhrasePresenter> {
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let rootScrollView: UIScrollView = {
    let scrollView = UIScrollView()
    scrollView.bounces = false
    scrollView.contentInsetAdjustmentBehavior = .never
    scrollView.keyboardDismissMode = .interactive
    return scrollView
  }()
  
  let contentView = UIView()
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Recover.title)
    label.textColor = .white
    label.font = .poppinsSemibold22
    return label
  }()
  
  let separatorView = GoldSeparatorView()
  
  let mainView = RecoverSeedPhraseView()
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  private func registerForKeyboardNotifications() {
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(adjustForKeyboard),
                                           name: UIResponder.keyboardWillShowNotification,
                                           object: nil)
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(adjustForKeyboard),
                                           name: UIResponder.keyboardWillHideNotification,
                                           object: nil)
  }
  
  @objc private func adjustForKeyboard(notification: Notification) {
    guard let keyboardValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue else { return }
    
    let keyboardHeight = keyboardValue.cgRectValue.size.height
    
    if notification.name == UIResponder.keyboardWillHideNotification {
      rootScrollView.contentInset = .zero
    } else {
      rootScrollView.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: keyboardHeight + 15, right: 0)
    }
    
    rootScrollView.scrollIndicatorInsets = rootScrollView.contentInset
  }
  
  override func setupUI() {
    registerForKeyboardNotifications()
    
    view.backgroundColor = .whiteTwo
    
    view.addSubview(rootScrollView)
    rootScrollView.addSubview(contentView)
    contentView.addSubviews(backgroundImageView,
                     titleLabel,
                     separatorView,
                     mainView)
    contentView.addGestureRecognizer(tapRecognizer)
  }
  
  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    contentView.snp.makeConstraints {
      $0.edges.equalToSuperview()
      $0.size.equalToSuperview()
    }
    backgroundImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(187)
    }
    titleLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(63)
      $0.centerX.equalToSuperview()
    }
    separatorView.snp.makeConstraints {
      $0.top.equalTo(titleLabel.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
    }
    mainView.snp.makeConstraints {
      $0.top.equalTo(separatorView.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-30)
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.validationState }
      .map { validationState -> String? in
        switch validationState {
        case .valid, .unknown: return nil
        case let .invalid(message): return message
        }
      }
      .bind(to: mainView.rx.error)
      .disposed(by: disposeBag)
    
    presenter.seedPhraseWordsRelay
      .observeOn(MainScheduler.instance)
      .subscribe(onNext: { [mainView] in mainView.configure(for: $0) })
      .disposed(by: disposeBag)
    
    tapRecognizer.rx.event.asDriver()
      .map { _ in () }
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let pasteDriver = mainView.rx.pasteTap
    let doneDriver = mainView.rx.doneTap
    presenter.bind(input: RecoverSeedPhrasePresenter.Input(paste: pasteDriver,
                                                           done: doneDriver))
  }
}
