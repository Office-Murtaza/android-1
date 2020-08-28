import RxFlow

protocol SeedPhraseFlowControllerDelegate: class {
  func didFinishSeedPhraseFlow()
}

class SeedPhraseFlowController: FlowController {
  
  weak var delegate: SeedPhraseFlowControllerDelegate?
  
}

extension SeedPhraseFlowController: EnterPasswordModuleDelegate {
  
  func didMatchPassword() {
    step.accept(SeedPhraseFlow.Steps.seedPhrase)
  }
  
}

extension SeedPhraseFlowController: SeedPhraseModuleDelegate {
  
  func didFinishCopyingSeedPhrase() {
    delegate?.didFinishSeedPhraseFlow()
  }
  
}
