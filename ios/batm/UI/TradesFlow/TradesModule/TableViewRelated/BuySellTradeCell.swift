import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class BuySellTradeCell: UITableViewCell {
  
  let topContainer = UIView()
  let bottomContainer = UIView()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 13, weight: .medium)
    label.textColor = .slateGrey
    return label
  }()
  
  let subtitleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = .warmGrey
    return label
  }()
  
  let priceLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12, weight: .medium)
    label.textColor = .ceruleanBlue
    return label
  }()
  
  let limitsLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = .warmGrey
    return label
  }()
  
  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override func prepareForReuse() {
    super.prepareForReuse()
    titleLabel.text = nil
    subtitleLabel.text = nil
    priceLabel.text = nil
    limitsLabel.text = nil
  }
  
  private func setupUI() {
    backgroundColor = .clear
    contentView.backgroundColor = .clear
    
    contentView.addSubviews(topContainer,
                            bottomContainer)
    
    topContainer.addSubviews(titleLabel, priceLabel)
    bottomContainer.addSubviews(subtitleLabel, limitsLabel)
  }
  
  private func setupLayout() {
    topContainer.snp.makeConstraints {
      $0.top.equalToSuperview().offset(13)
      $0.left.right.equalToSuperview().inset(15)
    }
    bottomContainer.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-13)
    }
    titleLabel.snp.makeConstraints {
      $0.left.top.bottom.equalToSuperview()
      $0.right.lessThanOrEqualTo(priceLabel.snp.left).offset(-15)
    }
    priceLabel.snp.makeConstraints {
      $0.right.centerY.equalToSuperview()
    }
    priceLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
    subtitleLabel.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
      $0.right.lessThanOrEqualTo(limitsLabel.snp.left).offset(-15)
    }
    limitsLabel.snp.makeConstraints {
      $0.right.centerY.equalToSuperview()
    }
    limitsLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
  }
  
  func configure(for trade: BuySellTrade) {
    var distance = ""
    if let tradeDistance = trade.distance {
      distance = ", \(tradeDistance)km"
    }
    
    titleLabel.text = "\(trade.username) (\(trade.tradeCount), \(trade.tradeRate)\(distance))"
    subtitleLabel.text = trade.paymentMethod
    priceLabel.text = "\(trade.price) USD"
    limitsLabel.text = "\(trade.minLimit) - \(trade.maxLimit) USD"
  }
}
