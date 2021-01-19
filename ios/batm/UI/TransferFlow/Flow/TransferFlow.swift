
import RxFlow
import RxSwift

class TransferFlow: BaseFlow<BTMNavigationController, TransferFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      CoinSendGiftAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case transfer
    case sendGift(BContact)
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case .transfer:
       let module = resolver.resolve(Module<TransferModule>.self)!
       return push(module.controller, animated: true)
    case let .sendGift(contact):
        let module = resolver.resolve(Module<CoinSendGiftModule>.self)!
        module.input.setupContact(contact)
        return push(module.controller, animated: true)
    }
  }
}


extension TransferFlow {
    class Dependencies: Assembly {
      
      func assemble(container: Container) {
        container
          .register(TransferFlowController.self) { ioc in
            let flowController = TransferFlowController()
            flowController.delegate = ioc.resolve(TransferFlowControllerDelegate.self)
            return flowController
          }
          .inObjectScope(.container)
            .implements(TransferModuleDelegate.self)
      }
    }
}
