import UIKit
import RxSwift
import RxCocoa

final class FilterCoinsCollectionViewDataSource: NSObject, UICollectionViewDataSource, HasDisposeBag {
  
  let coinsRelay = BehaviorRelay<[BTMCoin]>(value: [])
  let changeVisibilityRelay = PublishRelay<BTMCoin>()
  
  private var values: [BTMCoin] = [] {
    didSet {
      collectionView?.reloadData()
    }
  }
  
  weak var collectionView: UICollectionView? {
    didSet {
      guard let collectionView = collectionView else { return }
      collectionView.register(FilterCoinsCell.self)
      collectionView.reloadData()
    }
  }
  
  override init() {
    super.init()
    
    setupBindings()
  }
  
  private func setupBindings() {
    coinsRelay
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
    let cell = collectionView.dequeueReusableCell(FilterCoinsCell.self, for: indexPath)
    cell.delegate = self
    cell.configure(for: model)
    return cell
  }
}

extension FilterCoinsCollectionViewDataSource: FilterCoinsCellDelegate {
  func didTapChangeVisibility(_ coin: BTMCoin) {
    changeVisibilityRelay.accept(coin)
  }
}
