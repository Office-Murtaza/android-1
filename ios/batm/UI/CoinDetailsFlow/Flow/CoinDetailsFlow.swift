import RxFlow
import TrustWalletCore

class CoinDetailsFlow: BaseFlow<BTMNavigationController, CoinDetailsFlowController> {
  override func assemblies() -> [Assembly] {
    return [
      Dependencies(),
      CoinDetailsAssembly(),
      CoinWithdrawAssembly(),
      CoinSendGiftAssembly(),
      CoinSellAssembly(),
      CoinSellDetailsAnotherAddressAssembly(),
      CoinSellDetailsCurrentAddressAssembly(),
      TransactionDetailsAssembly()
    ]
  }
  
  enum Steps: Step, Equatable {
    case coinDetails(CoinBalance)
    case transactionDetails(TransactionDetails, CoinType)
    case withdraw(BTMCoin, CoinBalance)
    case sendGift(BTMCoin, CoinBalance)
    case sell(BTMCoin, CoinBalance, SellDetails)
    case sellDetailsForAnotherAddress(SellDetailsForAnotherAddress)
    case sellDetailsForCurrentAddress(SellDetailsForCurrentAddress)
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
    case let .transactionDetails(details, type):
      let module = resolver.resolve(Module<TransactionDetailsModule>.self)!
      module.input.setup(with: details, for: type)
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
    case let .sell(coin, coinBalance, details):
      let module = resolver.resolve(Module<CoinSellModule>.self)!
      module.input.setup(coin: coin, coinBalance: coinBalance, details: details)
      return push(module.controller)
    case let .sellDetailsForAnotherAddress(details):
      let module = resolver.resolve(Module<CoinSellDetailsAnotherAddressModule>.self)!
      module.input.setup(with: details)
      return replaceLast(module.controller)
    case let .sellDetailsForCurrentAddress(details):
      let module = resolver.resolve(Module<CoinSellDetailsCurrentAddressModule>.self)!
      module.input.setup(with: details)
      return replaceLast(module.controller)
    case .pop: return pop()
    }
  }
}

