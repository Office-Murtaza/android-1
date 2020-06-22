import Foundation
import RxFlow

protocol LoginFlowControllerDelegate: class {
  func didFinishLogin()
}

final class LoginFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = LoginFlow.Steps.welcome
  
  weak var delegate: LoginFlowControllerDelegate?
  
}

extension LoginFlowController: WelcomeModuleDelegate {
  
  func showCreateWalletScreen() {
    step.accept(LoginFlow.Steps.createWallet)
  }
  
  func showRecoverScreen() {
    step.accept(LoginFlow.Steps.recover)
  }
  
  func showContactSupportAlert() {
    step.accept(LoginFlow.Steps.contactSupport)
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
    step.accept(LoginFlow.Steps.pinCode(.setup))
  }
  
}

extension LoginFlowController: RecoverModuleDelegate {
  
  func didCancelRecovering() {
    step.accept(LoginFlow.Steps.pop)
  }
  
  func finishRecovering() {
    step.accept(LoginFlow.Steps.recoverSeedPhrase)
  }
  
}

extension LoginFlowController: RecoverSeedPhraseModuleDelegate {
  
  func finishRecoveringSeedPhrase() {
    step.accept(LoginFlow.Steps.pinCode(.setup))
  }
  
}

extension LoginFlowController: PinCodeModuleDelegate {
  
  func didFinishPinCode(for stage: PinCodeStage) {
    switch stage {
    case .setup: step.accept(LoginFlow.Steps.pinCode(.confirmation))
    case .confirmation, .verification:
      delegate?.didFinishLogin()
    }
  }
  
}
