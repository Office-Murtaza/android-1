import Foundation
import RxFlow

protocol FlowController: Stepper { }

extension FlowController {
  var completedStep: Step {
    return BaseSteps.completed
  }
  
  func complete(action: () -> Void) {
    action()
    step.accept(completedStep)
  }
}

protocol FlowActivator {
  var initialStep: Step { get }
  func activate()
}

extension FlowController where Self: FlowActivator {
  func activate() {
    step.accept(initialStep)
  }
}
