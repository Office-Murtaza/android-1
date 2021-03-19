import RxFlow

protocol UpdatePinFlowControllerDelegate: AnyObject {
  func didFinishUpdatePinFlow()
}

class UpdatePinFlowController: FlowController {
  
  weak var delegate: UpdatePinFlowControllerDelegate?
  
}

extension UpdatePinFlowController: PinCodeModuleDelegate {
  
  func didFinishPinCode(for stage: PinCodeStage, with pinCode: String) {
    switch stage {
    case .verification:
      step.accept(UpdatePinFlow.Steps.newPin)
    case .setup:
      step.accept(UpdatePinFlow.Steps.confirmNewPin(pinCode))
    case .confirmation:
      delegate?.didFinishUpdatePinFlow()
    }
  }
  
}
