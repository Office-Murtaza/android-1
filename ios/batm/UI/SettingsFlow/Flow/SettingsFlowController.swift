import Foundation
import RxSwift
import RxFlow

protocol SettingsFlowControllerDelegate: class {}

class SettingsFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = SettingsFlow.Steps.settings
  
  weak var delegate: SettingsFlowControllerDelegate?
  
}

extension SettingsFlowController: SettingsModuleDelegate {
  
  func didSelectPhone(_ phoneNumber: PhoneNumber) {
    step.accept(SettingsFlow.Steps.phone(phoneNumber))
  }
  
  func didSelectChangePassword() {
    step.accept(SettingsFlow.Steps.changePassword)
  }
  
  func didSelectChangePin() {
    step.accept(SettingsFlow.Steps.changePin)
  }
  
  func didSelectVerification(_ info: VerificationInfo) {
    step.accept(SettingsFlow.Steps.verification(info))
  }
  
  func didSelectShowSeedPhrase() {
    step.accept(SettingsFlow.Steps.showSeedPhrase)
  }
  
  func didSelectUnlink() {
    step.accept(SettingsFlow.Steps.unlink)
  }
  
}

extension SettingsFlowController: ChangePhoneFlowControllerDelegate {
  
  func didFinishChangePhoneFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: ChangePasswordFlowControllerDelegate {
  
  func didFinishChangePasswordFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: ChangePinFlowControllerDelegate {
  
  func didFinishChangePinFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: VerificationFlowControllerDelegate {
  
  func didFinishVerificationFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: ShowSeedPhraseFlowControllerDelegate {
  
  func didFinishShowSeedPhraseFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: UnlinkFlowControllerDelegate {
  
  func didFinishUnlinkFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}
