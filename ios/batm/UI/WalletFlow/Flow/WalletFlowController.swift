import Foundation
import RxSwift
import RxFlow

protocol WalletFlowControllerDelegate: AnyObject {}

class WalletFlowController: FlowController, FlowActivator {
    weak var delegate: WalletFlowControllerDelegate?
    weak var module: WalletModule?
    var initialStep: Step = WalletFlow.Steps.wallet
    
}

extension WalletFlowController: WalletModuleDelegate {
    func showCoinDetails(for type: CustomCoinType) {
        step.accept(WalletFlow.Steps.coinDetails(type))
    }
    
    func showCoinDetail(predefinedConfig: CoinDetailsPredefinedDataConfig) {
        step.accept(WalletFlow.Steps.coinDetailsPredefinedData(predefinedConfig))
    }
}

extension WalletFlowController: CoinDetailsFlowControllerDelegate {}
