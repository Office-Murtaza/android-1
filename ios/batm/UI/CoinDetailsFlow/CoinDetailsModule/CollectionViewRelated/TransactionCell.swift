import UIKit
import RxSwift
import RxCocoa

final class TransactionCell: UICollectionViewCell {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.distribution = .fillEqually
    stackView.alignment = .center
    stackView.spacing = 10
    return stackView
  }()
  
  let dateLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsSemibold10
    label.textColor = .slateGrey
    label.textAlignment = .center
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
    return label
  }()
  
  let typeLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsSemibold10
    label.textColor = .slateGrey
    label.textAlignment = .center
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
    return label
  }()
  
  let statusView = UIView()
  
  let coloredStatusView: UIView = {
    let view = UIView()
    view.layer.cornerRadius = 3
    return view
  }()
  
  let statusLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsSemibold10
    label.textColor = .white
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
    return label
  }()
  
  let amountLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsSemibold10
    label.textColor = .slateGrey
    label.textAlignment = .center
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
    return label
  }()
  
  let divider: UIView = {
    let view = UIView()
    view.backgroundColor = UIColor.black.withAlphaComponent(0.1)
    return view
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
    dateLabel.text = nil
    typeLabel.text = nil
    statusLabel.text = nil
    amountLabel.text = nil
  }
  
  private func setupUI() {
    contentView.backgroundColor = .clear
    
    contentView.addSubviews(stackView,
                            divider)
    stackView.addArrangedSubviews(dateLabel,
                                  typeLabel,
                                  statusView,
                                  amountLabel)
    statusView.addSubview(coloredStatusView)
    coloredStatusView.addSubview(statusLabel)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    coloredStatusView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    statusLabel.snp.makeConstraints {
      $0.top.bottom.equalToSuperview().inset(2)
      $0.left.right.equalToSuperview().inset(5)
    }
    divider.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview()
      $0.height.equalTo(1)
    }
  }
  
  func configure(for model: Transaction) {
    dateLabel.text = model.dateString
    typeLabel.text = model.type.verboseValue.uppercased()
    statusLabel.text = model.status.verboseValue
    coloredStatusView.backgroundColor = model.status.associatedColor
    amountLabel.text = "\(model.amount.coinFormatted)"
  }
}
