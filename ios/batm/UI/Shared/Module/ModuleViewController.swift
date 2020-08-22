import UIKit
import RxSwift
import RxCocoa
import MBProgressHUD

typealias ModuleViewController<P: ModulePresenter> = GenericModuleViewController<P, UIView>
typealias NavigationScreenViewController<P: ModulePresenter> = GenericModuleViewController<P, NavigationScreenView>

class GenericModuleViewController<PresenterType: ModulePresenter, View: UIView>: UIViewController, NavigationBarVisibility, NavigationBarAppearance {
  
  final var presenter: PresenterType!
  
  final var didSetupConstraints: Bool = false
  
  // MARK: - NavigationBarVisibility
   
  var shouldShowNavigationBar: Bool {
    return false
  }
  
  // MARK: - NavigationBarAppearance
  var navBarLargeTitleModeEnabled: Bool {
    return false
  }
  
  var navBarBackgroundColor: UIColor? {
    return nil
  }
  
  var navBarTitleTextAttributes: [NSAttributedString.Key: Any]? {
    return [.foregroundColor: UIColor.slateGrey]
  }
  
  var navBarTintColor: UIColor {
    return .ceruleanBlue
  }
  
  var navBarBarTintColor: UIColor {
    return .white
  }
  
  var navBarIsTranslucent: Bool {
    return false
  }
  
  var navBarStyle: UIBarStyle {
    return .default
  }
  
  var customView: View {
    guard let casted = view as? View else { fatalError("View should be a covariant of \(View.self)") }
    return casted
  }
  
  override func loadView() {
    let view = View(frame: UIScreen.main.bounds)
    view.backgroundColor = .white
    self.view = view
    
  }
  
  final override func viewDidLoad() {
    super.viewDidLoad()
    
    view.setNeedsUpdateConstraints()
    
    navigationItem.backBarButtonItem = UIBarButtonItem(title: "Back", style: .plain, target: nil, action: nil)
    
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
