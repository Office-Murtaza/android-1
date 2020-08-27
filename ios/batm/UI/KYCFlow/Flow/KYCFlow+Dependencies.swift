import Swinject

extension KYCFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(KYCFlowController.self) { ioc in
          let flowController = KYCFlowController()
          flowController.delegate = ioc.resolve(KYCFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(KYCModuleDelegate.self,
                    VerificationModuleDelegate.self,
                    VIPVerificationModuleDelegate.self,
                    PickerFlowControllerDelegate.self)
    }
  }
}
