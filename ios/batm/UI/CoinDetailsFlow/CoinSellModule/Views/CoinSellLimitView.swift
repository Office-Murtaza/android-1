import UIKit
import RxSwift
import RxCocoa

class CoinSellLimitView: UIView {
  
  let dailyLimitLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinSell.dailyLimit)
    label.textColor = .slateGrey
    label.font = .poppinsBold16
    return label
  }()
  
  let dailyLimitValueLabel: UILabel = {
    let label = UILabel()
    label.textColor = .greyishTwo
    label.font = .poppinsMedium14
    return label
  }()
  
  let transactionLimitLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinSell.transactionLimit)
    label.textColor = .slateGrey
    label.font = .poppinsBold16
    return label
  }()
  
  let transactionLimitValueLabel: UILabel = {
    let label = UILabel()
    label.textColor = .greyishTwo
    label.font = .poppinsMedium14
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
    
    addSubviews(dailyLimitLabel,
                dailyLimitValueLabel,
                transactionLimitLabel,
                transactionLimitValueLabel)
  }
  
  private func setupLayout() {
    dailyLimitLabel.snp.makeConstraints {
      $0.top.centerX.equalToSuperview()
    }
    dailyLimitValueLabel.snp.makeConstraints {
      $0.top.equalTo(dailyLimitLabel.snp.bottom).offset(10)
      $0.centerX.equalToSuperview()
    }
    transactionLimitLabel.snp.makeConstraints {
      $0.top.equalTo(dailyLimitValueLabel.snp.bottom).offset(25)
      $0.centerX.equalToSuperview()
    }
    transactionLimitValueLabel.snp.makeConstraints {
      $0.top.equalTo(transactionLimitLabel.snp.bottom).offset(10)
      $0.centerX.bottom.equalToSuperview()
    }
  }
}

extension Reactive where Base == CoinSellLimitView {
  var limits: Binder<SellDetails> {
    return Binder(base) { target, value in
      target.dailyLimitValueLabel.text = "\(value.dailyLimit.fiatFormatted) USD"
      target.transactionLimitValueLabel.text = "\(value.transactionLimit.fiatFormatted) USD"
    }
  }
}
