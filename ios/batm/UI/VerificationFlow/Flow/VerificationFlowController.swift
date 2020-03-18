import RxFlow

protocol VerificationFlowControllerDelegate: class {
  func didFinishVerificationFlow()
}

class VerificationFlowController: FlowController {
  
  weak var delegate: VerificationFlowControllerDelegate?
  weak var verificationInfoModule: VerificationInfoModule?
  weak var verificationModule: VerificationModule?
  weak var vipVerificationModule: VIPVerificationModule?
  
}

extension VerificationFlowController: VerificationInfoModuleDelegate {
  func didFinishVerificationInfo() {
    delegate?.didFinishVerificationFlow()
  }
  
  func didSelectVerify(from module: VerificationInfoModule) {
    verificationInfoModule = module
    step.accept(VerificationFlow.Steps.verification)
  }
  
  func didSelectVIPVerify(from module: VerificationInfoModule) {
    verificationInfoModule = module
    step.accept(VerificationFlow.Steps.vipVerification)
  }
}

extension VerificationFlowController: VerificationModuleDelegate {
  func showPicker(from module: VerificationModule) {
    verificationModule = module
    step.accept(VerificationFlow.Steps.showPicker)
  }
  
  func didFinishVerification(with info: VerificationInfo?) {
    info.flatMap { verificationInfoModule?.setup(with: $0) }
    verificationModule = nil
    step.accept(VerificationFlow.Steps.pop)
  }
}

extension VerificationFlowController: VIPVerificationModuleDelegate {
  func showPicker(from module: VIPVerificationModule) {
    vipVerificationModule = module
    step.accept(VerificationFlow.Steps.showPicker)
  }
  
  func didFinishVIPVerification(with info: VerificationInfo?) {
    info.flatMap { verificationInfoModule?.setup(with: $0) }
    vipVerificationModule = nil
    step.accept(VerificationFlow.Steps.pop)
  }
}

extension VerificationFlowController: PickerFlowControllerDelegate {
  func didPick(image: UIImage) {
    verificationModule?.didPick(image: image)
    vipVerificationModule?.didPick(image: image)
  }
}
