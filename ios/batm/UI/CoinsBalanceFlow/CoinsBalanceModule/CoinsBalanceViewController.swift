import UIKit
import RxSwift
import RxCocoa
import SnapKit

class CoinsBalanceViewController: ModuleViewController<CoinsBalancePresenter>, UICollectionViewDelegateFlowLayout {
  
  var dataSource: CoinsBalanceCollectionViewDataSource!
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()
  
  let topSafeAreaContainer = UIView()
  
  let balanceContainer = UIView()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinsBalance.totalBalance)
    label.textColor = .lightGold
    label.font = .poppinsSemibold16
    return label
  }()
  
  let balanceLabel: UILabel = {
    let label = UILabel()
    label.textColor = .white
    label.font = .poppinsSemibold28
    return label
  }()
  
  let refreshControl = UIRefreshControl()
  
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
                     topSafeAreaContainer,
                     collectionView)
    
    topSafeAreaContainer.addSubview(balanceContainer)
    
    balanceContainer.addSubviews(titleLabel,
                                 balanceLabel)
    
    collectionView.backgroundColor = .clear
    collectionView.refreshControl = refreshControl
    collectionView.delegate = self
  }
  
  override func setupLayout() {
    backgroundImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(187)
    }
    topSafeAreaContainer.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.equalToSuperview()
      $0.bottom.equalTo(backgroundImageView)
    }
    balanceContainer.snp.makeConstraints {
      $0.left.right.centerY.equalToSuperview()
    }
    titleLabel.snp.makeConstraints {
      $0.top.centerX.equalToSuperview()
    }
    balanceLabel.snp.makeConstraints {
      $0.top.equalTo(titleLabel.snp.bottom)
      $0.centerX.bottom.equalToSuperview()
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
      .map { $0.coinsBalance?.totalBalance }
      .filterNil()
      .map { "$ \(NSNumber(value: $0).decimalValue)" }
      .asObservable()
      .bind(to: balanceLabel.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coinsBalance?.coins }
      .filterNil()
      .asObservable()
      .bind(to: dataSource.coinBalancesRelay)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.isFetching }
      .asObservable()
      .bind(to: refreshControl.rx.isRefreshing)
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let refreshDriver = refreshControl.rx.controlEvent(.valueChanged).asDriver()
    let filterCoinsTapDriver = dataSource.footerTap.asDriver(onErrorDriveWith: .empty())
    let coinTap = dataSource.coinTap.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: CoinsBalancePresenter.Input(refresh: refreshDriver,
                                                      filterCoinsTap: filterCoinsTapDriver,
                                                      coinTap: coinTap))
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    return CGSize(width: collectionView.bounds.width - 25, height: 86)
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      minimumLineSpacingForSectionAt section: Int) -> CGFloat {
    return 13
  }
  
  func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForFooterInSection section: Int) -> CGSize {
    return CGSize(width: collectionView.bounds.width, height: 70)
  }
  
}
