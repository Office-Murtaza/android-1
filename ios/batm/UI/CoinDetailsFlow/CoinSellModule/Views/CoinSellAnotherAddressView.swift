import UIKit
import RxSwift
import RxCocoa

class CoinSellAnotherAddressView: UIView {
  
  let checkboxView = CheckboxView()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinSell.sellFromAnother)
    label.textColor = .slateGrey
    label.font = .poppinsRegular12
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
    
    addSubviews(checkboxView, titleLabel)
  }
  
  private func setupLayout() {
    checkboxView.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    titleLabel.snp.makeConstraints {
      $0.left.equalTo(checkboxView.snp.right).offset(10)
      $0.right.centerY.equalToSuperview()
    }
  }
}

extension Reactive where Base == CoinSellAnotherAddressView {
  var isAccepted: Driver<Bool> {
    return base.checkboxView.rx.isAccepted
  }
}
