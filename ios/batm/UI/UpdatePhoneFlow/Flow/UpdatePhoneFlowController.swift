import RxFlow

protocol UpdatePhoneFlowControllerDelegate: class {
  func didFinishUpdatePhoneFlow()
}

class UpdatePhoneFlowController: FlowController {
  
  weak var delegate: UpdatePhoneFlowControllerDelegate?
  
}

extension UpdatePhoneFlowController: ShowPhoneModuleDelegate {
  
  func didSelectUpdatePhone() {
    step.accept(UpdatePhoneFlow.Steps.enterPassword)
  }
  
}

extension UpdatePhoneFlowController: EnterPasswordModuleDelegate {
  
  func didMatchPassword() {
    step.accept(UpdatePhoneFlow.Steps.updatePhone)
  }
  
}

extension UpdatePhoneFlowController: UpdatePhoneModuleDelegate {
  
  func didUpdatePhone() {
    delegate?.didFinishUpdatePhoneFlow()
  }
  
}
