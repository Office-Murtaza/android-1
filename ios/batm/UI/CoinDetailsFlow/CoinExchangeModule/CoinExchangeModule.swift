import Foundation

protocol CoinExchangeModule: AnyObject {
    func setup()
}
protocol CoinExchangeModuleDelegate: AnyObject {
    func didFinishCoinExchange()
    func handleError()
}
