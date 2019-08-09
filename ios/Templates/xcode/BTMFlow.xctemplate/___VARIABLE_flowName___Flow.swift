import RxFlow

class ___FILEBASENAME___: BaseFlow<BTMNavigationController, ___VARIABLE_flowName___FlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies()
    ]
  }
  
  enum Steps: Step, Equatable {
    
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    default: return .none
    }
  }
}

