import UIKit
import RxSwift
import RxCocoa

class CoinDetailsDepositView: UIView {
  
  let topContainer: UIView = {
    let view = UIView()
    view.backgroundColor = .whiteFour
    return view
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsSemibold20
    return label
  }()
  
  let closeButton: UIButton = {
    let button = UIButton(type: .system)
    let image = UIImage(named: "welcome_close")!.withRenderingMode(.alwaysOriginal)
    button.setImage(image, for: .normal)
    return button
  }()
  
  let qrCodeImageView = UIImageView(image: nil)
  
  let addressLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.address)
    label.textColor = .warmGrey
    label.font = .poppinsMedium14
    return label
  }()
  
  let addressValueLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsSemibold14
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.4
    return label
  }()
  
  let copyLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .copy)
    return label
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    backgroundColor = .white
    layer.cornerRadius = 33
    layer.masksToBounds = true
    
    addSubviews(topContainer,
                qrCodeImageView,
                addressLabel,
                addressValueLabel,
                copyLabel)
    topContainer.addSubviews(titleLabel,
                             closeButton)
  }
  
  private func setupLayout() {
    topContainer.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(80)
    }
    titleLabel.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    closeButton.snp.makeConstraints {
      $0.centerY.equalToSuperview()
      $0.right.equalToSuperview().offset(-25)
    }
    qrCodeImageView.snp.makeConstraints {
      $0.top.equalTo(topContainer.snp.bottom).offset(25)
      $0.centerX.equalToSuperview()
    }
    addressLabel.snp.makeConstraints {
      $0.top.equalTo(qrCodeImageView.snp.bottom).offset(25)
      $0.centerX.equalToSuperview()
    }
    addressValueLabel.snp.makeConstraints {
      $0.top.equalTo(addressLabel.snp.bottom).offset(10)
      $0.left.greaterThanOrEqualToSuperview().offset(25)
      $0.right.lessThanOrEqualToSuperview().offset(-25)
      $0.centerX.equalToSuperview()
    }
    copyLabel.snp.makeConstraints {
      $0.top.equalTo(addressValueLabel.snp.bottom).offset(10)
      $0.centerX.equalToSuperview()
      $0.bottom.equalToSuperview().offset(-30)
    }
  }
  
  func configure(for coin: BTMCoin) {
    titleLabel.text = String(format: localize(L.CoinDeposit.title), coin.type.code)
    qrCodeImageView.image = UIImage.qrCode(from: coin.address)
    addressValueLabel.text = coin.address
  }
}

extension Reactive where Base == CoinDetailsDepositView {
  var copyTap: Driver<String?> {
    return base.copyLabel.rx.tap
      .map { [base] in base.addressValueLabel.text }
  }
}

