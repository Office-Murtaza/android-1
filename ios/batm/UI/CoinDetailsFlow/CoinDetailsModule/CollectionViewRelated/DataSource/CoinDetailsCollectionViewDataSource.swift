import UIKit
import RxSwift
import RxCocoa

final class CoinDetailsCollectionViewDataSource: NSObject, UICollectionViewDataSource, HasDisposeBag, ItemsCountProvider {
  
  let transactionsRelay = BehaviorRelay<[Transaction]>(value: [])
  let headerViewConfigRelay = BehaviorRelay<CoinDetailsHeaderViewConfig?>(value: nil)
  
  fileprivate let didTapDepositRelay = PublishRelay<Void>()
  fileprivate let didTapWithdrawRelay = PublishRelay<Void>()
  fileprivate let didTapSendGiftRelay = PublishRelay<Void>()
  fileprivate let didTapSellRelay = PublishRelay<Void>()
  fileprivate let didSelectPeriodRelay = PublishRelay<SelectedPeriod>()
  
  private var values: [Transaction] = [] {
    didSet {
      collectionView?.reloadData()
    }
  }
  
  private var headerViewConfig: CoinDetailsHeaderViewConfig? {
    didSet {
      collectionView?.reloadData()
    }
  }
  
  weak var collectionView: UICollectionView? {
    didSet {
      guard let collectionView = collectionView else { return }
      collectionView.register(TransactionCell.self)
      collectionView.registerHeader(CoinDetailsHeaderView.self)
      collectionView.reloadData()
    }
  }
  
  override init() {
    super.init()
    
    setupBindings()
  }
  
  private func setupBindings() {
    transactionsRelay
      .subscribe(onNext: { [unowned self] in self.values = $0 })
      .disposed(by: disposeBag)
    
    headerViewConfigRelay
      .subscribe(onNext: { [unowned self] in self.headerViewConfig = $0 })
      .disposed(by: disposeBag)
  }
  
  func numberOfItems() -> Int {
    return values.count
  }
  
  func numberOfSections(in collectionView: UICollectionView) -> Int {
    return 1
  }
  
  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return values.count
  }
  
  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let model = values[indexPath.item]
    let cell = collectionView.dequeueReusableCell(TransactionCell.self, for: indexPath)
    cell.configure(for: model)
    return cell
  }
  
  func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
    guard kind == UICollectionView.elementKindSectionHeader else { fatalError() }
    
    let headerView = collectionView.dequeueHeader(CoinDetailsHeaderView.self, for: indexPath)
    headerView.delegate = self
    
    if let config = headerViewConfig {
      headerView.configure(with: config)
    }
    
    return headerView
  }
}

extension CoinDetailsCollectionViewDataSource: CoinDetailsHeaderViewDelegate {
  func didTapDeposit() {
    didTapDepositRelay.accept(())
  }
  
  func didTapWithdraw() {
    didTapWithdrawRelay.accept(())
  }
  
  func didTapSendGift() {
    didTapSendGiftRelay.accept(())
  }
  
  func didTapSell() {
    didTapSellRelay.accept(())
  }
  
  func didSelectPeriod(_ period: SelectedPeriod) {
    didSelectPeriodRelay.accept(period)
  }
}

extension Reactive where Base == CoinDetailsCollectionViewDataSource {
  var depositTap: Driver<Void> {
    return base.didTapDepositRelay.asDriver(onErrorDriveWith: .empty())
  }
  var withdrawTap: Driver<Void> {
    return base.didTapWithdrawRelay.asDriver(onErrorDriveWith: .empty())
  }
  var sendGiftTap: Driver<Void> {
    return base.didTapSendGiftRelay.asDriver(onErrorDriveWith: .empty())
  }
  var sellTap: Driver<Void> {
    return base.didTapSellRelay.asDriver(onErrorDriveWith: .empty())
  }
  var selectedPeriod: Driver<SelectedPeriod> {
    return base.didSelectPeriodRelay.asDriver(onErrorDriveWith: .empty())
  }
}
