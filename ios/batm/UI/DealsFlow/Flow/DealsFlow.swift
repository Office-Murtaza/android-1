import RxFlow
import RxSwift

class DealsFlow: BaseFlow<BTMNavigationController, DealsFlowController> {
    override func assemblies() -> [Assembly] {
        return [Dependencies(),
                DealsAssembly(),
                CoinExchangeAssembly(),
                CoinStakingAssembly(),
                CoinExchangeAssembly(),
                TransferSelectReceiverAssembly()]
    }
    
    enum Steps: Step, Equatable {
        case deals
        case staking
        case swap
        case transfer
        case p2p
        case popToRoot(String?=nil)
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
        case .staking:
            let module = resolver.resolve(Module<CoinStakingModule>.self)!
            module.input.setup()
            return push(module.controller)
        case .swap:
            let module = resolver.resolve(Module<CoinExchangeModule>.self)!
            return push(module.controller)
        case .transfer:
            let transfer = TransferFlow(view: self.view, parent: self)
            let step = TransferFlow.Steps.transfer
            return next(flow: transfer, step: step)
        case let .popToRoot(toastMessage):
            toastMessage.flatMap { message in
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
                    self?.view.topViewController?.view.makeToast(message)
                }
            }
            return popToRoot()
        case .p2p: return push(UIViewController())//print("start p2p flow")
        }
    }
}
