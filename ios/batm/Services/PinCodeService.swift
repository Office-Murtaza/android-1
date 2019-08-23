import Foundation
import RxSwift
import RxCocoa

protocol PinCodeService {
  func verifyPinCode() -> Completable
}

protocol PinCodeVerificationModuleDelegate: PinCodeModuleDelegate {}

class PinCodeServiceImpl: PinCodeService {
  
  private let pinCodeStorage: PinCodeStorage
  private let didVerifyPinCodeRelay = PublishRelay<Void>()
  
  var module: Module<PinCodeModule>!
  
  init(pinCodeStorage: PinCodeStorage) {
    self.pinCodeStorage = pinCodeStorage
  }
  
  func verifyPinCode() -> Completable {
    return pinCodeStorage.get()
      .flatMapCompletable { [unowned self] _ in self.processWithPinCodeModule() }
      .catchError {
        if let error = $0 as? PinCodeStorageError, error == .notFound {
          return .empty()
        }
        
        throw $0
      }
  }
  
  private func processWithPinCodeModule() -> Completable {
    return Observable.just(())
      .observeOn(MainScheduler.instance)
      .doOnNext { [unowned self] in
        var topRootViewController: UIViewController = (UIApplication.shared.keyWindow?.rootViewController)!
        while(topRootViewController.presentedViewController != nil){
          topRootViewController = topRootViewController.presentedViewController!
        }
        
        guard !self.module.controller.isBeingPresented &&
          self.module.controller.presentingViewController == nil else { return }
        
        topRootViewController.present(self.module.controller, animated: true, completion: nil)
      }
      .flatMap { [didVerifyPinCodeRelay] in didVerifyPinCodeRelay }
      .take(1)
      .toCompletable()
      .observeOn(MainScheduler.instance)
      .do(onCompleted: { [unowned self] in
        if self.module.controller.presentingViewController != nil && !self.module.controller.isBeingDismissed {
          self.module.controller.dismiss(animated: true, completion: nil)
        }
      })
  }
}

extension PinCodeServiceImpl: PinCodeVerificationModuleDelegate {
  func didFinishPinCode() {
    didVerifyPinCodeRelay.accept(())
  }
}
