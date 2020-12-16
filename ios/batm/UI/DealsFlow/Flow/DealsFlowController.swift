import Foundation
import RxSwift
import RxFlow

protocol DealsFlowControllerDelegate: class {}

class DealsFlowController: FlowController, FlowActivator {
    var initialStep: Step = DealsFlow.Steps.deals
    weak var delegate: DealsFlowControllerDelegate?
}

extension DealsFlowController: DealsModuleDelegate {
    func didSelectStaking() {
        step.accept(DealsFlow.Steps.staking)
    }
    
    func didSelectSwap() {
        step.accept(DealsFlow.Steps.swap)
    }
}

extension DealsFlowController: CoinExchangeModuleDelegate {
    func didFinishCoinExchange() {
        step.accept(DealsFlow.Steps.pop(localize(L.CoinDetails.transactionCreated)))
    }
}

extension DealsFlowController: CoinStakingModuleDelegate {
    func didFinishCoinStaking() {
        step.accept(CoinDetailsFlow.Steps.pop(localize(L.CoinDetails.transactionCreated)))
    }
}
