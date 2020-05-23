import RxFlow

class TradesFlow: BaseFlow<BTMNavigationController, TradesFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      TradesAssembly(),
    ]
  }
  
  enum Steps: Step, Equatable {
    case trades(CoinBalance)
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .trades(coinBalance):
      let module = resolver.resolve(Module<TradesModule>.self)!
      module.input.setup(coinBalance: coinBalance)
      return push(module.controller)
    }
  }
}

