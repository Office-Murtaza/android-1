import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinDetailsViewController: NavigationScreenViewController<CoinDetailsPresenter>, UICollectionViewDelegateFlowLayout {
  
  var dataSource: CoinDetailsCollectionViewDataSource!
  
  let collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
    return collectionView
  }()
  
  let refreshControl = UIRefreshControl()
  
  let fab = CoinDetailsFloatingActionButton()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.backgroundColor = .whiteTwo
    
    view.addSubviews(collectionView,
                     fab.view)
    
    collectionView.backgroundColor = .clear
    collectionView.refreshControl = refreshControl
    collectionView.delegate = self
  }

  override func setupLayout() {
    collectionView.snp.makeConstraints {
      $0.top.equalTo(customView.backgroundImageView.snp.bottom)
      $0.left.right.bottom.equalToSuperview()
    }
    fab.view.snp.makeConstraints {
      $0.right.bottom.equalTo(view.safeAreaLayoutGuide).inset(16)
    }
  }
  
  func setupUIBindings() {
    collectionView.dataSource = dataSource
    dataSource.collectionView = collectionView
    
    presenter.state
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [unowned self] in self.customView.setTitle($0.type.verboseValue) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .filter { $0.coinBalance != nil && $0.priceChartData != nil }
      .map { CoinDetailsHeaderViewConfig(coinBalance: $0.coinBalance!,
                                         priceChartData: $0.priceChartData!,
                                         selectedPeriod: $0.selectedPeriod) }
      .bind(to: dataSource.headerViewConfigRelay)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.transactions?.transactions }
      .filterNil()
      .asObservable()
      .bind(to: dataSource.transactionsRelay)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.isFetching }
      .asObservable()
      .bind(to: refreshControl.rx.isRefreshing)
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let refreshDriver = refreshControl.rx.controlEvent(.valueChanged).asDriver()
    let depositDriver = fab.rx.depositTap
    let withdrawDriver = fab.rx.withdrawTap
    let sendGiftDriver = fab.rx.sendGiftTap
    let sellDriver = fab.rx.sellTap
    let exchangeDriver = fab.rx.exchangeTap
    let tradesDriver = fab.rx.tradesTap
    let showMoreDriver = collectionView.rx.willDisplayLastCell.asDriver(onErrorDriveWith: .empty())
    let transactionSelectedDriver = collectionView.rx.itemSelected.asDriver()
    let updateSelectedPeriodDriver = dataSource.rx.selectedPeriod
    
    presenter.bind(input: CoinDetailsPresenter.Input(back: backDriver,
                                                     refresh: refreshDriver,
                                                     deposit: depositDriver,
                                                     withdraw: withdrawDriver,
                                                     sendGift: sendGiftDriver,
                                                     sell: sellDriver,
                                                     exchange: exchangeDriver,
                                                     trades: tradesDriver,
                                                     showMore: showMoreDriver,
                                                     transactionSelected: transactionSelectedDriver,
                                                     updateSelectedPeriod: updateSelectedPeriodDriver))
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    return CGSize(width: collectionView.bounds.width - 20, height: 50)
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      referenceSizeForHeaderInSection section: Int) -> CGSize {
    return CGSize(width: collectionView.bounds.width, height: 390)
  }
}
