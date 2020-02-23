import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinDetailsViewController: NavigationScreenViewController<CoinDetailsPresenter>, UICollectionViewDelegateFlowLayout {
  
  var dataSource: CoinDetailsCollectionViewDataSource!
  
  let balanceView = CoinDetailsBalanceView()
  
  let buttonsView = CoinDetailsButtonsView()
  
  let collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
    collectionView.contentInset = UIEdgeInsets(top: 10, left: 0, bottom: 0, right: 0)
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
    
    view.addSubviews(balanceView,
                     buttonsView,
                     collectionView,
                     backgroundDarkView,
                     depositView)
    
    collectionView.backgroundColor = .clear
    collectionView.refreshControl = refreshControl
    collectionView.delegate = self
  }

  override func setupLayout() {
    balanceView.snp.makeConstraints {
      $0.top.equalTo(customView.backgroundImageView.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(10)
    }
    buttonsView.snp.makeConstraints {
      $0.top.equalTo(balanceView.snp.bottom).offset(25)
      $0.left.right.equalToSuperview().inset(10)
    }
    collectionView.snp.makeConstraints {
      $0.top.equalTo(buttonsView.snp.bottom)
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
      .drive(onNext: { [customView, balanceView] in
        customView.setTitle($0.type.verboseValue)
        balanceView.configure(for: $0)
      })
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
    
    buttonsView.rx.depositTap
      .withLatestFrom(presenter.state)
      .map { $0.coin }
      .filterNil()
      .do(onNext: { [depositView] in depositView.configure(for: $0) })
      .asDriver()
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
    let withdrawDriver = buttonsView.rx.withdrawTap
    let sendGiftDriver = buttonsView.rx.sendGiftTap
    let sellDriver = buttonsView.rx.sellTap
    let copyDriver = depositView.rx.copyTap
    let showMoreDriver = collectionView.rx.willDisplayLastCell.asDriver(onErrorDriveWith: .empty())
    let transactionSelectedDriver = collectionView.rx.itemSelected.asDriver()
    
    presenter.bind(input: CoinDetailsPresenter.Input(back: backDriver,
                                                     refresh: refreshDriver,
                                                     withdraw: withdrawDriver,
                                                     sendGift: sendGiftDriver,
                                                     sell: sellDriver,
                                                     copy: copyDriver,
                                                     showMore: showMoreDriver,
                                                     transactionSelected: transactionSelectedDriver))
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    return CGSize(width: collectionView.bounds.width - 20, height: 50)
  }
}
