import RxFlow

class TradesFlow: BaseFlow<BTMNavigationController, TradesFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      TradesAssembly(),
      BuySellTradeDetailsAssembly(),
      CreateEditTradeAssembly(),
      ReserveAssembly(),
    ]
  }
  
  enum Steps: Step, Equatable {
    case trades(BTMCoin, [CoinBalance], CoinSettings)
    case buySellTradeDetails(CoinBalance, BuySellTrade, TradeType)
    case createEditTrade(CoinBalance)
    case reserve(BTMCoin, [CoinBalance], CoinSettings)
    case pop
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .trades(coin, coinBalances, coinSettings):
      let module = resolver.resolve(Module<TradesModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinSettings: coinSettings)
      return push(module.controller)
    case let .buySellTradeDetails(coinBalance, trade, type):
      let module = resolver.resolve(Module<BuySellTradeDetailsModule>.self)!
      module.input.setup(coinBalance: coinBalance, trade: trade, type: type)
      return push(module.controller)
    case let .createEditTrade(coinBalance):
      let module = resolver.resolve(Module<CreateEditTradeModule>.self)!
      module.input.setup(coinBalance: coinBalance)
      return push(module.controller)
    case let .reserve(coin, coinBalances, coinSettings):
      let module = resolver.resolve(Module<ReserveModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinSettings: coinSettings)
      return push(module.controller)
    case .pop: return pop()
    }
  }
}

