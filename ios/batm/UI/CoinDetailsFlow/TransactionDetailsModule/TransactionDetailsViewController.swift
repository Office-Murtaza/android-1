import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class TransactionDetailsViewController: NavigationScreenViewController<TransactionDetailsPresenter> {
  
  let didTapRefTxIdRelay = PublishRelay<Void>()
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let generalSectionView = TransactionDetailsGeneralSectionView()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.contentView.addSubview(stackView)
    customView.setTitle(localize(L.TransactionDetails.title))
  }

  override func setupLayout() {
    stackView.snp.makeConstraints {
      $0.top.bottom.equalToSuperview()
      $0.left.right.equalToSuperview().inset(25)
    }
  }
  
  func setupUIBindings() {
    generalSectionView.configure(with: presenter.details, for: presenter.type)
    stackView.addArrangedSubview(generalSectionView)
    
    if presenter.details.hasGiftInfo {
      let divider = Divider()
      stackView.addArrangedSubview(divider)
      
      let giftSectionView = TransactionDetailsGiftSectionView()
      giftSectionView.configure(for: presenter.details)
      stackView.addArrangedSubview(giftSectionView)
    }
    
    if presenter.details.hasExchangeInfo {
      let divider = Divider()
      stackView.addArrangedSubview(divider)
      
      let exchangeSectionView = TransactionDetailsExchangeSectionView()
      exchangeSectionView.configure(for: presenter.details)
      stackView.addArrangedSubview(exchangeSectionView)
      
      exchangeSectionView.rx.linkTap
        .asObservable()
        .bind(to: didTapRefTxIdRelay)
        .disposed(by: disposeBag)
    }
    
    if presenter.details.hasSellInfo {
      let divider = Divider()
      stackView.addArrangedSubview(divider)
      
      let sellInfoSectionView = TransactionDetailsSellInfoSectionView()
      sellInfoSectionView.configure(for: presenter.details)
      stackView.addArrangedSubview(sellInfoSectionView)
    }
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let openTxIdLinkDriver = generalSectionView.rx.linkTap
    let openRefTxIdLinkDriver = didTapRefTxIdRelay.asDriver(onErrorDriveWith: .empty())
    
    presenter.bind(input: TransactionDetailsPresenter.Input(back: backDriver,
                                                            openTxIdLink: openTxIdLinkDriver,
                                                            openRefTxIdLink: openRefTxIdLinkDriver))
  }
  
  
}
