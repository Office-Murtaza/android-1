import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinDetailsViewController: NavigationScreenViewController<CoinDetailsPresenter>, UICollectionViewDelegateFlowLayout {
  
  let didTapDepositRelay = PublishRelay<Void>()
  let didTapWithdrawRelay = PublishRelay<Void>()
  let didTapSendGiftRelay = PublishRelay<Void>()
  let didTapSellRelay = PublishRelay<Void>()
  let didTapExchangeRelay = PublishRelay<Void>()
  let didTapTradesRelay = PublishRelay<Void>()
  let didTapStakingRelay = PublishRelay<Void>()
  
  var dataSource: CoinDetailsCollectionViewDataSource!
  
  let collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
    return collectionView
  }()
  
  let refreshControl = UIRefreshControl()
  
  let fab = FloatingActionButton()
  
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
    
    setupFAB()
  }
  
  private func setupFAB() {
    fab.view.addItem(title: localize(L.CoinDetails.deposit), image: UIImage(named: "fab_deposit")) { [unowned self] _ in
      self.didTapDepositRelay.accept(())
    }
    fab.view.addItem(title: localize(L.CoinDetails.withdraw), image: UIImage(named: "fab_withdraw")) { [unowned self] _ in
      self.didTapWithdrawRelay.accept(())
    }
    fab.view.addItem(title: localize(L.CoinDetails.sendGift), image: UIImage(named: "fab_send_gift")) { [unowned self] _ in
      self.didTapSendGiftRelay.accept(())
    }
    fab.view.addItem(title: localize(L.CoinDetails.sell), image: UIImage(named: "fab_sell")) { [unowned self] _ in
      self.didTapSellRelay.accept(())
    }
    fab.view.addItem(title: localize(L.CoinDetails.exchange), image: UIImage(named: "fab_exchange")) { [unowned self] _ in
      self.didTapExchangeRelay.accept(())
    }
    fab.view.addItem(title: localize(L.CoinDetails.trade), image: UIImage(named: "fab_trade")) { [unowned self] _ in
      self.didTapTradesRelay.accept(())
    }
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
      .map { $0.coin?.type == .catm }
      .filter { $0 }
      .asObservable()
      .take(1)
      .subscribe(onNext: { [unowned self] _ in
        self.fab.view.addItem(title: localize(L.CoinDetails.staking), image: UIImage(named: "fab_staking")) { [unowned self] _ in
          self.didTapStakingRelay.accept(())
        }
      })
      .disposed(by: disposeBag)
    
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
    let depositDriver = didTapDepositRelay.asDriver(onErrorDriveWith: .empty())
    let withdrawDriver = didTapWithdrawRelay.asDriver(onErrorDriveWith: .empty())
    let sendGiftDriver = didTapSendGiftRelay.asDriver(onErrorDriveWith: .empty())
    let sellDriver = didTapSellRelay.asDriver(onErrorDriveWith: .empty())
    let exchangeDriver = didTapExchangeRelay.asDriver(onErrorDriveWith: .empty())
    let tradesDriver = didTapTradesRelay.asDriver(onErrorDriveWith: .empty())
    let stakingDriver = didTapStakingRelay.asDriver(onErrorDriveWith: .empty())
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
                                                     staking: stakingDriver,
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
