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
  
  let backgroundDarkView: BackgroundDarkView = {
    let view = BackgroundDarkView()
    view.alpha = 0
    return view
  }()
  
  let depositView: CoinDetailsDepositView = {
    let view = CoinDetailsDepositView()
    view.alpha = 0
    return view
  }()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.backgroundColor = .whiteTwo
    
    view.addSubviews(collectionView,
                     backgroundDarkView,
                     depositView)
    
    collectionView.backgroundColor = .clear
    collectionView.refreshControl = refreshControl
    collectionView.delegate = self
  }

  override func setupLayout() {
    collectionView.snp.makeConstraints {
      $0.top.equalTo(customView.backgroundImageView.snp.bottom)
      $0.left.right.bottom.equalToSuperview()
    }
    backgroundDarkView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    depositView.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(30)
      $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-30)
    }
  }
  
  private func hideDepositView() {
    UIView.animate(withDuration: 0.3) {
      self.backgroundDarkView.alpha = 0
      self.depositView.alpha = 0
    }
  }
  
  private func showDepositView() {
    UIView.animate(withDuration: 0.3) {
      self.backgroundDarkView.alpha = 1
      self.depositView.alpha = 1
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
    
    dataSource.rx.depositTap
      .withLatestFrom(presenter.state)
      .map { $0.coin }
      .filterNil()
      .do(onNext: { [depositView] in depositView.configure(for: $0) })
      .drive(onNext: { [unowned self] _ in self.showDepositView() })
      .disposed(by: disposeBag)
    
    Driver.merge(depositView.closeButton.rx.tap.asDriver(),
                 backgroundDarkView.rx.tap)
      .drive(onNext: { [unowned self] in self.hideDepositView() })
      .disposed(by: disposeBag)
    
    depositView.rx.copyTap
      .drive(onNext: { [unowned self] _ in self.view.makeToast(localize(L.Shared.copied)) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let refreshDriver = refreshControl.rx.controlEvent(.valueChanged).asDriver()
    let withdrawDriver = dataSource.rx.withdrawTap
    let sendGiftDriver = dataSource.rx.sendGiftTap
    let sellDriver = dataSource.rx.sellTap
    let copyDriver = depositView.rx.copyTap
    let showMoreDriver = collectionView.rx.willDisplayLastCell.asDriver(onErrorDriveWith: .empty())
    let transactionSelectedDriver = collectionView.rx.itemSelected.asDriver()
    let updateSelectedPeriodDriver = dataSource.rx.selectedPeriod
    
    presenter.bind(input: CoinDetailsPresenter.Input(back: backDriver,
                                                     refresh: refreshDriver,
                                                     withdraw: withdrawDriver,
                                                     sendGift: sendGiftDriver,
                                                     sell: sellDriver,
                                                     copy: copyDriver,
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
