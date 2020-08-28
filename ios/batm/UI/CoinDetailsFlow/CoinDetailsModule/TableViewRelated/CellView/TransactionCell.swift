import UIKit
import RxSwift
import RxCocoa

final class TransactionCell: UITableViewCell {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.distribution = .fillEqually
    stackView.alignment = .center
    stackView.spacing = 10
    return stackView
  }()
  
  let dateContainer = UIView()
  
  let dateLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 13)
    label.textColor = .slateGrey
    label.textAlignment = .center
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
    return label
  }()
  
  let typeContainer = UIView()
  
  let typeLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 13)
    label.textColor = .warmGrey
    label.textAlignment = .center
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
    return label
  }()
  
  let statusViewContainer = UIView()
  
  let statusView = StatusView()
  
  let amountContainer = UIView()
  
  let amountLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 13)
    label.textColor = .warmGrey
    label.textAlignment = .center
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
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
    dateLabel.text = nil
    typeLabel.text = nil
    amountLabel.text = nil
  }
  
  private func setupUI() {
    contentView.addSubviews(stackView)
    stackView.addArrangedSubviews(dateContainer,
                                  typeContainer,
                                  statusViewContainer,
                                  amountContainer)
    dateContainer.addSubview(dateLabel)
    typeContainer.addSubview(typeLabel)
    statusViewContainer.addSubview(statusView)
    amountContainer.addSubview(amountLabel)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.top.bottom.equalToSuperview()
      $0.left.right.equalToSuperview().inset(15)
    }
    dateLabel.snp.makeConstraints {
      $0.left.centerY.equalToSuperview()
      $0.right.lessThanOrEqualToSuperview()
    }
    typeLabel.snp.makeConstraints {
      $0.center.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview()
      $0.right.lessThanOrEqualToSuperview()
    }
    statusView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    amountLabel.snp.makeConstraints {
      $0.right.centerY.equalToSuperview()
      $0.left.greaterThanOrEqualToSuperview()
    }
  }
  
  func configure(for model: Transaction) {
    dateLabel.text = model.dateString
    typeLabel.text = model.type.verboseValue
    amountLabel.text = model.amount.coinFormatted
    
    statusView.configure(text: model.status.verboseValue, color: model.status.associatedColor)
  }
}
