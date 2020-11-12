import Foundation
import RxSwift
import RxFlow

protocol SettingsFlowControllerDelegate: class {}

class SettingsFlowController: FlowController, FlowActivator {
  
  var initialStep: Step = SettingsFlow.Steps.settings
  
  weak var delegate: SettingsFlowControllerDelegate?
  
}

extension SettingsFlowController: SettingsModuleDelegate {
    func didSelectWallet() {
        step.accept(SettingsFlow.Steps.wallet)
    }
    
  func didSelectSecurity() {
    step.accept(SettingsFlow.Steps.security)
  }
  
  func didSelectKYC(_ kyc: KYC) {
    step.accept(SettingsFlow.Steps.kyc(kyc))
  }
  
  func didSelectAbout() {
    step.accept(SettingsFlow.Steps.about)
  }
    
    func didSelectSupport() {
        step.accept(SettingsFlow.Steps.support)
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
  
  func didSelectUnlink() {
    step.accept(SettingsFlow.Steps.unlink)
  }
  
}

extension SettingsFlowController: SupportModuleDelegate {}
extension SettingsFlowController: AboutModuleDelegate {}

extension SettingsFlowController: UpdatePhoneFlowControllerDelegate {
  
  func didFinishUpdatePhoneFlow() {
    step.accept(SettingsFlow.Steps.popToRoot(localize(L.UpdatePhone.phoneUpdated)))
  }
  
}

extension SettingsFlowController: UpdatePasswordFlowControllerDelegate {
  
  func didFinishUpdatePasswordFlow() {
    step.accept(SettingsFlow.Steps.popToRoot(localize(L.UpdatePassword.passwordUpdated)))
  }
  
}

extension SettingsFlowController: UpdatePinFlowControllerDelegate {
  
  func didFinishUpdatePinFlow() {
    step.accept(SettingsFlow.Steps.popToRoot(localize(L.UpdatePIN.pinUpdated)))
  }
  
}

extension SettingsFlowController: KYCFlowControllerDelegate {
  
  func didFinishKYCFlow() {
    step.accept(SettingsFlow.Steps.popToRoot(nil))
  }
  
}

extension SettingsFlowController: SeedPhraseFlowControllerDelegate {
  
  func didFinishSeedPhraseFlow() {
    step.accept(SettingsFlow.Steps.popToRoot(nil))
  }
  
}

extension SettingsFlowController: UnlinkFlowControllerDelegate {
  
  func didFinishUnlinkFlow() {
    step.accept(SettingsFlow.Steps.popToRoot(nil))
  }
  
}
