import UIKit

extension UICollectionReusableView: ReusableView {}

extension UICollectionView {
  
  func register<T: UICollectionViewCell>(_ type: T.Type) {
    register(type, forCellWithReuseIdentifier: T.reuseIdentifier)
  }
  
  func registerHeader<T: UICollectionReusableView>(_ type: T.Type) {
    register(type,
             forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader,
             withReuseIdentifier: T.reuseIdentifier)
  }
  
  func registerFooter<T: UICollectionReusableView>(_ type: T.Type) {
    register(type,
             forSupplementaryViewOfKind: UICollectionView.elementKindSectionFooter,
             withReuseIdentifier: T.reuseIdentifier)
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
  
  func dequeueReusableSupplementaryView<T: UICollectionReusableView>(_ type: T.Type,
                                                                     ofKind kind: String,
                                                                     for indexPath: IndexPath) -> T {
    guard let cell = dequeueReusableSupplementaryView(ofKind: kind,
                                                      withReuseIdentifier: T.reuseIdentifier,
                                                      for: indexPath) as? T
      else {
        fatalError("Unable to dequeue CollectionReusableView: \(T.self) with identifier: \(T.reuseIdentifier)")
    }
    return cell
  }
  
  func dequeueHeader<T: UICollectionReusableView>(_ type: T.Type, for indexPath: IndexPath) -> T {
    return dequeueReusableSupplementaryView(type,
                                            ofKind: UICollectionView.elementKindSectionHeader,
                                            for: indexPath)
  }
  
  func dequeueFooter<T: UICollectionReusableView>(_ type: T.Type, for indexPath: IndexPath) -> T {
    return dequeueReusableSupplementaryView(type,
                                            ofKind: UICollectionView.elementKindSectionFooter,
                                            for: indexPath)
  }
}
