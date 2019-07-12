import UIKit

extension UICollectionViewCell: ReusableView {}

extension UICollectionView {
  
  func register<T: UICollectionViewCell>(_ type: T.Type) {
    register(type, forCellWithReuseIdentifier: T.reuseIdentifier)
  }
  
  func dequeueReusableCell<T: UICollectionViewCell>(for indexPath: IndexPath) -> T {
    return dequeueReusableCell(T.self, for: indexPath)
  }
  
  func dequeueReusableCell<T: UICollectionViewCell>(_ type: T.Type, for indexPath: IndexPath) -> T {
    guard let cell = dequeueReusableCell(withReuseIdentifier: T.reuseIdentifier, for: indexPath) as? T else {
      fatalError("Unable to dequeue CollectionViewCell: \(T.self) with identifier: \(T.reuseIdentifier)")
    }
    return cell
  }
}
