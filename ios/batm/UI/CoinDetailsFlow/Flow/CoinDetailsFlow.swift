import RxFlow

class CoinDetailsFlow: BaseFlow<BTMNavigationController, CoinDetailsFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      CoinDetailsAssembly(),
      CoinWithdrawAssembly(),
      CoinSendGiftAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case coinDetails(CoinBalance)
    case withdraw(BTMCoin, CoinBalance)
    case sendGift(BTMCoin, CoinBalance)
    case pop
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .coinDetails(coinBalance):
      let module = resolver.resolve(Module<CoinDetailsModule>.self)!
      module.input.setup(with: coinBalance)
      return push(module.controller)
    case let .withdraw(coin, coinBalance):
      let module = resolver.resolve(Module<CoinWithdrawModule>.self)!
      module.input.setup(with: coin)
      module.input.setup(with: coinBalance)
      return push(module.controller)
    case let .sendGift(coin, coinBalance):
      let module = resolver.resolve(Module<CoinSendGiftModule>.self)!
      module.input.setup(with: coin)
      module.input.setup(with: coinBalance)
      return push(module.controller)
    case .pop: return pop()
    }
  }
}

