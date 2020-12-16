import Foundation

protocol DealsModule: class {}
protocol DealsModuleDelegate: class {
    func didSelectStaking()
    func didSelectSwap()
}
