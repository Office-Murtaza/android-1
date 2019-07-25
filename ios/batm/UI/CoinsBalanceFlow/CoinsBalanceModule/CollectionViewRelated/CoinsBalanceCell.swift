import UIKit

final class CoinsBalanceCell: UICollectionViewCell {
  
  let typeImageView = UIImageView(image: nil)
  
  let textContainer = UIView()
  
  let typeLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsBold13
    label.textColor = .slateGrey
    return label
  }()
  
  let priceLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsMedium12
    label.textColor = .greyishTwo
    return label
  }()
  
  let balanceLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsSemibold12
    label.textColor = .slateGrey
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
  
  override func prepareForReuse() {
    super.prepareForReuse()
    typeImageView.image = nil
    typeLabel.text = nil
    priceLabel.text = nil
    balanceLabel.text = nil
  }
  
  private func setupUI() {
    contentView.backgroundColor = .white
    contentView.layer.cornerRadius = 26
    contentView.layer.shadowColor = UIColor.black.cgColor
    contentView.layer.shadowOffset = CGSize(width: 0, height: 3)
    contentView.layer.shadowRadius = 10
    contentView.layer.shadowOpacity = 0.15
    
    contentView.addSubviews(typeImageView,
                            textContainer,
                            balanceLabel)
    textContainer.addSubviews(typeLabel,
                              priceLabel)
  }
  
  private func setupLayout() {
    typeImageView.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview().inset(20)
      $0.width.equalTo(typeImageView.snp.height)
    }
    textContainer.snp.makeConstraints {
      $0.left.equalTo(typeImageView.snp.right).offset(16)
      $0.centerY.equalToSuperview()
    }
    typeLabel.snp.makeConstraints {
      $0.top.left.equalToSuperview()
      $0.right.lessThanOrEqualToSuperview()
    }
    priceLabel.snp.makeConstraints {
      $0.top.equalTo(typeLabel.snp.bottom).offset(7)
      $0.left.bottom.equalToSuperview()
      $0.right.lessThanOrEqualToSuperview()
    }
    balanceLabel.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-20)
      $0.centerY.equalToSuperview()
    }
  }
  
  func configure(for model: CoinBalance) {
    typeImageView.image = model.type.logo
    typeLabel.text = model.type.verboseValue
    priceLabel.text = "USD \(model.price)"
    balanceLabel.text = "\(model.balance) \(model.type.code)"
  }
}
