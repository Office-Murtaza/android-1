import Foundation
import RxSwift
import RxCocoa

protocol PinCodeService {
  func verifyPinCode() -> Completable
}

protocol PinCodeVerificationModuleDelegate: PinCodeModuleDelegate {}

class PinCodeServiceImpl: PinCodeService, PinCodeVerificationModuleDelegate {
  
  var module: Module<PinCodeModule>!
  
  private let didVerifyPinCodeRelay = PublishRelay<Void>()
  
  func verifyPinCode() -> Completable {
    DispatchQueue.main.async {
      var topRootViewController: UIViewController = (UIApplication.shared.keyWindow?.rootViewController)!
      while(topRootViewController.presentedViewController != nil){
        topRootViewController = topRootViewController.presentedViewController!
      }
      
      guard !self.module.controller.isBeingPresented else { return }
      
      topRootViewController.present(self.module.controller, animated: true, completion: nil)
    }
    
    return didVerifyPinCodeRelay
      .take(1)
      .toCompletable()
      .do(onCompleted: { [unowned self] in
        DispatchQueue.main.async {
          self.module.controller.dismiss(animated: true, completion: nil)
        }
      })
    
    
  }
  
  func didFinishPinCode() {
    didVerifyPinCodeRelay.accept(())
  }
}
