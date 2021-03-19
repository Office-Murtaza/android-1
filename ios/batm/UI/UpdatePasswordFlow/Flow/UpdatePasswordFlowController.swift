import RxFlow

protocol UpdatePasswordFlowControllerDelegate: AnyObject {
  func didFinishUpdatePasswordFlow()
}

class UpdatePasswordFlowController: FlowController {
  
  weak var delegate: UpdatePasswordFlowControllerDelegate?
  
}

extension UpdatePasswordFlowController: UpdatePasswordModuleDelegate {
  
  func didUpdatePassword() {
    delegate?.didFinishUpdatePasswordFlow()
  }
  
}
