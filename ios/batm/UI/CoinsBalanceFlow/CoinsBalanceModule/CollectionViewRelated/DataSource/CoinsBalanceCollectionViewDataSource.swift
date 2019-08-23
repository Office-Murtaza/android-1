import UIKit
import RxSwift
import RxCocoa

final class CoinsBalanceCollectionViewDataSource: NSObject, UICollectionViewDataSource, HasDisposeBag {
  
  let coinBalancesRelay = BehaviorRelay<[CoinBalance]>(value: [])
  let coinTap = PublishRelay<CoinBalance>()
  let footerTap = PublishRelay<Void>()
  
  private var values: [CoinBalance] = [] {
    didSet {
      collectionView?.reloadData()
    }
  }
  
  weak var collectionView: UICollectionView? {
    didSet {
      guard let collectionView = collectionView else { return }
      collectionView.register(CoinsBalanceCell.self)
      collectionView.registerFooter(CoinsBalanceFooterView.self)
      collectionView.reloadData()
      
      collectionView.rx.itemSelected
        .map { [unowned self] in self.values[$0.item] }
        .bind(to: coinTap)
        .disposed(by: disposeBag)
    }
  }
  
  override init() {
    super.init()
    
    setupBindings()
  }
  
  private func setupBindings() {
    coinBalancesRelay
      .subscribe(onNext: { [unowned self] in self.values = $0 })
      .disposed(by: disposeBag)
  }
  
  func numberOfSections(in collectionView: UICollectionView) -> Int {
    return 1
  }
  
  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return values.count
  }
  
  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let model = values[indexPath.item]
    let cell = collectionView.dequeueReusableCell(CoinsBalanceCell.self, for: indexPath)
    cell.configure(for: model)
    return cell
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      viewForSupplementaryElementOfKind kind: String,
                      at indexPath: IndexPath) -> UICollectionReusableView {
    let footerView = collectionView.dequeueFooter(CoinsBalanceFooterView.self, for: indexPath)
    footerView.configure(for: footerTap)
    return footerView
  }
}
