import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class UnlinkViewController: ModuleViewController<UnlinkPresenter> {
  
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
    label.text = localize(L.Unlink.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  let warningLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Unlink.warning)
    label.textColor = .slateGrey
    label.textAlignment = .center
    label.font = .poppinsMedium14
    label.numberOfLines = 0
    return label
  }()
  
  let unlinkButton: MainButton = {
    let button = MainButton()
    button.configure(for: .unlink)
    return button
  }()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.backgroundColor = .white
    
    view.addSubviews(backgroundImageView,
                     safeAreaContainer,
                     warningLabel,
                     unlinkButton)
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
    warningLabel.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(25)
    }
    unlinkButton.snp.makeConstraints {
      $0.top.equalTo(warningLabel.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(45)
    }
  }
  
  func setupUIBindings() {
    
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
    let unlinkDriver = unlinkButton.rx.tap.asDriver()
    
    presenter.bind(input: UnlinkPresenter.Input(back: backDriver,
                                                unlink: unlinkDriver))
  }
}
