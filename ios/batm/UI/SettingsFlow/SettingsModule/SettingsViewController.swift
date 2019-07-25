import UIKit
import RxSwift
import RxCocoa
import SnapKit

class SettingsViewController: ModuleViewController<SettingsPresenter> {
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.backgroundColor = .green
  }
  
  override func setupLayout() {
    
  }
  
  private func setupUIBindings() {
    
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    presenter.bind(input: SettingsPresenter.Input())
  }
  
}
