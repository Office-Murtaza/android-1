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
      SupportAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case welcome(String?)
    case createWallet
    case phoneVerification(String)
    case seedPhrase(String, String)
    case recover
    case recoverSeedPhrase(String, String)
    case pinCode(PinCodeStage, String? = nil)
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
    case let .welcome(toastMessage):
      let module = resolver.resolve(Module<WelcomeModule>.self)!
      toastMessage.flatMap { message in
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
          self?.view.topViewController?.view.makeToast(message)
        }
      }
      return replaceRoot(module.controller, animated: false)
    case .createWallet:
      let module = resolver.resolve(Module<CreateWalletModule>.self)!
      return push(module.controller)
    case let .phoneVerification(phoneNumber):
      let module = resolver.resolve(Module<PhoneVerificationModule>.self)!
      module.input.setup(phoneNumber: phoneNumber, for: .creation)
      return push(module.controller)
    case let .seedPhrase(phoneNumber, password):
      let module = resolver.resolve(Module<SeedPhraseModule>.self)!
      module.input.setup(for: .creation(phoneNumber, password))
      return replaceLast(module.controller)
    case .recover:
      let module = resolver.resolve(Module<RecoverModule>.self)!
      return push(module.controller)
    case let .recoverSeedPhrase(phoneNumber, password):
      let module = resolver.resolve(Module<RecoverSeedPhraseModule>.self)!
      module.input.setup(phoneNumber: phoneNumber, password: password)
      return replaceLast(module.controller)
    case let .pinCode(stage, pinCode):
      let module = resolver.resolve(Module<PinCodeModule>.self)!
      module.input.setup(for: stage)
      module.input.setup(for: .new)
      if stage == .confirmation {
        module.input.setup(shouldShowNavBar: true)
      }
      
      if let pinCode = pinCode {
        module.input.setup(with: pinCode)
      }
      
      return push(module.controller)
    case .contactSupport:
      let module = resolver.resolve(Module<SupportModule>.self)!
      return push(module.controller)
    case .pop:
      return pop()
    }
  }
}
