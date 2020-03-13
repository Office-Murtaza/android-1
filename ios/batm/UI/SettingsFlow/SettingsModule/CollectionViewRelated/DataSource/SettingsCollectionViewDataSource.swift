import UIKit
import RxSwift
import RxCocoa

final class SettingsCollectionViewDataSource: NSObject, UICollectionViewDataSource, HasDisposeBag {
  
  var values: [SettingsCellType] = [] {
    didSet {
      collectionView?.reloadData()
    }
  }
  
  weak var collectionView: UICollectionView? {
    didSet {
      guard let collectionView = collectionView else { return }
      collectionView.register(SettingsCell.self)
      collectionView.reloadData()
    }
  }
  
  func numberOfSections(in collectionView: UICollectionView) -> Int {
    return 1
  }
  
  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return values.count
  }
  
  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let type = values[indexPath.item]
    let cell = collectionView.dequeueReusableCell(SettingsCell.self, for: indexPath)
    cell.configure(for: type)
    return cell
  }
}
