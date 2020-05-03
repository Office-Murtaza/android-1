import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class QRCodeCardView: UIView {
  
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
    layer.cornerRadius = 4
    layer.shadowColor = UIColor.black.cgColor
    layer.shadowOffset = CGSize(width: 0, height: 0)
    layer.shadowRadius = 5
    layer.shadowOpacity = 0.2
    
    addSubviews(qrCodeImageView,
                addressLabel,
                copyButton)
  }
  
  private func setupLayout() {
    qrCodeImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview().inset(10)
      $0.width.equalTo(qrCodeImageView.snp.height)
    }
    addressLabel.snp.makeConstraints {
      $0.top.equalTo(qrCodeImageView.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview().offset(20)
      $0.right.lessThanOrEqualToSuperview().offset(-20)
    }
    copyButton.snp.makeConstraints {
      $0.top.equalTo(addressLabel.snp.bottom).offset(5)
      $0.bottom.equalToSuperview().inset(5)
      $0.centerX.equalToSuperview()
    }
  }
  
  func configure(for address: String) {
    qrCodeImageView.image = UIImage.qrCode(from: address)
    addressLabel.text = address
  }
}

extension Reactive where Base == QRCodeCardView {
  var copy: Driver<Void> {
    return base.copyButton.rx.tap.asDriver()
  }
}
