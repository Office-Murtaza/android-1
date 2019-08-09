import RxFlow

protocol ShowSeedPhraseFlowControllerDelegate: class {
  func didFinishShowSeedPhraseFlow()
}

class ShowSeedPhraseFlowController: FlowController {
  
  weak var delegate: ShowSeedPhraseFlowControllerDelegate?
  
}

extension ShowSeedPhraseFlowController: EnterPasswordModuleDelegate {
  
  func didFinishEnterPassword() {
    delegate?.didFinishShowSeedPhraseFlow()
  }
  
  func didMatchPassword() {
    step.accept(ShowSeedPhraseFlow.Steps.showSeedPhrase)
  }
  
}

extension ShowSeedPhraseFlowController: ShowSeedPhraseModuleDelegate {
  
  func didFinishShowSeedPhrase() {
    delegate?.didFinishShowSeedPhraseFlow()
  }
  
}
