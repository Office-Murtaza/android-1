import UIKit
import RxSwift
import RxCocoa

final class FilterCoinsCell: UICollectionViewCell {
  
  var disposeBag = DisposeBag()
  
  let typeImageView = UIImageView(image: nil)
  
  let typeLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsBold13
    label.textColor = .slateGrey
    return label
  }()
  
  let visibilityView: UIView = {
    let view = UIView()
    view.layer.cornerRadius = 8
    return view
  }()
  
  let visibilityTitle: UILabel = {
    let label = UILabel()
    label.font = .poppinsSemibold13
    label.textColor = .white
    return label
  }()
  
  let dummyButton = DummyButton()
  
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
                            visibilityView,
                            dummyButton,
                            divider)
    visibilityView.addSubview(visibilityTitle)
  }
  
  private func setupLayout() {
    typeImageView.snp.makeConstraints {
      $0.left.equalToSuperview()
      $0.top.bottom.equalToSuperview().inset(16)
      $0.width.equalTo(typeImageView.snp.height)
    }
    typeLabel.snp.makeConstraints {
      $0.left.equalTo(typeImageView.snp.right).offset(16)
      $0.right.lessThanOrEqualTo(visibilityView.snp.left).offset(-10)
      $0.centerY.equalToSuperview()
    }
    visibilityView.snp.makeConstraints {
      $0.centerY.right.equalToSuperview()
      $0.height.equalTo(30)
    }
    visibilityTitle.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(10)
      $0.centerY.equalToSuperview()
    }
    dummyButton.snp.makeConstraints {
      $0.edges.equalTo(visibilityView)
    }
    divider.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview()
      $0.height.equalTo(1)
    }
  }
  
  func configure(for model: BTMCoin, tapRelay: PublishRelay<BTMCoin>) {
    typeImageView.image = model.type.logo
    typeLabel.text = model.type.verboseValue
    
    visibilityView.backgroundColor = model.isVisible ? .warmGreyThree : .ceruleanBlue
    visibilityTitle.text = model.isVisible ? localize(L.FilterCoins.hide) : localize(L.FilterCoins.show)
    
    
    
    dummyButton.rx.tap
      .asDriver()
      .drive(onNext: { tapRelay.accept(model) })
      .disposed(by: disposeBag)
  }
}
