import UIKit
import RxSwift
import RxCocoa

final class CreateEditTradeTypeView: UIView, HasDisposeBag {
  
  let acceptedTypeRelay = BehaviorRelay<TradeType>(value: .buy)
  
  let buyContainer = UIView()
  
  let buyCheckboxView = MaterialCheckBoxView()
  
  let buyTitleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Trades.buy)
    label.textColor = .warmGrey
    label.font = .systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  let sellContainer = UIView()
  
  let sellCheckboxView = MaterialCheckBoxView()
  
  let sellTitleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Trades.sell)
    label.textColor = .warmGrey
    label.font = .systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(buyContainer, sellContainer)
    
    buyContainer.addSubviews(buyCheckboxView,
                             buyTitleLabel)
    
    sellContainer.addSubviews(sellCheckboxView,
                              sellTitleLabel)
  }
  
  private func setupLayout() {
    buyContainer.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    sellContainer.snp.makeConstraints {
      $0.left.equalTo(buyContainer.snp.right).offset(15)
      $0.top.right.bottom.equalToSuperview()
    }
    buyCheckboxView.snp.makeConstraints {
      $0.left.centerY.equalToSuperview()
    }
    sellCheckboxView.snp.makeConstraints {
      $0.left.centerY.equalToSuperview()
    }
    buyTitleLabel.snp.makeConstraints {
      $0.left.equalTo(buyCheckboxView.snp.right).offset(10)
      $0.top.right.bottom.equalToSuperview()
    }
    sellTitleLabel.snp.makeConstraints {
      $0.left.equalTo(sellCheckboxView.snp.right).offset(10)
      $0.top.right.bottom.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    acceptedTypeRelay
      .distinctUntilChanged()
      .subscribe(onNext: { [unowned self] type in
        self.buyCheckboxView.set(accepted: type == .buy)
        self.sellCheckboxView.set(accepted: type == .sell)
      })
      .disposed(by: disposeBag)
    
    acceptedTypeRelay
      .map { $0 != .buy }
      .asObservable()
      .bind(to: buyCheckboxView.rx.isUserInteractionEnabled)
      .disposed(by: disposeBag)

    acceptedTypeRelay
      .map { $0 != .sell }
      .asObservable()
      .bind(to: sellCheckboxView.rx.isUserInteractionEnabled)
      .disposed(by: disposeBag)
    
    buyCheckboxView.rx.isAccepted
      .filter { $0 }
      .drive(onNext: { [acceptedTypeRelay] _ in acceptedTypeRelay.accept(.buy) })
      .disposed(by: disposeBag)
    
    sellCheckboxView.rx.isAccepted
      .filter { $0 }
      .drive(onNext: { [acceptedTypeRelay] _ in acceptedTypeRelay.accept(.sell) })
      .disposed(by: disposeBag)
  }
  
  func configure(for trade: BuySellTrade) {
    acceptedTypeRelay.accept(trade.type)
  }
}

extension Reactive where Base == CreateEditTradeTypeView {
  var acceptedType: Driver<TradeType> {
    return base.acceptedTypeRelay.asDriver()
  }
}
