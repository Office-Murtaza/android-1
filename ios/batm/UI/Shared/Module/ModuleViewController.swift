import UIKit
import RxSwift
import RxCocoa
import MBProgressHUD

typealias ModuleViewController<P: ModulePresenter> = GenericModuleViewController<P, UIView>
typealias NavigationScreenViewController<P: ModulePresenter> = GenericModuleViewController<P, NavigationScreenView>

class GenericModuleViewController<PresenterType: ModulePresenter, View: UIView>: UIViewController, NavigationBarVisibility {
  
  final var presenter: PresenterType!
  
  final var didSetupConstraints: Bool = false
   
  var shouldShowNavigationBar: Bool {
    return false
  }
  
  var customView: View {
    guard let casted = view as? View else { fatalError("View should be a covariant of \(View.self)") }
    return casted
  }
  
  override func loadView() {
    self.view = View(frame: UIScreen.main.bounds)
  }
  
  final override func viewDidLoad() {
    super.viewDidLoad()
    
    view.setNeedsUpdateConstraints()
    
    setupUI()
    setupDefaultBindings()
    setupBindings()
    
    view.layoutIfNeeded()
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
    rxVisible.bind(to: presenter.visible).disposed(by: disposeBag)
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
