import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinDepositViewController: ModuleViewController<CoinDepositPresenter> {
  
  let qrCodeCardView = QRCodeCardView()
  
  override var shouldShowNavigationBar: Bool { return true }

  override func setupUI() {
    view.addSubview(qrCodeCardView)
  }

  override func setupLayout() {
    qrCodeCardView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview().inset(30)
    }
  }
  
  func setupUIBindings() {
    title = String(format: localize(L.CoinDeposit.title), presenter.coin.type.code)
    
    qrCodeCardView.configure(for: presenter.coin.address)
    
    qrCodeCardView.rx.copy
      .drive(onNext: { [unowned self] in self.view.makeToast(localize(L.Shared.copied)) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let copyDriver = qrCodeCardView.rx.copy
    
    presenter.bind(input: CoinDepositPresenter.Input(copy: copyDriver))
  }
}
