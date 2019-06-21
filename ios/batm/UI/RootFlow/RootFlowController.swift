import Foundation
import RxSwift
import RxFlow

class RootFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = RootFlow.Steps.login
  
}

extension RootFlowController: LoginFlowDelegate {}
