import Foundation
import RxFlow

final class LoginFlow: BaseFlow<BTMNavigationController, LoginFlowController> {
  
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      WelcomeAssembly(),
      CreateWalletAssembly(),
      PhoneVerificationAssembly(),
      SeedPhraseAssembly(),
      RecoverAssembly(),
      RecoverSeedPhraseAssembly(),
    ]
  }
  
  enum Steps: Step, Equatable {
    case welcome
    case createWallet
    case phoneVerification(String, String)
    case seedPhrase(String, String)
    case recover
    case recoverSeedPhrase(String, String)
    case pinCode(PinCodeStage)
    case backToWelcome
    case contactSupport
    case pop
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleLoginFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleLoginFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .welcome:
      let module = resolver.resolve(Module<WelcomeModule>.self)!
      return replaceRoot(module.controller, animated: false)
    case .createWallet:
      let module = resolver.resolve(Module<CreateWalletModule>.self)!
      return push(module.controller)
    case let .phoneVerification(phoneNumber, password):
      let module = resolver.resolve(Module<PhoneVerificationModule>.self)!
      module.input.setup(phoneNumber: phoneNumber, password: password)
      return push(module.controller)
    case let .seedPhrase(phoneNumber, password):
      let module = resolver.resolve(Module<SeedPhraseModule>.self)!
      module.input.setup(phoneNumber: phoneNumber, password: password)
      return push(module.controller)
    case .recover:
      let module = resolver.resolve(Module<RecoverModule>.self)!
      return push(module.controller)
    case let .recoverSeedPhrase(phoneNumber, password):
      let module = resolver.resolve(Module<RecoverSeedPhraseModule>.self)!
      module.input.setup(phoneNumber: phoneNumber, password: password)
      return push(module.controller)
    case let .pinCode(stage):
      let module = resolver.resolve(Module<PinCodeModule>.self)!
      module.input.setup(for: stage)
      return push(module.controller)
    case .backToWelcome:
      let module = resolver.resolve(Module<WelcomeModule>.self)!
      return replaceRoot(module.controller, animated: true)
    case .contactSupport:
      let alert = resolver.resolve(UIAlertController.self, argument: view.topViewController!)!
      return present(alert, animated: true)
    case .pop:
      return pop()
    }
  }
}
