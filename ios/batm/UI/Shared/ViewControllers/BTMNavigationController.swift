import UIKit

protocol NavigationBarVisibility {
  var shouldShowNavigationBar: Bool { get }
}

protocol NavigationBarAppearance {
  var navBarLargeTitleModeEnabled: Bool { get }
  var navBarBackgroundColor: UIColor? { get }
  var navBarTitleTextAttributes: [NSAttributedString.Key: Any]? { get }
  var navBarTintColor: UIColor { get }
  var navBarBarTintColor: UIColor { get }
  var navBarIsTranslucent: Bool { get }
  var navBarStyle: UIBarStyle { get }
}

final class BTMNavigationController: UINavigationController {
  
  required init() {
    super.init(nibName: nil, bundle: nil)
    setupDefaults()
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    setupDefaults()
  }
  
  func setupDefaults() {
    self.delegate = self
  }
  
  // MARK: - Handling rotations
  
  private func targetControllerForRotations() -> UIViewController? {
    return topViewController
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    return targetControllerForRotations()?.supportedInterfaceOrientations ?? .portrait
  }
  
  override var preferredInterfaceOrientationForPresentation: UIInterfaceOrientation {
    return targetControllerForRotations()?.preferredInterfaceOrientationForPresentation ?? .portrait
  }
}

extension BTMNavigationController: UINavigationControllerDelegate {
  
  func navigationController(_ navigationController: UINavigationController,
                            willShow viewController: UIViewController, animated: Bool) {
    
    if let config = viewController as? NavigationBarAppearance {
      view.backgroundColor = config.navBarBackgroundColor
      viewController.navigationItem.largeTitleDisplayMode = config.navBarLargeTitleModeEnabled ? .always : .never
      navigationBar.titleTextAttributes = config.navBarTitleTextAttributes
      navigationBar.tintColor = config.navBarTintColor
      navigationBar.barTintColor = config.navBarBarTintColor
      navigationBar.isTranslucent = config.navBarIsTranslucent
      navigationBar.barStyle = config.navBarStyle
    }
    
    let isVisible = !navigationController.isNavigationBarHidden
    let shouldBeVisible = (viewController as? NavigationBarVisibility).flatMap { $0.shouldShowNavigationBar } ?? true
    
    switch (isVisible, shouldBeVisible) {
    case (true, false), (false, true):
      setNavigationBarHidden(!shouldBeVisible, animated: animated)
    case (false, false), (true, true):
      return
    }
  }
}
