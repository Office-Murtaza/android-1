import Foundation
import RxSwift
import RxFlow

protocol DealsFlowControllerDelegate: class {}

class DealsFlowController: FlowController, FlowActivator {
    var initialStep: Step = DealsFlow.Steps.deals
    weak var delegate: DealsFlowControllerDelegate?
}

extension DealsFlowController: ATMModuleDelegate {}
