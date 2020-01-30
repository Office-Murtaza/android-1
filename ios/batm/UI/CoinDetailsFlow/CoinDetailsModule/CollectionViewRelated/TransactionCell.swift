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
  
  let statusViewContainer = UIView()
  
  let statusView = TransactionStatusView()
  
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
    amountLabel.text = nil
    
    statusView.reset()
  }
  
  private func setupUI() {
    contentView.backgroundColor = .clear
    
    contentView.addSubviews(stackView,
                            divider)
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
    divider.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview()
      $0.height.equalTo(1)
    }
  }
  
  func configure(for model: Transaction) {
    dateLabel.text = model.dateString
    typeLabel.text = model.type.verboseValue.uppercased()
    amountLabel.text = "\(model.amount.coinFormatted)"
    
    statusView.configure(for: model.status)
  }
}
