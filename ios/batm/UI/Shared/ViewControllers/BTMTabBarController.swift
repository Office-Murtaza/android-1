import UIKit

class BTMTabBarController: UITabBarController {
  override var shouldAutorotate: Bool {
    return selectedViewController?.shouldAutorotate ?? false
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    return selectedViewController?.supportedInterfaceOrientations ?? .portrait
  }
  
  override var preferredInterfaceOrientationForPresentation: UIInterfaceOrientation {
    return selectedViewController?.preferredInterfaceOrientationForPresentation ?? .portrait
  }
}
