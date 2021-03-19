import Foundation
import RxSwift
import RxFlow

protocol ATMFlowControllerDelegate: AnyObject {}

class ATMFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = ATMFlow.Steps.atm
  
  weak var delegate: ATMFlowControllerDelegate?
  
}

extension ATMFlowController: ATMModuleDelegate {}
