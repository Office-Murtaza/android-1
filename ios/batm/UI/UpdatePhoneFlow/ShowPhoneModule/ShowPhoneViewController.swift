import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class ShowPhoneViewController: ModuleViewController<ShowPhonePresenter> {
  
  let phoneBackgroundView: UIView = {
    let view = UIView()
    view.backgroundColor = .duckEggBlue
    view.layer.cornerRadius = 25
    return view
  }()
  
  let phoneImageView = UIImageView(image: UIImage(named: "security_phone"))
  
  let phoneNumberLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .systemFont(ofSize: 20, weight: .medium)
    return label
  }()
  
  let updateButton = MDCButton.update
  
  override func setupUI() {
    title = localize(L.ShowPhone.title)
    
    view.addSubviews(phoneBackgroundView,
                     phoneNumberLabel,
                     updateButton)
    phoneBackgroundView.addSubview(phoneImageView)
  }
  
  override func setupLayout() {
    phoneBackgroundView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide).offset(30)
      $0.centerX.equalToSuperview()
      $0.size.equalTo(50)
    }
    phoneImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    phoneNumberLabel.snp.makeConstraints {
      $0.top.equalTo(phoneBackgroundView.snp.bottom).offset(23)
      $0.centerX.equalToSuperview()
    }
    updateButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func setupUIBindings() {
    phoneNumberLabel.text = presenter.formattedPhoneNumber
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let updateDriver = updateButton.rx.tap.asDriver()
    
    presenter.bind(input: ShowPhonePresenter.Input(update: updateDriver))
  }
  
}
