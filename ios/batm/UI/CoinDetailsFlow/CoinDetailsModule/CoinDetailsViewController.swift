import UIKit
import RxCocoa
import RxSwift
import SnapKit
import JJFloatingActionButton

final class CoinDetailsViewController: NavigationScreenViewController<CoinDetailsPresenter>, UICollectionViewDelegateFlowLayout {
  
  let didTapDepositRelay = PublishRelay<Void>()
  let didTapWithdrawRelay = PublishRelay<Void>()
  let didTapSendGiftRelay = PublishRelay<Void>()
  let didTapSellRelay = PublishRelay<Void>()
  let didTapExchangeRelay = PublishRelay<Void>()
  
  var dataSource: CoinDetailsCollectionViewDataSource!
  
  let collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
    return collectionView
  }()
  
  let refreshControl = UIRefreshControl()
  
  let floatingActionButton = JJFloatingActionButton()
  
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
                     floatingActionButton,
                     backgroundDarkView,
                     depositView)
    
    collectionView.backgroundColor = .clear
    collectionView.refreshControl = refreshControl
    collectionView.delegate = self
    
    configureFloatingActionButton()
  }
  
  private func configureFloatingActionButton() {
    floatingActionButton.buttonDiameter = 56
    floatingActionButton.overlayView.backgroundColor = UIColor(white: 0, alpha: 0.6)
    floatingActionButton.buttonImage = UIImage(named: "fab_plus")
    floatingActionButton.buttonColor = .ceruleanBlue
    floatingActionButton.buttonImageColor = .white

    let fabCancelImage = UIImage(named: "fab_cancel")
    fabCancelImage.flatMap { floatingActionButton.buttonAnimationConfiguration = .transition(toImage: $0) }
    floatingActionButton.itemAnimationConfiguration = .slideIn(withInterItemSpacing: 15)

    floatingActionButton.layer.shadowColor = UIColor.black.cgColor
    floatingActionButton.layer.shadowOffset = CGSize(width: 0, height: 5)
    floatingActionButton.layer.shadowOpacity = Float(0.2)
    floatingActionButton.layer.shadowRadius = CGFloat(5)
    
    floatingActionButton.configureDefaultItem { item in
      item.titleLabel.font = .systemFont(ofSize: 14, weight: .medium)
      item.titleLabel.textColor = .white
      item.buttonColor = .ceruleanBlue
      item.buttonImageColor = .white
      
      item.layer.shadowColor = UIColor.black.cgColor
      item.layer.shadowOffset = CGSize(width: 0, height: 5)
      item.layer.shadowOpacity = Float(0.2)
      item.layer.shadowRadius = CGFloat(5)
    }
    
    floatingActionButton.addItem(title: localize(L.CoinDetails.deposit), image: UIImage(named: "fab_deposit")) { [unowned self] _ in
      self.didTapDepositRelay.accept(())
    }
    floatingActionButton.addItem(title: localize(L.CoinDetails.withdraw), image: UIImage(named: "fab_withdraw")) { [unowned self] _ in
      self.didTapWithdrawRelay.accept(())
    }
    floatingActionButton.addItem(title: localize(L.CoinDetails.sendGift), image: UIImage(named: "fab_send_gift")) { [unowned self] _ in
      self.didTapSendGiftRelay.accept(())
    }
    floatingActionButton.addItem(title: localize(L.CoinDetails.sell), image: UIImage(named: "fab_sell")) { [unowned self] _ in
      self.didTapSellRelay.accept(())
    }
    floatingActionButton.addItem(title: localize(L.CoinDetails.c2cExchange), image: UIImage(named: "fab_exchange")) { [unowned self] _ in
      self.didTapExchangeRelay.accept(())
    }
    
    floatingActionButton.delegate = self
  }

  override func setupLayout() {
    collectionView.snp.makeConstraints {
      $0.top.equalTo(customView.backgroundImageView.snp.bottom)
      $0.left.right.bottom.equalToSuperview()
    }
    floatingActionButton.snp.makeConstraints {
      $0.right.bottom.equalTo(view.safeAreaLayoutGuide).inset(16)
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
    
    didTapDepositRelay.asDriver(onErrorDriveWith: .empty())
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
    let withdrawDriver = didTapWithdrawRelay.asDriver(onErrorDriveWith: .empty())
    let sendGiftDriver = didTapSendGiftRelay.asDriver(onErrorDriveWith: .empty())
    let sellDriver = didTapSellRelay.asDriver(onErrorDriveWith: .empty())
    let exchangeDriver = didTapExchangeRelay.asDriver(onErrorDriveWith: .empty())
    let copyDriver = depositView.rx.copyTap
    let showMoreDriver = collectionView.rx.willDisplayLastCell.asDriver(onErrorDriveWith: .empty())
    let transactionSelectedDriver = collectionView.rx.itemSelected.asDriver()
    let updateSelectedPeriodDriver = dataSource.rx.selectedPeriod
    
    presenter.bind(input: CoinDetailsPresenter.Input(back: backDriver,
                                                     refresh: refreshDriver,
                                                     withdraw: withdrawDriver,
                                                     sendGift: sendGiftDriver,
                                                     sell: sellDriver,
                                                     exchange: exchangeDriver,
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

extension CoinDetailsViewController: JJFloatingActionButtonDelegate {
  func floatingActionButtonWillOpen(_ button: JJFloatingActionButton) {
    button.buttonColor = .white
    button.buttonImageColor = .ceruleanBlue
  }
  
  func floatingActionButtonWillClose(_ button: JJFloatingActionButton) {
    button.buttonColor = .ceruleanBlue
    button.buttonImageColor = .white
  }
}
