import RxFlow

protocol UnlinkFlowControllerDelegate: class {
  func didFinishUnlinkFlow()
}

class UnlinkFlowController: FlowController {
  
  weak var delegate: UnlinkFlowControllerDelegate?
  weak var module: UnlinkModule?
  
}

extension UnlinkFlowController: UnlinkModuleDelegate {
  
  func didFinishUnlink() {
    delegate?.didFinishUnlinkFlow()
  }
  
  func didUnlink(from module: UnlinkModule) {
    self.module = module
    step.accept(UnlinkFlow.Steps.enterPassword)
  }
  
}

extension UnlinkFlowController: EnterPasswordModuleDelegate {
  
  func didFinishEnterPassword() {
    delegate?.didFinishUnlinkFlow()
  }
  
  func didMatchPassword() {
    module?.unlink()
  }
  
}
