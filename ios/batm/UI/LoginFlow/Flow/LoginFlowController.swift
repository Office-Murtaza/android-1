import Foundation
import RxFlow

protocol LoginFlowControllerDelegate: class {
  func didFinishLogin()
}

final class LoginFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = LoginFlow.Steps.welcome(nil)
  
  var isCreatingAccount = true
  var password = ""
  
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
  
  func finishCreatingWallet(phoneNumber: String, password: String) {
    isCreatingAccount = true
    self.password = password
    step.accept(LoginFlow.Steps.phoneVerification(phoneNumber))
  }
  
}

extension LoginFlowController: PhoneVerificationModuleDelegate {
  
  func didFinishPhoneVerification(phoneNumber: String) {
    if isCreatingAccount {
      step.accept(LoginFlow.Steps.seedPhrase(phoneNumber, password))
    } else {
      step.accept(LoginFlow.Steps.recoverSeedPhrase(phoneNumber, password))
    }
  }
  
}

extension LoginFlowController: SeedPhraseModuleDelegate {
  
  func didFinishCopyingSeedPhrase() {
    step.accept(LoginFlow.Steps.pinCode(.setup))
  }
  
}

extension LoginFlowController: RecoverModuleDelegate {
  
  func didCancelRecovering() {
    step.accept(LoginFlow.Steps.pop)
  }
  
  func finishRecovering(phoneNumber: String, password: String) {
    isCreatingAccount = false
    self.password = password
    step.accept(LoginFlow.Steps.phoneVerification(phoneNumber))
  }
  
}

extension LoginFlowController: RecoverSeedPhraseModuleDelegate {
  
  func finishRecoveringSeedPhrase() {
    step.accept(LoginFlow.Steps.pinCode(.setup))
  }
  
}

extension LoginFlowController: PinCodeModuleDelegate {
  
  func didFinishPinCode(for stage: PinCodeStage, with pinCode: String) {
    switch stage {
    case .setup: step.accept(LoginFlow.Steps.pinCode(.confirmation, pinCode))
    case .confirmation, .verification:
      delegate?.didFinishLogin()
    }
  }
  
}
