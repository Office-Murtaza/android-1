import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

protocol FilterCoinsCellDelegate: class {
  func didTapChangeVisibility(_ coin: BTMCoin)
}

final class FilterCoinsCell: UICollectionViewCell {
  
  var disposeBag = DisposeBag()
  
  weak var delegate: FilterCoinsCellDelegate?
  
  let typeImageView = UIImageView(image: nil)
  
  let typeLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 16, weight: .medium)
    label.textColor = .slateGrey
    return label
  }()
  
  let visibilitySwitch = UISwitch()
  
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
    typeImageView.image = nil
    typeLabel.text = nil
    disposeBag = DisposeBag()
  }
  
  private func setupUI() {
    contentView.backgroundColor = .clear
    
    contentView.addSubviews(typeImageView,
                            typeLabel,
                            visibilitySwitch,
                            divider)
  }
  
  private func setupLayout() {
    typeImageView.snp.makeConstraints {
      $0.left.centerY.equalToSuperview()
    }
    typeLabel.snp.makeConstraints {
      $0.left.equalTo(typeImageView.snp.right).offset(20)
      $0.right.lessThanOrEqualTo(visibilitySwitch.snp.left).offset(-20)
      $0.centerY.equalToSuperview()
    }
    visibilitySwitch.snp.makeConstraints {
      $0.right.centerY.equalToSuperview()
    }
    divider.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview()
      $0.height.equalTo(1)
    }
  }
  
  func configure(for coin: BTMCoin) {
    typeImageView.image = coin.type.logo
    typeLabel.text = coin.type.verboseValue
    visibilitySwitch.isOn = coin.isVisible
    
    visibilitySwitch.rx.isOn
      .asDriver()
      .filter { coin.isVisible != $0 }
      .drive(onNext: { [unowned self] _ in self.delegate?.didTapChangeVisibility(coin) })
      .disposed(by: disposeBag)
  }
}
