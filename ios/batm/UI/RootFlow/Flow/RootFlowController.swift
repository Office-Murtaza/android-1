import Foundation
import RxSwift
import RxFlow

class RootFlowController: FlowController, FlowActivator, HasDisposeBag {
  
  var initialStep: Step = RootFlow.Steps.login
  
  private let loginUsecase: LoginUsecase
  
  init(loginUsecase: LoginUsecase) {
    self.loginUsecase = loginUsecase
  }
  
  func activate() {
    let initialStepObservable = Observable.just(initialStep)
    let restoreSessionStepObservable: Observable<Step> = loginUsecase.getLoginState()
      .flatMap {
        switch $0 {
        case .loggedOut:
          return .just(RootFlow.Steps.login)
        case .loggedIn:
          return .just(RootFlow.Steps.verifyPinCode)
        }
      }
      .asObservable()
    let logoutStepObservable: Observable<Step> = loginUsecase.getLogoutObservable()
      .replace(RootFlow.Steps.login)
    
    Observable.merge(initialStepObservable,
                     restoreSessionStepObservable,
                     logoutStepObservable)
      .bind(to: step)
      .disposed(by: disposeBag)
  }
  
}

extension RootFlowController: LoginFlowControllerDelegate {
  func didFinishLogin() {
    step.accept(RootFlow.Steps.main)
  }
}

extension RootFlowController: PinCodeModuleDelegate {
  func didFinishPinCode() {
    step.accept(RootFlow.Steps.main)
  }
}

extension RootFlowController: MainFlowControllerDelegate {}
