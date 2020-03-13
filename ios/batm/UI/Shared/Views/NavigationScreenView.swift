import Foundation
import UIKit
import SnapKit
import RxSwift

class NavigationScreenView: UIView, HasDisposeBag {
  
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
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let rootScrollView: UIScrollView = {
    let scrollView = UIScrollView()
    scrollView.bounces = false
    scrollView.keyboardDismissMode = .interactive
    return scrollView
  }()
  
  let contentView = UIView()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBehavior()
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
       rootScrollView.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: keyboardHeight, right: 0)
     }
     
     rootScrollView.scrollIndicatorInsets = rootScrollView.contentInset
   }
  
  private func setupUI() {
    registerForKeyboardNotifications()
    
    backgroundColor = .white
    
    addSubviews(rootScrollView,
                backgroundImageView,
                safeAreaContainer)
    
    safeAreaContainer.addSubviews(backButton, titleLabel)
    
    rootScrollView.addSubview(contentView)
    rootScrollView.addGestureRecognizer(tapRecognizer)
  }
  
  private func setupLayout() {
    backgroundImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.bottom.equalTo(safeAreaLayoutGuide.snp.top).offset(44)
    }
    safeAreaContainer.snp.makeConstraints {
      $0.left.right.bottom.equalTo(backgroundImageView)
      $0.top.equalTo(safeAreaLayoutGuide)
    }
    backButton.snp.makeConstraints {
      $0.centerY.equalTo(titleLabel)
      $0.left.equalToSuperview().offset(15)
      $0.size.equalTo(45)
    }
    titleLabel.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom)
      $0.left.right.bottom.equalToSuperview()
    }
    contentView.snp.makeConstraints {
      $0.edges.equalToSuperview()
      $0.width.equalToSuperview()
    }
  }
  
  private func setupBehavior() {
    tapRecognizer.rx.event.asDriver().map { _ in () }
      .drive(onNext: { [unowned self] in self.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  func setTitle(_ title: String?) {
    titleLabel.text = title
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}
