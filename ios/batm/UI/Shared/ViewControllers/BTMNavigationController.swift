import UIKit

protocol NavigationBarVisibility {
  var shouldShowNavigationBar: Bool { get }
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
