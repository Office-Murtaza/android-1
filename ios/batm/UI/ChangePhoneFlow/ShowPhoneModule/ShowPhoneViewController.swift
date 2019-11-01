import UIKit
import RxSwift
import RxCocoa
import SnapKit

class ShowPhoneViewController: ModuleViewController<ShowPhonePresenter> {
  
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
    label.text = localize(L.ShowPhone.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  let phoneView: UIView = {
    let view = UIView()
    view.backgroundColor = .lightGold
    view.layer.cornerRadius = 16
    return view
  }()
  
  let phoneImageView = UIImageView(image: UIImage(named: "welcome_phone"))
  
  let phoneLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsMedium16
    return label
  }()
  
  let changeButton: MainButton = {
    let button = MainButton()
    button.configure(for: .change)
    return button
  }()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.backgroundColor = .white
    
    view.addSubviews(backgroundImageView,
                     safeAreaContainer,
                     phoneView,
                     phoneLabel,
                     changeButton)
    safeAreaContainer.addSubviews(backButton,
                                  titleLabel)
    phoneView.addSubview(phoneImageView)
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
    phoneView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom).offset(32)
      $0.left.equalTo(45)
      $0.size.equalTo(46)
    }
    phoneImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    phoneLabel.snp.makeConstraints {
      $0.centerY.equalTo(phoneView)
      $0.left.equalTo(phoneView.snp.right).offset(20)
    }
    changeButton.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(45)
      $0.top.equalTo(phoneView.snp.bottom).offset(32)
    }
  }
  
  private func setupUIBindings() {
    presenter.phoneNumberRelay
      .observeOn(MainScheduler.instance)
      .bind(to: phoneLabel.rx.text)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
    let changeDriver = changeButton.rx.tap.asDriver()
    
    presenter.bind(input: ShowPhonePresenter.Input(back: backDriver,
                                                   change: changeDriver))
  }
  
}
