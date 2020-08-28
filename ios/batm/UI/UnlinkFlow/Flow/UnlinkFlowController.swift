import RxFlow

protocol UnlinkFlowControllerDelegate: class {}

class UnlinkFlowController: FlowController {
  
  weak var delegate: UnlinkFlowControllerDelegate?
  
}

extension UnlinkFlowController: UnlinkModuleDelegate {}
