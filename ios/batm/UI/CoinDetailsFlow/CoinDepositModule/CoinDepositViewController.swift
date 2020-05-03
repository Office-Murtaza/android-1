import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinDepositViewController: NavigationScreenViewController<CoinDepositPresenter> {
  
  let qrCodeCardView = QRCodeCardView()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.setTitle(String(format: localize(L.CoinDeposit.title), presenter.coin.type.code))
    customView.contentView.addSubview(qrCodeCardView)
  }

  override func setupLayout() {
    customView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
    qrCodeCardView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview().inset(30)
    }
  }
  
  func setupUIBindings() {
    qrCodeCardView.configure(for: presenter.coin.publicKey)
    
    qrCodeCardView.rx.copy
      .drive(onNext: { [unowned self] in self.view.makeToast(localize(L.Shared.copied)) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let copyDriver = qrCodeCardView.rx.copy
    
    presenter.bind(input: CoinDepositPresenter.Input(back: backDriver,
                                                     copy: copyDriver))
  }
}
