import RxFlow

class TradesFlow: BaseFlow<BTMNavigationController, TradesFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      TradesAssembly(),
      BuySellTradeDetailsAssembly(),
      CreateEditTradeAssembly(),
      ReserveAssembly(),
      RecallAssembly(),
    ]
  }
  
  enum Steps: Step, Equatable {
    case trades(BTMCoin, [CoinBalance], CoinDetails)
    case buySellTradeDetails(CoinBalance, BuySellTrade, TradeType)
    case createEditTrade(CoinBalance)
    case reserve(BTMCoin, [CoinBalance], CoinDetails)
    case recall(BTMCoin, [CoinBalance], CoinDetails)
    case pop
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .trades(coin, coinBalances, coinDetails):
      let module = resolver.resolve(Module<TradesModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails)
      return push(module.controller)
    case let .buySellTradeDetails(coinBalance, trade, type):
      let module = resolver.resolve(Module<BuySellTradeDetailsModule>.self)!
      module.input.setup(coinBalance: coinBalance, trade: trade, type: type)
      return push(module.controller)
    case let .createEditTrade(coinBalance):
      let module = resolver.resolve(Module<CreateEditTradeModule>.self)!
      module.input.setup(coinBalance: coinBalance)
      return push(module.controller)
    case let .reserve(coin, coinBalances, coinDetails):
      let module = resolver.resolve(Module<ReserveModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails)
      return push(module.controller)
    case let .recall(coin, coinBalances, coinDetails):
      let module = resolver.resolve(Module<RecallModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails)
      return push(module.controller)
    case .pop: return pop()
    }
  }
}

