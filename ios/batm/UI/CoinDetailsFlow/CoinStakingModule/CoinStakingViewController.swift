import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class CoinStakingViewController: NavigationScreenViewController<CoinStakingPresenter> {
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    
  }

  override func setupLayout() {
    
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [customView] in
        let title = String(format: localize(L.CoinStaking.title), $0)
        customView.setTitle(title)
      })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinStakingPresenter.Input(back: backDriver))
  }
}
