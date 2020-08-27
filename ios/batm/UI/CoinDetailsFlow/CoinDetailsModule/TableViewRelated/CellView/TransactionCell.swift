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
  
  let dateLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 13)
    label.textColor = .slateGrey
    label.textAlignment = .center
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
    return label
  }()
  
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
    stackView.addArrangedSubviews(dateLabel,
                                  typeLabel,
                                  statusViewContainer,
                                  amountLabel)
    statusViewContainer.addSubview(statusView)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    statusView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
  }
  
  func configure(for model: Transaction) {
    dateLabel.text = model.dateString
    typeLabel.text = model.type.verboseValue
    amountLabel.text = model.amount.coinFormatted
    
    statusView.configure(text: model.status.verboseValue, color: model.status.associatedColor)
  }
}
