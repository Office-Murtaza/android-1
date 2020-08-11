import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class ErrorViewController: ModuleViewController<ErrorPresenter> {
  
  let topSpacer = UIView()
  
  let imageView = UIImageView(image: nil)
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .systemFont(ofSize: 18, weight: .bold)
    return label
  }()
  
  let subtitleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .systemFont(ofSize: 16)
    return label
  }()
  
  let bottomSpacer = UIView()
  
  let actionButton = MDCButton.contained
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.backgroundColor = .white
    
    view.addSubviews(topSpacer,
                     imageView,
                     titleLabel,
                     subtitleLabel,
                     bottomSpacer,
                     actionButton)
  }

  override func setupLayout() {
    topSpacer.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    imageView.snp.makeConstraints {
      $0.top.equalTo(topSpacer.snp.bottom)
      $0.centerX.equalToSuperview()
    }
    titleLabel.snp.makeConstraints {
      $0.top.equalTo(imageView.snp.bottom).offset(10)
      $0.centerX.equalToSuperview()
    }
    subtitleLabel.snp.makeConstraints {
      $0.top.equalTo(imageView.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
      $0.width.equalTo(215)
    }
    bottomSpacer.snp.makeConstraints {
      $0.top.equalTo(subtitleLabel.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(topSpacer).multipliedBy(2)
    }
    actionButton.snp.makeConstraints {
      $0.top.equalTo(bottomSpacer.snp.bottom)
      $0.left.right.equalToSuperview().inset(15)
      $0.height.equalTo(50)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  func setupUIBindings() {
    switch presenter.type! {
    case .serverError:
      imageView.image = UIImage(named: "server_error")
      titleLabel.text = localize(L.Error.ServerError.title)
      subtitleLabel.text = localize(L.Error.ServerError.subtitle)
      actionButton.setTitle(localize(L.Shared.Button.goBack), for: .normal)
    case .somethingWentWrong:
      imageView.image = UIImage(named: "something_went_wrong")
      titleLabel.text = localize(L.Error.SomethingWentWrong.title)
      subtitleLabel.text = localize(L.Error.SomethingWentWrong.subtitle)
      actionButton.setTitle(localize(L.Shared.Button.goBack), for: .normal)
    case .noConnection:
      imageView.image = UIImage(named: "no_connection")
      titleLabel.text = localize(L.Error.NoConnection.title)
      subtitleLabel.text = localize(L.Error.NoConnection.subtitle)
      actionButton.setTitle(localize(L.Shared.Button.retry), for: .normal)
    }
  }

  override func setupBindings() {
    setupUIBindings()
    
    let actionDriver = actionButton.rx.tap.asDriver()
    
    presenter.bind(input: ErrorPresenter.Input(action: actionDriver))
  }
}
