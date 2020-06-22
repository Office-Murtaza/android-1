import UIKit
import RxSwift
import RxCocoa
import SnapKit

class FilterCoinsViewController: NavigationScreenViewController<FilterCoinsPresenter>, UICollectionViewDelegateFlowLayout {
  
  var dataSource: FilterCoinsCollectionViewDataSource!
  
  let collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    layout.minimumLineSpacing = 0
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
    return collectionView
  }()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.backgroundColor = .white
    
    customView.setTitle(localize(L.FilterCoins.title))
    customView.addSubviews(collectionView)
    
    collectionView.backgroundColor = .clear
    collectionView.delegate = self
  }
  
  override func setupLayout() {
    collectionView.snp.makeConstraints {
      $0.top.equalTo(customView.backgroundImageView.snp.bottom)
      $0.left.right.bottom.equalToSuperview()
    }
  }
  
  private func setupUIBindings() {
    collectionView.dataSource = dataSource
    dataSource.collectionView = collectionView
    
    presenter.state
      .map { $0.coins.sorted() }
      .asObservable()
      .bind(to: dataSource.coinsRelay)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let changeVisibilityDriver = dataSource.changeVisibilityRelay.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: FilterCoinsPresenter.Input(back: backDriver,
                                                     changeVisibility: changeVisibilityDriver))
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    return CGSize(width: collectionView.bounds.width - 30, height: 80)
  }
  
}
