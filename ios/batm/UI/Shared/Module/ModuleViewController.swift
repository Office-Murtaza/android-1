import UIKit
import RxSwift
import RxCocoa
import MBProgressHUD

class ModuleViewController <PresenterType: ModulePresenter>: UIViewController, NavigationBarVisibility {
  
  final var presenter: PresenterType!
  
  final var didSetupConstraints: Bool = false
  
  var shouldShowNavigationBar: Bool {
    return true
  }
  
  final override func viewDidLoad() {
    super.viewDidLoad()
    
    view.setNeedsUpdateConstraints()
    
    setupUI()
    setupDefaultBindings()
    setupBindings()
    
    presenter.viewIsReady()
  }
  
  final override func updateViewConstraints() {
    if !didSetupConstraints {
      setupLayout()
      didSetupConstraints = true
    }
    super.updateViewConstraints()
  }
  
  private func setupDefaultBindings() {
    presenter.activity.drive(view.rx.showHUD).disposed(by: disposeBag)
    presenter.errors.drive(rx.errors).disposed(by: disposeBag)
  }
  
  func setupLayout() {
    // Extension point
  }
  
  func setupUI() {
    // Extension point
  }
  
  func setupBindings() {
    // Extension point
  }
}
