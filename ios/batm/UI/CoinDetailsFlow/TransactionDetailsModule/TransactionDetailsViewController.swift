import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class TransactionDetailsViewController: NavigationScreenViewController<TransactionDetailsPresenter> {
  
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
    generalSectionView.configure(for: presenter.details)
    stackView.addArrangedSubview(generalSectionView)
    
    if presenter.details.hasGiftInfo {
      let divider = Divider()
      stackView.addArrangedSubview(divider)
      
      let giftSectionView = TransactionDetailsGiftSectionView()
      giftSectionView.configure(for: presenter.details)
      stackView.addArrangedSubview(giftSectionView)
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
    let openLinkDriver = generalSectionView.rx.linkTap
    
    presenter.bind(input: TransactionDetailsPresenter.Input(back: backDriver,
                                                            openLink: openLinkDriver))
  }
  
  
}
