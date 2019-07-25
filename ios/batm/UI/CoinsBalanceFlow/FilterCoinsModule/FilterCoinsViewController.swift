import UIKit
import RxSwift
import RxCocoa
import SnapKit

class FilterCoinsViewController: ModuleViewController<FilterCoinsPresenter>, UICollectionViewDelegateFlowLayout {
  
  var dataSource: FilterCoinsCollectionViewDataSource!
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()
  
  let safeAreaContainer = UIView()
  
  let backButton: UIButton = {
    let button = UIButton()
    button.setImage(UIImage(named: "back"), for: .normal)
    return button
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.FilterCoins.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  let collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
    collectionView.contentInset = UIEdgeInsets(top: 25, left: 0, bottom: 25, right: 0)
    return collectionView
  }()
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.backgroundColor = .whiteThree
    
    view.addSubviews(backgroundImageView,
                     safeAreaContainer,
                     collectionView)
    safeAreaContainer.addSubviews(backButton,
                                  titleLabel)
    
    collectionView.backgroundColor = .clear

    collectionView.delegate = self
  }
  
  override func setupLayout() {
    backgroundImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.bottom.equalTo(view.safeAreaLayoutGuide.snp.top).offset(44)
    }
    safeAreaContainer.snp.makeConstraints {
      $0.left.right.bottom.equalTo(backgroundImageView)
      $0.top.equalTo(view.safeAreaLayoutGuide)
    }
    backButton.snp.makeConstraints {
      $0.centerY.equalTo(titleLabel)
      $0.left.equalToSuperview().offset(15)
      $0.size.equalTo(45)
    }
    titleLabel.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    collectionView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom)
      $0.left.right.bottom.equalToSuperview()
    }
  }
  
  private func setupUIBindings() {
    collectionView.dataSource = dataSource
    dataSource.collectionView = collectionView
    
    presenter.state
      .map { $0.coins }
      .asObservable()
      .bind(to: dataSource.coinsRelay)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
    let changeVisibilityDriver = dataSource.changeVisibilityRelay.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: FilterCoinsPresenter.Input(back: backDriver,
                                                     changeVisibility: changeVisibilityDriver))
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    return CGSize(width: collectionView.bounds.width - 50, height: 80)
  }
  
}
