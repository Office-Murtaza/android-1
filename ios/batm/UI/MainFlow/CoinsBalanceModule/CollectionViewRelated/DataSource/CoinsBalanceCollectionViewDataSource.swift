import UIKit
import RxSwift
import RxCocoa

final class CoinsBalanceCollectionViewDataSource: NSObject, UICollectionViewDataSource, HasDisposeBag {
  
  let coinBalancesRelay = BehaviorRelay<[CoinBalance]>(value: [])
  
  private var values: [CoinBalance] = [] {
    didSet {
      collectionView?.reloadData()
    }
  }
  
  weak var collectionView: UICollectionView? {
    didSet {
      guard let collectionView = collectionView else { return }
      collectionView.register(CoinsBalanceCell.self)
      collectionView.reloadData()
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
}
