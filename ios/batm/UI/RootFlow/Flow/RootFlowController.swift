import Foundation
import RxSwift
import RxFlow

class RootFlowController: FlowController, FlowActivator, HasDisposeBag {
  
  var initialStep: Step = RootFlow.Steps.splash
  
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
        case .seedPhrase:
          return .just(RootFlow.Steps.seedPhrase)
        case .setupPinCode:
          return .just(RootFlow.Steps.pinCode(.setup))
        case .loggedIn:
          return .just(RootFlow.Steps.pinCode(.verification))
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
  func didFinishPinCode(for stage: PinCodeStage) {
    switch stage {
    case .setup: step.accept(RootFlow.Steps.pinCode(.confirmation))
    case .confirmation, .verification: step.accept(RootFlow.Steps.main)
    }
    step.accept(RootFlow.Steps.main)
  }
}

extension RootFlowController: MainFlowControllerDelegate {}
