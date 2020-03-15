import Swinject

extension VerificationFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(VerificationFlowController.self) { ioc in
          let flowController = VerificationFlowController()
          flowController.delegate = ioc.resolve(VerificationFlowControllerDelegate.self)
          return flowController
        }
        .inObjectScope(.container)
        .implements(VerificationInfoModuleDelegate.self,
                    VerificationModuleDelegate.self,
                    VIPVerificationModuleDelegate.self,
                    PickerFlowControllerDelegate.self)
    }
  }
}
