import RxFlow
import TrustWalletCore

class CoinDetailsFlow: BaseFlow<BTMNavigationController, CoinDetailsFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      CoinDetailsAssembly(),
      CoinDepositAssembly(),
      CoinWithdrawAssembly(),
      CoinSendGiftAssembly(),
      CoinSellAssembly(),
      CoinSellDetailsAnotherAddressAssembly(),
      CoinSellDetailsCurrentAddressAssembly(),
      CoinExchangeAssembly(),
      TransactionDetailsAssembly(),
    ]
  }
  
  enum Steps: Step, Equatable {
    case coinDetails([CoinBalance], CoinSettings, PriceChartData)
    case transactionDetails(TransactionDetails, CustomCoinType)
    case deposit(BTMCoin)
    case withdraw(BTMCoin, [CoinBalance], CoinSettings)
    case sendGift(BTMCoin, [CoinBalance], CoinSettings)
    case sell(BTMCoin, [CoinBalance], CoinSettings, SellDetails)
    case sellDetailsForAnotherAddress(SellDetailsForAnotherAddress)
    case sellDetailsForCurrentAddress(SellDetailsForCurrentAddress)
    case exchange(BTMCoin, [CoinBalance], CoinSettings)
    case trades(CoinBalance)
    case pop
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .coinDetails(coinBalances, coinSettings, data):
      let module = resolver.resolve(Module<CoinDetailsModule>.self)!
      module.input.setup(coinBalances: coinBalances, coinSettings: coinSettings, data: data)
      return push(module.controller)
    case let .transactionDetails(details, type):
      let module = resolver.resolve(Module<TransactionDetailsModule>.self)!
      module.input.setup(with: details, for: type)
      return push(module.controller)
    case let .deposit(coin):
      let module = resolver.resolve(Module<CoinDepositModule>.self)!
      module.input.setup(coin: coin)
      return push(module.controller)
    case let .withdraw(coin, coinBalances, coinSettings):
      let module = resolver.resolve(Module<CoinWithdrawModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinSettings: coinSettings)
      return push(module.controller)
    case let .sendGift(coin, coinBalances, coinSettings):
      let module = resolver.resolve(Module<CoinSendGiftModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinSettings: coinSettings)
      return push(module.controller)
    case let .sell(coin, coinBalances, coinSettings, details):
      let module = resolver.resolve(Module<CoinSellModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinSettings: coinSettings, details: details)
      return push(module.controller)
    case let .sellDetailsForAnotherAddress(details):
      let module = resolver.resolve(Module<CoinSellDetailsAnotherAddressModule>.self)!
      module.input.setup(with: details)
      return replaceLast(module.controller)
    case let .sellDetailsForCurrentAddress(details):
      let module = resolver.resolve(Module<CoinSellDetailsCurrentAddressModule>.self)!
      module.input.setup(with: details)
      return replaceLast(module.controller)
    case let .exchange(coin, coinBalances, coinSettings):
      let module = resolver.resolve(Module<CoinExchangeModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinSettings: coinSettings)
      return push(module.controller)
    case let .trades(coinBalance):
      let flow = TradesFlow(view: view, parent: self)
      let step = TradesFlow.Steps.trades(coinBalance)
      return next(flow: flow, step: step)
    case .pop: return pop()
    }
  }
}

