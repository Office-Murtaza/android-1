import RxFlow

protocol UnlinkFlowControllerDelegate: AnyObject {}

class UnlinkFlowController: FlowController {
  
  weak var delegate: UnlinkFlowControllerDelegate?
  
}

extension UnlinkFlowController: UnlinkModuleDelegate {}
