import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinDetailsViewController: ModuleViewController<CoinDetailsPresenter>, UICollectionViewDelegateFlowLayout {
  
  var dataSource: CoinDetailsCollectionViewDataSource!
  
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
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
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
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.backgroundColor = .whiteTwo
    
    view.addSubviews(backgroundImageView,
                     safeAreaContainer,
                     balanceView,
                     buttonsView,
                     collectionView,
                     backgroundDarkView,
                     depositView)
    safeAreaContainer.addSubviews(backButton,
                                  titleLabel)
    
    collectionView.backgroundColor = .clear
    collectionView.refreshControl = refreshControl
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
    balanceView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom).offset(25)
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
      $0.bottom.equalToSuperview().offset(-view.safeAreaInsets.bottom - 60)
      $0.left.right.equalToSuperview().inset(30)
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
    titleLabel.text = presenter.coinBalance.type.verboseValue
    balanceView.configure(for: presenter.coinBalance)
    
    collectionView.dataSource = dataSource
    dataSource.collectionView = collectionView
    
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
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
    let refreshDriver = refreshControl.rx.controlEvent(.valueChanged).asDriver()
    let depositDriver = buttonsView.rx.depositTap
    let withdrawDriver = buttonsView.rx.withdrawTap
    let sendGiftDriver = buttonsView.rx.sendGiftTap
    let sellDriver = buttonsView.rx.sellTap
    let copyDriver = depositView.rx.copyTap
    let showMoreDriver = collectionView.rx.willDisplayLastCell.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: CoinDetailsPresenter.Input(back: backDriver,
                                                     refresh: refreshDriver,
                                                     deposit: depositDriver,
                                                     withdraw: withdrawDriver,
                                                     sendGift: sendGiftDriver,
                                                     sell: sellDriver,
                                                     copy: copyDriver,
                                                     showMore: showMoreDriver))
  }
  
  func collectionView(_ collectionView: UICollectionView,
                      layout collectionViewLayout: UICollectionViewLayout,
                      sizeForItemAt indexPath: IndexPath) -> CGSize {
    return CGSize(width: collectionView.bounds.width - 20, height: 50)
  }
}
