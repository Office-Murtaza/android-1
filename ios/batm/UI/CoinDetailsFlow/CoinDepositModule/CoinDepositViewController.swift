import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinDepositViewController: NavigationScreenViewController<CoinDepositPresenter> {
  
  let qrCodeContainer: UIView = {
    let view = UIView()
    view.backgroundColor = .white
    view.layer.cornerRadius = 4
    view.layer.shadowColor = UIColor.black.cgColor
    view.layer.shadowOffset = CGSize(width: 0, height: 0)
    view.layer.shadowRadius = 5
    view.layer.shadowOpacity = 0.2
    return view
  }()
  
  let qrCodeImageView = UIImageView(image: nil)
  
  let addressLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .systemFont(ofSize: 12, weight: .medium)
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.5
    return label
  }()
  
  let copyButton = MDCButton.copy
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.setTitle(String(format: localize(L.CoinDeposit.title), presenter.coin.type.code))
    
    customView.contentView.addSubviews(qrCodeContainer,
                                       copyButton)
    
    qrCodeContainer.addSubviews(qrCodeImageView,
                                addressLabel)
  }

  override func setupLayout() {
    customView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
    qrCodeContainer.snp.makeConstraints {
      $0.top.left.right.equalToSuperview().inset(30)
    }
    qrCodeImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview().inset(10)
      $0.width.equalTo(qrCodeImageView.snp.height)
    }
    addressLabel.snp.makeConstraints {
      $0.top.equalTo(qrCodeImageView.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview().offset(20)
      $0.right.lessThanOrEqualToSuperview().offset(-20)
      $0.bottom.equalToSuperview().inset(25)
    }
    copyButton.snp.makeConstraints {
      $0.top.equalTo(qrCodeContainer.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
      $0.width.equalTo(100)
    }
  }
  
  func setupUIBindings() {
    qrCodeImageView.image = UIImage.qrCode(from: presenter.coin.publicKey)
    addressLabel.text = presenter.coin.publicKey
    
    copyButton.rx.tap.asDriver()
      .drive(onNext: { [unowned self] in self.view.makeToast(localize(L.Shared.copied)) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let copyDriver = copyButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinDepositPresenter.Input(back: backDriver,
                                                     copy: copyDriver))
  }
}
