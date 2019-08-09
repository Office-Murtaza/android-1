import RxFlow

protocol ChangePhoneFlowControllerDelegate: class {
  func didFinishChangePhoneFlow()
}

class ChangePhoneFlowController: FlowController {
  
  weak var delegate: ChangePhoneFlowControllerDelegate?
  
}

extension ChangePhoneFlowController: ShowPhoneModuleDelegate {
  
  func didFinishShowPhone() {
    delegate?.didFinishChangePhoneFlow()
  }
  
  func didSelectChangePhone() {
    step.accept(ChangePhoneFlow.Steps.enterPassword)
  }
  
}

extension ChangePhoneFlowController: EnterPasswordModuleDelegate {
  
  func didFinishEnterPassword() {
    delegate?.didFinishChangePhoneFlow()
  }
  
  func didMatchPassword() {
    step.accept(ChangePhoneFlow.Steps.changePhone)
  }
  
}

extension ChangePhoneFlowController: ChangePhoneModuleDelegate {
  
  func didFinishChangePhone() {
    delegate?.didFinishChangePhoneFlow()
  }
  
  func didChangePhone() {
    delegate?.didFinishChangePhoneFlow()
  }
  
}
