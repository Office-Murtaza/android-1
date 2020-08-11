import Foundation
import RxSwift
import RxCocoa

protocol ErrorService {
  func showError(for type: ErrorType) -> Completable
}

class ErrorServiceImpl: ErrorService {
  
  private let getModule: (() -> Module<ErrorModule>)
  private let didFinishRelay = PublishRelay<Void>()
  
  var module: Module<ErrorModule>?
  
  
  init(getModule: @escaping (() -> Module<ErrorModule>)) {
    self.getModule = getModule
  }
  
  func showError(for type: ErrorType) -> Completable {
    return Observable.just(())
      .observeOn(MainScheduler.instance)
      .doOnNext { [unowned self] in
        guard self.module == nil else { return }
        
        let module = self.getModule()
        module.input.setup(with: type)
        self.module = module
        
        UIViewController.presentModuleIfNeeded(module)
    }
    .flatMap { [didFinishRelay] in didFinishRelay }
    .take(1)
    .toCompletable()
    .observeOn(MainScheduler.instance)
    .do(onCompleted: { [unowned self] in
      guard let module = self.module else { return }
      
      UIViewController.dismissModuleIfNeeded(module)
      
      self.module = nil
    })
  }
}

extension ErrorServiceImpl: ErrorModuleDelegate {
  func didFinishError() {
    didFinishRelay.accept(())
  }
}
