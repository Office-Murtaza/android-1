import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class ___FILEBASENAME___: NavigationScreenViewController<___VARIABLE_moduleName___Presenter> {
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    customView.setTitle(localize(L.___VARIABLE_moduleName___.title))
  }

  override func setupLayout() {
    
  }
  
  func setupUIBindings() {
    
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    
    presenter.bind(input: ___VARIABLE_moduleName___Presenter.Input(back: backDriver))
  }
}
