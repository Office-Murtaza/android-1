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
      CoinStakingAssembly(),
      TransactionDetailsAssembly(),
      ReserveAssembly(),
      RecallAssembly(),
    ]
  }
  
  enum Steps: Step, Equatable {
    case coinDetails([CoinBalance], CoinDetails, PriceChartData)
    case transactionDetails(TransactionDetails, CustomCoinType)
    case deposit(BTMCoin)
    case withdraw(BTMCoin, [CoinBalance], CoinDetails)
    case sendGift(BTMCoin, [CoinBalance], CoinDetails)
    case sell(BTMCoin, [CoinBalance], CoinDetails, SellDetails)
    case sellDetailsForAnotherAddress(SellDetailsForAnotherAddress)
    case sellDetailsForCurrentAddress(SellDetailsForCurrentAddress)
    case exchange(BTMCoin, [CoinBalance], CoinDetails)
    case trades(BTMCoin, [CoinBalance], CoinDetails)
    case staking(BTMCoin, [CoinBalance], CoinDetails, StakeDetails)
    case reserve(BTMCoin, [CoinBalance], CoinDetails)
    case recall(BTMCoin, [CoinBalance], CoinDetails)
    case pop(String? = nil)
  }
  
  override func route(to step: Step) -> NextFlowItems {
    return castable(step)
      .map(handleFlow(step:))
      .extract(NextFlowItems.none)
  }
  
  private func handleFlow(step: Steps) -> NextFlowItems {
    switch step {
    case let .coinDetails(coinBalances, coinDetails, data):
      let module = resolver.resolve(Module<CoinDetailsModule>.self)!
      module.input.setup(coinBalances: coinBalances, coinDetails: coinDetails, data: data)
      return push(module.controller)
    case let .transactionDetails(details, type):
      let module = resolver.resolve(Module<TransactionDetailsModule>.self)!
      module.input.setup(with: details, for: type)
      return push(module.controller)
    case let .deposit(coin):
      let module = resolver.resolve(Module<CoinDepositModule>.self)!
      module.input.setup(coin: coin)
      return push(module.controller)
    case let .withdraw(coin, coinBalances, coinDetails):
      let module = resolver.resolve(Module<CoinWithdrawModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails)
      return push(module.controller)
    case let .sendGift(coin, coinBalances, coinDetails):
      let module = resolver.resolve(Module<CoinSendGiftModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails)
      return push(module.controller)
    case let .sell(coin, coinBalances, coinDetails, details):
      let module = resolver.resolve(Module<CoinSellModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails, details: details)
      return push(module.controller)
    case let .sellDetailsForAnotherAddress(details):
      let module = resolver.resolve(Module<CoinSellDetailsAnotherAddressModule>.self)!
      module.input.setup(with: details)
      return replaceLast(module.controller)
    case let .sellDetailsForCurrentAddress(details):
      let module = resolver.resolve(Module<CoinSellDetailsCurrentAddressModule>.self)!
      module.input.setup(with: details)
      return replaceLast(module.controller)
    case let .exchange(coin, coinBalances, coinDetails):
      let module = resolver.resolve(Module<CoinExchangeModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails)
      return push(module.controller)
    case let .trades(coin, coinBalances, CoinDetails):
      let flow = TradesFlow(view: view, parent: self)
      let step = TradesFlow.Steps.trades(coin, coinBalances, CoinDetails)
      return next(flow: flow, step: step)
    case let .staking(coin, coinBalances, coinDetails, stakeDetails):
      let module = resolver.resolve(Module<CoinStakingModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails, stakeDetails: stakeDetails)
      return push(module.controller)
    case let .reserve(coin, coinBalances, coinDetails):
      let module = resolver.resolve(Module<ReserveModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails)
      return push(module.controller)
    case let .recall(coin, coinBalances, coinDetails):
      let module = resolver.resolve(Module<RecallModule>.self)!
      module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails)
      return push(module.controller)
    case let .pop(toastMessage):
      toastMessage.flatMap { message in
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
          self?.view.topViewController?.view.makeToast(message)
        }
      }
      return pop()
    }
  }
}

