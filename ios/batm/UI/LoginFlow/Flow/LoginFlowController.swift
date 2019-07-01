import Foundation
import RxFlow

protocol LoginFlowDelegate: class {}

final class LoginFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = LoginFlow.Steps.welcome
  
  weak var delegate: LoginFlowDelegate?
  
}

extension LoginFlowController: WelcomeModuleDelegate {
  
  func showCreateWalletScreen() {
    step.accept(LoginFlow.Steps.createWallet)
  }
  
}

extension LoginFlowController: CreateWalletModuleDelegate {
  
  func didCancelCreatingWallet() {
    step.accept(LoginFlow.Steps.pop)
  }
  
  func finishCreatingWallet() {
    step.accept(LoginFlow.Steps.seedPhrase)
  }
  
}

extension LoginFlowController: SeedPhraseModuleDelegate {
  
  func finishCopyingSeedPhrase() {
    step.accept(LoginFlow.Steps.backToWelcome)
  }
  
}
