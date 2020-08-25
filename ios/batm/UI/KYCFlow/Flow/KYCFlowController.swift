import RxFlow

protocol KYCFlowControllerDelegate: class {
  func didFinishKYCFlow()
}

class KYCFlowController: FlowController {
  
  weak var delegate: KYCFlowControllerDelegate?
  weak var kycModule: KYCModule?
  weak var verificationModule: VerificationModule?
  weak var vipVerificationModule: VIPVerificationModule?
  
}

extension KYCFlowController: KYCModuleDelegate {
  func didFinishKYC() {
    delegate?.didFinishKYCFlow()
  }
  
  func didSelectVerify(from module: KYCModule) {
    kycModule = module
    step.accept(KYCFlow.Steps.verification)
  }
  
  func didSelectVIPVerify(from module: KYCModule) {
    kycModule = module
    step.accept(KYCFlow.Steps.vipVerification)
  }
}

extension KYCFlowController: VerificationModuleDelegate {
  func showPicker(from module: VerificationModule) {
    verificationModule = module
    step.accept(KYCFlow.Steps.showPicker)
  }
  
  func didFinishVerification(with kyc: KYC?) {
    kyc.flatMap { kycModule?.setup(with: $0) }
    verificationModule = nil
    step.accept(KYCFlow.Steps.pop)
  }
}

extension KYCFlowController: VIPVerificationModuleDelegate {
  func showPicker(from module: VIPVerificationModule) {
    vipVerificationModule = module
    step.accept(KYCFlow.Steps.showPicker)
  }
  
  func didFinishVIPVerification(with kyc: KYC?) {
    kyc.flatMap { kycModule?.setup(with: $0) }
    vipVerificationModule = nil
    step.accept(KYCFlow.Steps.pop)
  }
}

extension KYCFlowController: PickerFlowControllerDelegate {
  func didPick(image: UIImage) {
    verificationModule?.didPick(image: image)
    vipVerificationModule?.didPick(image: image)
  }
}
