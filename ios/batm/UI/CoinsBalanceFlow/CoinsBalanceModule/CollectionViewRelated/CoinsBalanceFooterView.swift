import UIKit
import RxSwift
import RxCocoa

final class CoinsBalanceFooterView: UICollectionReusableView {
  
  struct C {
    static let plusHeight: CGFloat = 33
  }
  
  var disposeBag = DisposeBag()
  
  let container = UIView()
  
  let dummyButton = DummyButton()
  
  let plusView: UIView = {
    let view = UIView()
    view.backgroundColor = .ceruleanBlue
    view.layer.cornerRadius = C.plusHeight / 2
    return view
  }()
  
  let plusImageView = UIImageView(image: UIImage(named: "plus"))
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsSemibold13
    label.textColor = .ceruleanBlue
    label.text = localize(L.CoinsBalance.Footer.title)
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
  
  override func prepareForReuse() {
    super.prepareForReuse()
    disposeBag = DisposeBag()
  }
  
  private func setupUI() {
    backgroundColor = .clear
    
    addSubviews(container,
                dummyButton)
    container.addSubviews(plusView,
                          titleLabel)
    plusView.addSubview(plusImageView)
  }
  
  private func setupLayout() {
    container.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    dummyButton.snp.makeConstraints {
      $0.edges.equalTo(container)
    }
    plusView.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
      $0.size.equalTo(33)
    }
    plusImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    titleLabel.snp.makeConstraints {
      $0.left.equalTo(plusView.snp.right).offset(10)
      $0.centerY.right.equalToSuperview()
    }
  }
  
  func configure(for tapRelay: PublishRelay<Void>) {
    dummyButton.rx.tap
      .asDriver()
      .drive(onNext: { tapRelay.accept(()) })
      .disposed(by: disposeBag)
  }
}
