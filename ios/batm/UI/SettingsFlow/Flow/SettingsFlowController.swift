import Foundation
import RxSwift
import RxFlow

protocol SettingsFlowControllerDelegate: class {}

class SettingsFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = SettingsFlow.Steps.settings
  
  weak var delegate: SettingsFlowControllerDelegate?
  
}

extension SettingsFlowController: SettingsModuleDelegate {
  func didSelectSecurity() {
    step.accept(SettingsFlow.Steps.security)
  }
  
  func didSelectKYC(_ kyc: KYC) {
    step.accept(SettingsFlow.Steps.kyc(kyc))
  }
  
  func didSelectAbout() {
    step.accept(SettingsFlow.Steps.about)
  }
}

extension SettingsFlowController: SecurityModuleDelegate {
  
  func didSelectUpdatePhone(_ phoneNumber: PhoneNumber) {
    step.accept(SettingsFlow.Steps.updatePhone(phoneNumber))
  }
  
  func didSelectUpdatePassword() {
    step.accept(SettingsFlow.Steps.updatePassword)
  }
  
  func didSelectUpdatePIN(_ pinCode: String) {
    step.accept(SettingsFlow.Steps.updatePIN(pinCode))
  }
  
  func didSelectSeedPhrase() {
    step.accept(SettingsFlow.Steps.seedPhrase)
  }
  
  func didSelectUnlinkWallet() {
    step.accept(SettingsFlow.Steps.unlinkWallet)
  }
  
}

extension SettingsFlowController: AboutModuleDelegate {}

extension SettingsFlowController: UpdatePhoneFlowControllerDelegate {
  
  func didFinishUpdatePhoneFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: UpdatePasswordFlowControllerDelegate {
  
  func didFinishUpdatePasswordFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: UpdatePinFlowControllerDelegate {
  
  func didFinishUpdatePinFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: KYCFlowControllerDelegate {
  
  func didFinishKYCFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: SeedPhraseFlowControllerDelegate {
  
  func didFinishSeedPhraseFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}

extension SettingsFlowController: UnlinkFlowControllerDelegate {
  
  func didFinishUnlinkFlow() {
    step.accept(SettingsFlow.Steps.popToRoot)
  }
  
}
