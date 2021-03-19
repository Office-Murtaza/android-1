import RxFlow

protocol UpdatePhoneFlowControllerDelegate: AnyObject {
  func didFinishUpdatePhoneFlow()
}

class UpdatePhoneFlowController: FlowController {
  
  weak var delegate: UpdatePhoneFlowControllerDelegate?
  
  var phoneNumber = ""
  
}

extension UpdatePhoneFlowController: ShowPhoneModuleDelegate {
  
  func didSelectUpdatePhone(phoneNumber: String) {
    self.phoneNumber = phoneNumber
    step.accept(UpdatePhoneFlow.Steps.enterPassword)
  }
  
}

extension UpdatePhoneFlowController: EnterPasswordModuleDelegate {
  
  func didMatchPassword() {
    step.accept(UpdatePhoneFlow.Steps.updatePhone(phoneNumber))
  }
  
}

extension UpdatePhoneFlowController: UpdatePhoneModuleDelegate {
  
  func didNotMatchNewPhoneNumber(_ phoneNumber: String) {
    step.accept(UpdatePhoneFlow.Steps.verifyPhone(phoneNumber))
  }
  
}

extension UpdatePhoneFlowController: PhoneVerificationModuleDelegate {
  
  func didFinishPhoneVerification(phoneNumber: String) {
    delegate?.didFinishUpdatePhoneFlow()
  }
  
}
