import RxFlow
import RxSwift

class DealsFlow: BaseFlow<BTMNavigationController, DealsFlowController> {
    override func assemblies() -> [Assembly] {
        return [Dependencies(),
                DealsAssembly(),
                CoinExchangeAssembly(),
                CoinStakingAssembly(),
                CoinExchangeAssembly()]
    }
    
    enum Steps: Step, Equatable {
        case deals
        case staking(BTMCoin, [CoinBalance], CoinDetails, StakeDetails)
        case swap
        case pop(String? = nil)
    }
    
    override func route(to step: Step) -> NextFlowItems {
        return castable(step)
            .map(handleFlow(step:))
            .extract(NextFlowItems.none)
    }
    
    private func handleFlow(step: Steps) -> NextFlowItems {
        switch step {
        case .deals:
          let module = resolver.resolve(Module<DealsModule>.self)!
            module.controller.title = localize(L.Deals.title)
            module.controller.tabBarItem.image = UIImage(named: "tab_bar_deals")
            module.controller.tabBarItem.selectedImage = UIImage(named: "tab_bar_active_deals")
            return push(module.controller, animated: false)
        case let .staking(coin, coinBalances, coinDetails, stakeDetails):
            let module = resolver.resolve(Module<CoinStakingModule>.self)!
            module.input.setup(coin: coin, coinBalances: coinBalances, coinDetails: coinDetails, stakeDetails: stakeDetails)
            return push(module.controller)
        case .swap:
            let module = resolver.resolve(Module<CoinExchangeModule>.self)!
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
