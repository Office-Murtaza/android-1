import Foundation
import RxFlow

final class LoginFlow: BaseFlow<BTMNavigationController, LoginFlowController> {
  
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      WelcomeAssembly(),
      CreateWalletAssembly(),
      SeedPhraseAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case welcome
    case createWallet
    case seedPhrase
    case backToWelcome
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
      view.setViewControllers([module.controller], animated: false)
      return .none
    case .createWallet:
      let module = resolver.resolve(Module<CreateWalletModule>.self)!
      return push(module.controller)
    case .seedPhrase:
      let module = resolver.resolve(Module<SeedPhraseModule>.self)!
      return push(module.controller)
    case .backToWelcome:
      let module = resolver.resolve(Module<WelcomeModule>.self)!
      return replaceRoot(module.controller, animated: true)
    case .pop:
      return pop()
    }
  }
}
