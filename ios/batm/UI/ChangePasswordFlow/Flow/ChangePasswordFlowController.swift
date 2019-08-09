import RxFlow

protocol ChangePasswordFlowControllerDelegate: class {
  func didFinishChangePasswordFlow()
}

class ChangePasswordFlowController: FlowController {
  
  weak var delegate: ChangePasswordFlowControllerDelegate?
  
}

extension ChangePasswordFlowController: ChangePasswordModuleDelegate {
  
  func didFinishChangePassword() {
    delegate?.didFinishChangePasswordFlow()
  }
  
  func didChangePassword() {
    delegate?.didFinishChangePasswordFlow()
  }
  
}
