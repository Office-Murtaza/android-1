import Foundation
import RxSwift
import RxCocoa

protocol PinCodeService {
  func verifyPinCode() -> Completable
}

protocol PinCodeVerificationModuleDelegate: PinCodeModuleDelegate {}

class PinCodeServiceImpl: PinCodeService {
  
  private let pinCodeStorage: PinCodeStorage
  private let getModule: (() -> Module<PinCodeModule>)
  private let didVerifyPinCodeRelay = PublishRelay<Void>()
  
  var module: Module<PinCodeModule>?
  
  
  init(pinCodeStorage: PinCodeStorage, getModule: @escaping (() -> Module<PinCodeModule>)) {
    self.pinCodeStorage = pinCodeStorage
    self.getModule = getModule
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
        guard self.module == nil else { return }
        
        let module = self.getModule()
        self.module = module
        
        var topRootViewController: UIViewController = (UIApplication.shared.keyWindow?.rootViewController)!
        while(topRootViewController.presentedViewController != nil){
          topRootViewController = topRootViewController.presentedViewController!
        }
        
        guard !module.controller.isBeingPresented &&
          module.controller.presentingViewController == nil else { return }
        
        module.controller.modalPresentationStyle = .fullScreen
        topRootViewController.present(module.controller, animated: true, completion: nil)
      }
      .flatMap { [didVerifyPinCodeRelay] in didVerifyPinCodeRelay }
      .take(1)
      .toCompletable()
      .observeOn(MainScheduler.instance)
      .do(onCompleted: { [unowned self] in
        guard let module = self.module else { return }
        
        if module.controller.presentingViewController != nil && !module.controller.isBeingDismissed {
          module.controller.dismiss(animated: true, completion: nil)
        }
        
        self.module = nil
      })
  }
}

extension PinCodeServiceImpl: PinCodeVerificationModuleDelegate {
  func didFinishPinCode(for stage: PinCodeStage, with pinCode: String) {
    didVerifyPinCodeRelay.accept(())
  }
}
