import RxFlow
import TrustWalletCore

class CoinDetailsFlow: BaseFlow<BTMNavigationController, CoinDetailsFlowController> {
    private var coinDetailsModule: Module<CoinDetailsModule>?

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
            ReserveAssembly(),
            RecallAssembly(),
        ]
    }
    
    enum Steps: Step, Equatable {
        case coinDetails(CustomCoinType)
        case updateCoinDetails(String?, TransactionDetails?)
        case transactionDetails(TransactionDetails, CustomCoinType)
        case deposit(CustomCoinType)
        case withdraw(CustomCoinType)
        case sellDetailsForAnotherAddress(SellDetailsForAnotherAddress)
        case sellDetailsForCurrentAddress(SellDetailsForCurrentAddress)
        case reserve(CustomCoinType)
        case recall(CustomCoinType)
        case coinDetailsPredefinedData(CoinDetailsPredefinedDataConfig)
        case pop(String? = nil)
    }
    
    override func route(to step: Step) -> NextFlowItems {
        return castable(step)
            .map(handleFlow(step:))
            .extract(NextFlowItems.none)
    }
    
    private func handleFlow(step: Steps) -> NextFlowItems {
        switch step {
        case let .coinDetails(coinType):
            let module = resolver.resolve(Module<CoinDetailsModule>.self)!
            self.coinDetailsModule = module
            module.input.setup(with: coinType)
            return push(module.controller)
        case let .updateCoinDetails(message, transactionDetails):
            guard let coinDetailsModule = coinDetailsModule else { return pop() }
            coinDetailsModule.input.setup(transactionDetails: transactionDetails)
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
              self?.view.topViewController?.view.makeToast(message)
            }
            return pop()
        case let .coinDetailsPredefinedData(config):
            let module = resolver.resolve(Module<CoinDetailsModule>.self)!
            module.input.setup(predefinedData: config)
            return push(module.controller)
        case let .transactionDetails(transactionDetails, coinType):
            let module = resolver.resolve(Module<TransactionDetailsModule>.self)!
            module.input.setup(with: transactionDetails, coinType: coinType)
            return push(module.controller)
        case let .deposit(coinType):
            let module = resolver.resolve(Module<CoinDepositModule>.self)!
            module.input.setup(with: coinType)
            return push(module.controller)
        case let .withdraw(coinType):
            let module = resolver.resolve(Module<CoinWithdrawModule>.self)!
            module.input.setup(with: coinType)
            return push(module.controller)
        case let .sellDetailsForAnotherAddress(details):
            let module = resolver.resolve(Module<CoinSellDetailsAnotherAddressModule>.self)!
            module.input.setup(with: details)
            return replaceLast(module.controller)
        case let .sellDetailsForCurrentAddress(details):
            let module = resolver.resolve(Module<CoinSellDetailsCurrentAddressModule>.self)!
            module.input.setup(with: details)
            return replaceLast(module.controller)
        case let .reserve(coinType):
            let module = resolver.resolve(Module<ReserveModule>.self)!
            module.input.setup(with: coinType)
            return push(module.controller)
        case let .recall(coinType):
            let module = resolver.resolve(Module<RecallModule>.self)!
            module.input.setup(with: coinType)
            return push(module.controller)
        case let .pop(toastMessage):
            toastMessage.flatMap { message in
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
                    self?.view.topViewController?.view.makeToast(message)
                }
            }
            return pop()
        }
    }
}

