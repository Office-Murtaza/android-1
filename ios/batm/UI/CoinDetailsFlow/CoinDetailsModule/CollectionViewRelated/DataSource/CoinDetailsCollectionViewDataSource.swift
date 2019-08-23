import UIKit
import RxSwift
import RxCocoa

final class CoinDetailsCollectionViewDataSource: NSObject, UICollectionViewDataSource, HasDisposeBag, ItemsCountProvider {
  
  let transactionsRelay = BehaviorRelay<[Transaction]>(value: [])
  
  private var values: [Transaction] = [] {
    didSet {
      collectionView?.reloadData()
    }
  }
  
  weak var collectionView: UICollectionView? {
    didSet {
      guard let collectionView = collectionView else { return }
      collectionView.register(TransactionCell.self)
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
}
