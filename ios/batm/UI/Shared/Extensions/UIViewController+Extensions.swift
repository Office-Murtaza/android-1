import UIKit

extension UIViewController {
  static func presentModuleIfNeeded<T>(_ module: Module<T>) {
    var topRootViewController: UIViewController = (UIApplication.shared.keyWindow?.rootViewController)!
    while(topRootViewController.presentedViewController != nil){
      topRootViewController = topRootViewController.presentedViewController!
    }
    
    guard !module.controller.isBeingPresented &&
      module.controller.presentingViewController == nil else { return }
    
    module.controller.modalPresentationStyle = .fullScreen
    topRootViewController.present(module.controller, animated: true, completion: nil)
  }
  
  static func dismissModuleIfNeeded<T>(_ module: Module<T>) {
    if module.controller.presentingViewController != nil && !module.controller.isBeingDismissed {
      module.controller.dismiss(animated: true, completion: nil)
    }
  }
}
