import RxFlow

protocol ChangePinFlowControllerDelegate: class {
  func didFinishChangePinFlow()
}

class ChangePinFlowController: FlowController {
  
  weak var delegate: ChangePinFlowControllerDelegate?
  
}

extension ChangePinFlowController: ChangePinModuleDelegate {
  
  func didFinishChangePin() {
    delegate?.didFinishChangePinFlow()
  }
  
}
