import UIKit

enum VerificationInfoRowType {
  case txLimit(Double)
  case dailyLimit(Double)
  case status(VerificationStatus)
}

class VerificationInfoRowView: UIView {
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .poppinsSemibold16
    return label
  }()
  
  let valueLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsMedium16
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.5
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
    
    addSubviews(titleLabel, valueLabel)
  }
  
  private func setupLayout() {
    titleLabel.snp.makeConstraints {
      $0.top.left.equalToSuperview()
    }
    titleLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
    titleLabel.setContentHuggingPriority(.required, for: .horizontal)
    
    valueLabel.snp.makeConstraints {
      $0.left.equalToSuperview().offset(110)
      $0.top.right.bottom.equalToSuperview()
    }
  }
  
  func configure(for type: VerificationInfoRowType) {
    switch type {
    case let .txLimit(limit):
      titleLabel.text = localize(L.VerificationInfo.TxLimitRow.title)
      valueLabel.text = limit.fiatSellFormatted.withUSD
      valueLabel.textColor = .warmGrey
    case let .dailyLimit(limit):
      titleLabel.text = localize(L.VerificationInfo.DailyLimitRow.title)
      valueLabel.text = limit.fiatSellFormatted.withUSD
      valueLabel.textColor = .warmGrey
    case let .status(status):
      titleLabel.text = localize(L.VerificationInfo.StatusRow.title)
      valueLabel.text = status.verboseValue
      valueLabel.textColor = status.associatedColor
    }
  }
}
