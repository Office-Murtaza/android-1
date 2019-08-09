import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class ___FILEBASENAME___: ModuleViewController<___VARIABLE_moduleName___Presenter> {
  
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
    label.text = localize(L.Settings.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.backgroundColor = .white
    
    view.addSubviews(backgroundImageView,
                     safeAreaContainer)
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
  }
  
  func setupUIBindings() {
    
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
    
    presenter.bind(input: ___VARIABLE_moduleName___Presenter.Input(back: backDriver))
  }
}
